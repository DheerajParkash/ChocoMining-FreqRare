/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import java.util.HashSet;
import java.util.Set;

public class PropFreqRare extends Propagator<BoolVar> {
    private final int[] minSupports; // Array of minimum support thresholds for each item
    private final Set<Integer> U;    // Unassigned items
    private final Set<Integer> N;    // Items removed from U
    private final int[][] transactions; // Database transactions

    public PropFreqRare(BoolVar[] vars, int[] minSupports, int[][] transactions) {
        super(vars, PropagatorPriority.LINEAR, false);
        this.minSupports = minSupports;
        this.transactions = transactions;
        this.U = new HashSet<>();
        this.N = new HashSet<>();
        for (int i = 0; i < vars.length; i++) {
            U.add(i);
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        Set<Integer> cover = computeCover();
        int s = computeMinSupport();

        if (cover.size() < s) {
            fails(); // Prune the search space if cover size is less than min support
        }

        for (Integer i : new HashSet<>(U)) {
            if (computeCoverIntersectionSize(cover, i) < s) {
                vars[i].removeValue(1, this);
                U.remove(i);
                N.add(i);
            }
        }
    }

    private Set<Integer> computeCover() {
        Set<Integer> cover = new HashSet<>();
        for (int t = 0; t < transactions.length; t++) {
            Set<Integer> transactionItems = new HashSet<>();
            for (int i = 0; i < transactions[t].length; i++) {
                transactionItems.add(transactions[t][i]);
            }
            boolean include = true;
            for (int i : U) {
                if (vars[i].isInstantiatedTo(1) && !transactionItems.contains(i)) {
                    include = false;
                    break;
                }
            }
            if (include) {
                cover.add(t);
            }
        }
        return cover;
    }
    

    private int computeMinSupport() {
        int minSupport = Integer.MAX_VALUE;
        for (Integer i : U) {
            minSupport = Math.min(minSupport, minSupports[i]);
        }
        return minSupport;
    }

    private int computeCoverIntersectionSize(Set<Integer> cover, int item) {
        int count = 0;
        for (Integer t : cover) {
            // Ensure item index is within bounds before accessing it
            if (item < transactions[t].length && transactions[t][item] == 1) {
                count++;
            }
        }
        return count;
    }

    @Override
    public ESat isEntailed() {
        Set<Integer> cover = computeCover();
        int s = computeMinSupport();
        if (cover.size() < s) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
}
