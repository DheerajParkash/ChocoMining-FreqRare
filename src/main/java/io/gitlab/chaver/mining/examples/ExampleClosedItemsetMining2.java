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
import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables.MinCov;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;

/**
 * Example of closed pattern mining w.r.t. the set of measures M = {freq(x), max(x.freq)}
 */
public class ExampleClosedItemsetMining2 {

    public static void main(String[] args) throws Exception {
        // Read the transactional database
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
        // List of measures to be closed
        List<Measure> measures = Arrays.asList(freq(), maxFreq());
        // Create the Choco model
        Model model = new Model("Closed Itemset Mining with multiple measures");
        // Create the variables
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        // itemFreq[i] is equal to the frequency of item i in the database
        int[] itemFreq = database.computeItemFreq();
        IntVar[] itemFreqVar = model.intVarArray(database.getNbItems(), 0, database.getNbTransactions());
        for (int i = 0; i < database.getNbItems(); i++) {
            // itemFreqVar[i] = itemFreq[i] if items[i] == 1 else 0
            model.arithm(x[i], "*", model.intVar(itemFreq[i]), "=", itemFreqVar[i]).post();
        }
        // The maximum frequency of x represents the maximum frequency of its items
        // For example, if x = ABC, freq(A) = 5, freq(B) = 7, freq(C) = 3, then maxFreq(x) = 7
        IntVar maxFreq = model.intVar(maxFreq().getId(), 0, database.getNbTransactions());
        // Compute max value of itemFreqVar
        model.max(maxFreq, itemFreqVar).post();
        ConstraintFactory.coverSize(database, freq, x).post();
        // The constraint AdequateClosure ensures that x is closed w.r.t. M
        // Two versions are available : Domain Consistency (DC) and Weak Consistency (WC)
        // Note that the WC version is more time efficient than the DC one
        ConstraintFactory.adequateClosure(database, measures, x, true).post();
        Solver solver = model.getSolver();
        // Variable heuristic : select item i such that freq(x U i) is minimal
        // Value heuristic : instantiate it first to 0
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                x
        ));
        List<Pattern> closedPatterns = new LinkedList<>();
        while (solver.solve()) {
            int[] itemset = IntStream.range(0, x.length)
                    .filter(i -> x[i].getValue() == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            closedPatterns.add(new Pattern(itemset, new int[]{freq.getValue(), maxFreq.getValue()}));
        }
        System.out.println("List of closed patterns for the dataset contextPasquier99 w.r.t. M = {freq(x),max(x.freq)}:");
        for (Pattern closed : closedPatterns) {
            System.out.println(Arrays.toString(closed.getItems()) + ", freq=" + closed.getMeasures()[0] + ", maxFreq=" +
                    closed.getMeasures()[1]);
        }
    }
}
