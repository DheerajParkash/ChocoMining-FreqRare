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
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;
import io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables.MinCov;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.objective.ParetoMaximizer;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;

/**
 * Example of skypattern mining w.r.t. the set of measures M = {freq(x),area(x),allconf(x)}
 */
public class ExampleSkypatternMining {

    public static void main(String[] args) throws Exception {
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
        Model model = new Model("Skypattern Mining");
        List<Measure> M = Arrays.asList(freq(), area(), allConf());
        // Compute M' such that M is maximally M-skylineable (see Ugarte et al.)
        Set<Measure> M_prime = MeasureOperand.maxConvert(M);
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        // The area is the product between the frequency and the length
        IntVar area = freq.mul(length).intVar();
        model.sum(x, "=", length).post();
        int[] itemFreq = database.computeItemFreq();
        IntVar[] itemFreqVar = model.intVarArray(database.getNbItems(), 0, database.getNbTransactions());
        for (int i = 0; i < database.getNbItems(); i++) {
            // itemFreqVar[i] = itemFreq[i] if items[i] == 1 else 0
            model.arithm(x[i], "*", model.intVar(itemFreq[i]), "=", itemFreqVar[i]).post();
        }
        IntVar maxFreq = model.intVar(maxFreq().getId(), 0, database.getNbTransactions());
        // Compute max value of itemFreqVar
        model.max(maxFreq, itemFreqVar).post();
        // Aconf is the frequency of x divided by the maximum frequency of its items
        // Aconf is converted to an integer variable (multiplied by 10000)
        IntVar aconf = freq.mul(10000).div(maxFreq).intVar();
        ConstraintFactory.coverSize(database, freq, x).post();
        // Ensures that x is closed w.r.t. M'
        ConstraintFactory.adequateClosure(database, new ArrayList<>(M_prime), x, false).post();
        // We want to Pareto maximize {freq(x), area(x), aconf(x)}
        IntVar[] objectives = new IntVar[]{freq, area, aconf};
        ParetoMaximizer maximizer = new ParetoMaximizer(objectives);
        // Post a Pareto constraint to ensure that the next itemset is not dominated
        model.post(new Constraint("Pareto", maximizer));
        Solver solver = model.getSolver();
        solver.plugMonitor(maximizer);
        // We use the MinCov branching strategy to select the next item to branch on
        // MinCov select the item i that has not been instantiated that minimises freq(x U {i})
        // The value is first instancied to 0 (i.e. the item doesn't belong to the itemset)
        // Using this strategy guarantees that we first get the itemset with the maximal frequency
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                x
        ));
        while (solver.solve());
        System.out.println("List of skypatterns w.r.t. {freq(x),area(x),allconf(x)} for the dataset contextPasquier99:");
        for (Solution solution : maximizer.getParetoFront()) {
            int[] itemset = IntStream
                    .range(0, x.length)
                    .filter(i -> solution.getIntVal(x[i]) == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            System.out.println(Arrays.toString(itemset) + ", freq=" + solution.getIntVal(freq) + ", area=" +
                    solution.getIntVal(area) + ", aconf=" + solution.getIntVal(aconf));
        }

    }
}
