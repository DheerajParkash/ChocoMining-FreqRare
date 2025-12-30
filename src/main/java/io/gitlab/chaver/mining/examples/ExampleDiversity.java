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

import io.gitlab.chaver.mining.patterns.constraints.Overlap;
import io.gitlab.chaver.mining.patterns.constraints.factory.ConstraintFactory;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * Example of diverse pattern mining with a jmax threshold = 0.05 and theta = 0.01
 */
public class ExampleDiversity {

    public static void main(String[] args) throws Exception {
        TransactionalDatabase database = new DatReader("data/iris.dat").read();
        Model model = new Model("Diverse Itemset Mining");
        int theta = (int) Math.round(database.getNbTransactions() * 0.01d);
        IntVar freq = model.intVar("freq", theta, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        ConstraintFactory.coverSize(database, freq, x).post();
        ConstraintFactory.coverClosure(database, x).post();
        double jmax = 0.05;
        // Overlap is a global constraint that ensures that x is a diverse itemset
        // i.e. there exists no y in the history such that jaccard(x,y) > jmax
        // Each time a new itemset x is found, we add it to the history if it is a diverse itemset
        Overlap overlap = ConstraintFactory.overlap(database, x, jmax, theta);
        overlap.post();
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                x
        ));
        while (solver.solve());
        //solver.printStatistics();
        List<int[]> itemsets = overlap.getItemsetsHistory();
        List<BitSet> covers = overlap.getCoversHistory();
        System.out.println("List of diverse patterns for the dataset iris with a jmax threshold=0.05:");
        for (int i = 0; i < itemsets.size(); i++) {
            int[] itemset = Arrays.stream(itemsets.get(i))
                    .map(j -> database.getItems()[j])
                    .toArray();
            System.out.println(Arrays.toString(itemset) + ", cover=" + covers.get(i));
        }
    }
}
