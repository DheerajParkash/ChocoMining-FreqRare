/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.examples;

import io.gitlab.chaver.mining.patterns.constraints.factory.ConstraintFactory;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.stream.IntStream;

import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

/**
 * Example of Minimal Non-Redundant (MNR) association rule mining
 */
public class ExampleMNRsMining {

    static int[] getItemset(BoolVar[] x, TransactionalDatabase database) {
        return IntStream
                .range(0, x.length)
                .filter(i -> x[i].getValue() == 1)
                .map(i -> database.getItems()[i])
                .toArray();
    }

    public static void main(String[] args) throws Exception {
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
        // Min frequency of the rule (absolute value)
        int minFreq = 1;
        // Min confidence of the rule (percentage)
        int minConf = 20;
        Model model = new Model("MNRs mining");
        // Antecedent of the rule : x
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        // Consequent of the rule : y
        BoolVar[] y = model.boolVarArray("y", database.getNbItems());
        // z = x U y
        BoolVar[] z = model.boolVarArray("z", database.getNbItems());
        for (int i = 0; i < database.getNbItems(); i++) {
            // Ensure that an item i is not in the antecedent and consequent of the rule at the same time
            model.arithm(x[i], "+", y[i], "<=", 1).post();
            // z[i] = x[i] OR y[i]
            model.addClausesBoolOrEqVar(x[i], y[i], z[i]);
        }
        // sum(x) >= 1 (i.e. the antecedent of the rule is not empty)
        model.addClausesBoolOrArrayEqualTrue(x);
        // sum(y) >= 1 (i.e. the consequent of the rule is not empty)
        model.addClausesBoolOrArrayEqualTrue(y);
        // Frequency of z
        IntVar freqZ = model.intVar("freqZ", minFreq, database.getNbTransactions());
        ConstraintFactory.coverSize(database, freqZ, z).post();
        // Frequency of x
        IntVar freqX = model.intVar("freqX", minFreq, database.getNbTransactions());
        ConstraintFactory.coverSize(database, freqX, x).post();
        // Confidence of the rule = freqZ / freqX (multiplied by 100 to get an integer variable)
        freqZ.mul(100).ge(freqX.mul(minConf)).post();
        // Ensures that x is a generator (i.e. it has no subset with the same frequency)
        ConstraintFactory.generator(database, x).post();
        // Ensures that z is closed w.r.t. the frequency
        ConstraintFactory.coverClosure(database, z).post();
        Solver solver = model.getSolver();
        // Search strategy : first, instantiate x, then y, then z
        solver.setSearch(intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                ArrayUtils.append(x, y, z)
        ));
        System.out.println("List of the MNRs with a min freq>=1 and a min confidence>=0.2");
        while (solver.solve()) {
            double conf = (double) freqZ.getValue() / freqX.getValue();
            System.out.println(Arrays.toString(getItemset(x, database)) + " => " +
                    Arrays.toString(getItemset(y, database)) + ", freq=" + freqZ.getValue() + ", conf=" + conf);
        }
    }
}
