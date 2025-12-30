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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class FreqRareConstraint extends Constraint {

    private static class FreqRarePropagator extends Propagator<BoolVar> {
        private final int[] mis;
        private final Map<Integer, BitSet> itemCoverMap;
        private final int totalTransactions;

        public FreqRarePropagator(BoolVar[] vars, int[] mis, 
                                 Map<Integer, BitSet> itemCoverMap, int totalTransactions) {
            super(vars);
            this.mis = mis;
            this.itemCoverMap = itemCoverMap;
            this.totalTransactions = totalTransactions;
        }

        @Override
        public void propagate(int evtmask) throws ContradictionException {
            BitSet P = new BitSet(vars.length);
            BitSet U = new BitSet(vars.length);
            for (int i = 0; i < vars.length; i++) {
                if (vars[i].isInstantiated()) {
                    if (vars[i].getValue() == 1) P.set(i);
                } else {
                    U.set(i);
                }
            }

            // Compute cover(P)
            BitSet coverP = new BitSet(totalTransactions);
            if (P.isEmpty()) {
                coverP.set(0, totalTransactions); // cover(P) = all transactions when P is empty
            } else {
                boolean first = true;
                for (int i = P.nextSetBit(0); i >= 0; i = P.nextSetBit(i+1)) {
                    if (first) {
                        coverP.or(itemCoverMap.get(i));
                        first = false;
                    } else {
                        coverP.and(itemCoverMap.get(i));
                    }
                }
            }
            int coverSize = coverP.cardinality();

            // Compute minimum MIS
            int s = Integer.MAX_VALUE;
            if (P.isEmpty()) {
                // When P is empty, s = min(MIS of all unassigned items)
                for (int i = U.nextSetBit(0); i >= 0; i = U.nextSetBit(i+1)) {
                    s = Math.min(s, mis[i]);
                }
            } else {
                for (int i = P.nextSetBit(0); i >= 0; i = P.nextSetBit(i+1)) {
                    s = Math.min(s, mis[i]);
                }
                for (int i = U.nextSetBit(0); i >= 0; i = U.nextSetBit(i+1)) {
                    s = Math.min(s, mis[i]);
                }
            }

            // Check frequency constraint
            if (coverSize < s) {
                fails();
            }

            // Prune inconsistent items
            for (int i = U.nextSetBit(0); i >= 0; i = U.nextSetBit(i+1)) {
                BitSet coverPi = (BitSet) coverP.clone();
                coverPi.and(itemCoverMap.get(i));
                if (coverPi.cardinality() < s) {
                    vars[i].setToFalse(this);
                }
            }
        }

        @Override
        public ESat isEntailed() {
            return ESat.TRUE;
        }
    }

    public FreqRareConstraint(String name, BoolVar[] vars, int[] mis,
                             Map<Integer, BitSet> itemCoverMap, int totalTransactions) {
        super(name, new FreqRarePropagator(vars, mis, itemCoverMap, totalTransactions));
    }

    public static void post(BoolVar[] items, int[] mis, 
                       Map<Integer, BitSet> itemCovers, int transactionCount) {
        Model model = items[0].getModel();
        // Add constraint: itemset must be non-empty
        model.sum(items, ">=", 1).post(); 
        model.post(new FreqRareConstraint(
            "FreqRare", 
            items, 
            mis, 
            itemCovers, 
            transactionCount
        ));
    }
}