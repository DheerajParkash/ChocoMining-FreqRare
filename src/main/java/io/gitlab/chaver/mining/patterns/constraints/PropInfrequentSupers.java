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

import java.util.HashSet;
import java.util.Set;

/**
 * Given a vector of Boolean variables x and a threshold freq, ensures that all the supersets of x are infrequent w.r.t. freq
 * Fore more information, see Belaid et al. - Constraint Programming for Mining Borders of Frequent Itemsets
 */
public class PropInfrequentSupers extends Propagator<IntVar> {

    private BoolVar[] x;
    private int freq;
    private TransactionalDatabase database;
    private long[][] dataset;

    public PropInfrequentSupers(TransactionalDatabase database, int freq, BoolVar[] x) {
        super(x);
        this.freq = freq;
        this.x = x;
        this.database = database;
        dataset = database.getDatasetAsLongArray();
    }

    private SparseBitSet createCover() {
        return new SparseBitSet(database.getNbTransactions());
    }

    private SparseBitSet computeCover2(Set<Integer> presentItems, Set<Integer> freeItems, int i) {
        SparseBitSet cover = createCover();
        for (int j : presentItems) {
            cover.and(dataset[j]);
        }
        for (int j : freeItems) {
            if (j != i) {
                cover.and(dataset[j]);
            }
        }
        return cover;
    }

    private Set<Integer> unionSet(Set<Integer> items, int i) {
        Set<Integer> union = new HashSet<>(items);
        union.add(i);
        return union;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SparseBitSet cover = createCover();
        Set<Integer> presentItems = new HashSet<>();
        Set<Integer> absentItems = new HashSet<>();
        Set<Integer> freeItems = new HashSet<>();
        for (int i = database.getNbClass(); i < database.getNbItems(); i++) {
            if (x[i].isInstantiatedTo(1)) {
                cover.and(dataset[i]);
                presentItems.add(i);
            }
            if (x[i].isInstantiatedTo(0)) {
                absentItems.add(i);
            }
            if (!x[i].isInstantiated()) {
                cover.and(dataset[i]);
                freeItems.add(i);
            }
        }
        if (cover.cardinality() >= freq) {
            for (int j : absentItems) {
                if (cover.andCount(dataset[j]) >= freq) {
                    fails();
                }
            }
        }
        for (int i : freeItems) {
            SparseBitSet cover2 = computeCover2(presentItems, freeItems, i);
            if (cover2.cardinality() >= freq) {
                Set<Integer> union = unionSet(absentItems, i);
                for (int j : union) {
                    if (cover2.andCount(dataset[j]) >= freq) {
                        x[i].setToTrue(this);
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
