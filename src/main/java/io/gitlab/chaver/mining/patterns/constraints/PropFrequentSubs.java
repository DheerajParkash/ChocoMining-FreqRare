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

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.util.SparseBitSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * Given a vector x of Boolean variables and a threshold freq, ensures that all the subsets of x are frequent w.r.t. freq
 * Fore more information, see Belaid et al. - Constraint Programming for Mining Borders of Frequent Itemsets
 *
 */
public class PropFrequentSubs extends Propagator<IntVar> {

    private BoolVar[] x;
    private int freq;
    private TransactionalDatabase database;
    private long[][] dataset;

    public PropFrequentSubs(TransactionalDatabase database, int freq, BoolVar[] x) {
        super(x);
        this.freq = freq;
        this.x = x;
        this.database = database;
        dataset = database.getDatasetAsLongArray();
    }

    private SparseBitSet createCover() {
        return new SparseBitSet(database.getNbTransactions());
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SparseBitSet cover = createCover();
        Set<Integer> presentItems = new HashSet<>();
        Set<Integer> freeItems = new HashSet<>();
        for (int i = database.getNbClass(); i < database.getNbItems(); i++) {
            if (x[i].isInstantiatedTo(1)) {
                presentItems.add(i);
                cover.and(dataset[i]);
            }
            if (!x[i].isInstantiated()) {
                freeItems.add(i);
            }
        }
        Map<Integer, SparseBitSet> subcovers = new HashMap<>();
        for (int i : presentItems) {
            SparseBitSet subcover = createCover();
            for (int j : presentItems) {
                if (j != i) {
                    subcover.and(dataset[j]);
                }
            }
            if (subcover.cardinality() < freq) {
                fails();
            }
            subcovers.put(i, subcover);
        }
        if (cover.cardinality() < freq) {
            for (int i : freeItems) {
                x[i].setToFalse(this);
            }
        }
        for (int i : freeItems) {
            if (cover.andCount(dataset[i]) < freq) {
                for (int j : presentItems) {
                    if (subcovers.get(j).andCount(dataset[i]) < freq) {
                        x[i].setToFalse(this);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }
}
