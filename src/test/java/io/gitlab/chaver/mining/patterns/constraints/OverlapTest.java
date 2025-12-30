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

import io.gitlab.chaver.mining.patterns.constraints.factory.ConstraintFactory;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OverlapTest {

    Overlap createOverlap(String dataPath, boolean addConstraint, double jmax, int theta) throws Exception {
        TransactionalDatabase database = new DatReader(dataPath).read();
        Model model = new Model("Diversity");
        IntVar freq = model.intVar("freq", theta, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new PropCoverSize(database, freq, x)));
        model.post(new Constraint("Cover Closure", new PropCoverClosure(database, x)));
        Overlap overlap = ConstraintFactory.overlap(database, x, jmax, theta);
        overlap.post();
        if (!addConstraint) {
            overlap.getPropagator(0).setEnabled(false);
        }
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                x
        ));
        return overlap;
    }

    @Test
    void test() throws Exception {
        double jmax = 0.05;
        int theta = 15;
        Overlap overlap = createOverlap("src/test/resources/iris/iris.dat", true, jmax, theta);
        while (overlap.getPropagator(0).getModel().getSolver().solve());
        List<BitSet> covers = overlap.getCoversHistory();
        for (BitSet cover : covers) {
            for (BitSet cover2 : covers) {
                if (!cover.equals(cover2)) {
                    double jaccard = Overlap.computeJaccard(cover, cover2);
                    assertTrue(jaccard <= jmax);
                }
            }
        }
    }

    @Test
    void test2() throws Exception {
        double jmax = 0.05;
        int theta = 5;
        String dataPath = "src/test/resources/glass/glass.dat";
        Overlap overlap = createOverlap(dataPath, true, jmax, theta);
        while (overlap.getPropagator(0).getModel().getSolver().solve());
        //overlap.getPropagator(0).getModel().getSolver().printStatistics();
        Overlap overlap1 = createOverlap(dataPath, false, jmax, theta);
        while (overlap1.getPropagator(0).getModel().getSolver().solve());
        //overlap1.getPropagator(0).getModel().getSolver().printStatistics();
        assertTrue(overlap.getCoversHistory().equals(overlap1.getCoversHistory()));
        List<BitSet> covers = overlap.getCoversHistory();
        for (BitSet cover : covers) {
            for (BitSet cover2 : covers) {
                if (!cover.equals(cover2)) {
                    double jaccard = Overlap.computeJaccard(cover, cover2);
                    assertTrue(jaccard <= jmax);
                }
            }
        }
    }

}