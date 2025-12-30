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
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * A constraint wrapper for the constraint Overlap to access itemsets and covers history, and automatically plug the
 * solution monitor.
 */
public class Overlap extends Constraint implements IMonitorSolution {

    private TransactionalDatabase database;
    private BitSet[] verticalRepresentation;
    private BoolVar[] x;
    private double jmax;
    private int theta;
    private PropOverlap propOverlap;

    public Overlap(TransactionalDatabase database, BoolVar[] x, double jmax, int theta) {
        super("Overlap", new PropOverlap(database, x, jmax, theta));
        this.database = database;
        this.verticalRepresentation = database.getVerticalRepresentation();
        this.x = x;
        this.jmax = jmax;
        this.theta = theta;
        this.propOverlap = (PropOverlap) getPropagators()[0];
        // Plug this search monitor to check if the solution itemset is diverse w.r.t. the history before adding it
        x[0].getModel().getSolver().plugMonitor(this);
    }

    private BitSet createCover() {
        BitSet cover = new BitSet(database.getNbTransactions());
        cover.set(0, database.getNbTransactions());
        return cover;
    }

    @Override
    public void onSolution() {
        int[] itemset = IntStream
                .range(0, database.getNbItems())
                .filter(i -> x[i].getValue() == 1)
                .toArray();
        BitSet cover = createCover();
        for (int i : itemset) {
            cover.and(verticalRepresentation[i]);
        }
        for (BitSet HCover : propOverlap.getCoversHistory()) {
            if (computeJaccard(cover, HCover) > jmax) {
                return;
            }
        }
        propOverlap.getItemsetsHistory().add(itemset);
        propOverlap.getCoversHistory().add(cover);
    }

    public static double computeJaccard(BitSet cov, BitSet cov2) {
        BitSet inter = (BitSet) cov.clone();
        inter.and(cov2);
        BitSet union = (BitSet) cov.clone();
        union.or(cov2);
        return (double) inter.cardinality() / union.cardinality();
    }

    public List<int[]> getItemsetsHistory() {
        return propOverlap.getItemsetsHistory();
    }

    public List<BitSet> getCoversHistory() {
        return propOverlap.getCoversHistory();
    }
}
