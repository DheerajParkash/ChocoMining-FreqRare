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
import io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables.MinCov;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


/**
 * Example of closed pattern mining (a closed pattern is an itemset which has no superset with the same frequency)
 */
public class ExampleClosedItemsetMining {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis(); // Start time tracking

        // Read the transactional database
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
        // System.out.println("reading succefull");

        // Create the Choco model
        Model model = new Model("Closed Itemset Mining");
        
        // Array of Boolean variables where x[i] == 1 represents the fact that i belongs to the itemset
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        
        // Integer variable that represents the frequency of x with the bounds [1, nbTransactions]
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        
        // Integer variable that represents the length of x with the bounds [1, nbItems]
        IntVar length = model.intVar("length", 1, database.getNbItems());
        
        // Ensures that length = sum(x)
        model.sum(x, "=", length).post();
        
        // Ensures that freq = frequency(x)
        ConstraintFactory.coverSize(database, freq, x).post();
        
        // Explicit Frequency constraint: freq(P) ≥ α
        int alpha = 3; 
        // int alpha = (int) (0.3 * database.getNbTransactions()); // 90% of transactions
        model.arithm(freq, ">=", alpha).post();


        // Explicit Frequency constraint: size(P) ≥ lb
        int lb = 2; 
        model.arithm(length, ">=", lb).post();


        // Category Constraint Integration
        int catSize = 2;
        int maxItem = Arrays.stream(database.getItems()).max().orElse(0);
        int nbCat = (maxItem / catSize) + 1;
      
        int[] itemToCategory = new int[maxItem + 1];
        Map<Integer, List<Integer>> categoryItemsMap = new HashMap<>();

        for (int item = 0; item <= maxItem; item++) {
            int category = item / catSize;
            itemToCategory[item] = category;
            categoryItemsMap.computeIfAbsent(category, k -> new LinkedList<>()).add(item);
        }
        
        // Remove categories that have fewer than `catSize` items
        categoryItemsMap.entrySet().removeIf(entry -> entry.getValue().size() < catSize);
      
        BoolVar[] catPresence = model.boolVarArray("catPresence", categoryItemsMap.size());
        int categoryIndex = 0;
        for (Map.Entry<Integer, List<Integer>> entry : categoryItemsMap.entrySet()) {
            List<BoolVar> itemsInCategory = new LinkedList<>();
            for (int i = 0; i < database.getNbItems(); i++) {
                if (itemToCategory[database.getItems()[i]] == entry.getKey()) {
                    itemsInCategory.add(x[i]);
                }
            }
            model.addClausesBoolOrArrayEqVar(itemsInCategory.toArray(new BoolVar[0]), catPresence[categoryIndex]);
            categoryIndex++;
        }
        int m =2 ;
        model.sum(catPresence, ">=", m).post();


        // Ensures that x is a closed itemset
        ConstraintFactory.coverClosure(database, x).post();
        Solver solver = model.getSolver();
        
        // Variable heuristic : select item i such that freq(x U i) is minimal
        // Value heuristic : instantiate it first to 0
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                x
        ));
        
        // Create a list to store all the closed itemsets
        List<Pattern> closedPatterns = new LinkedList<>();
        while (solver.solve()) {
            int[] itemset = IntStream.range(0, x.length)
                    .filter(i -> x[i].getValue() == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            // Add the closed itemset with its frequency to the list
            closedPatterns.add(new Pattern(itemset, new int[]{freq.getValue()}));
        }

        long endTime = System.currentTimeMillis(); // End time tracking
        long executionTime = endTime - startTime;

        System.out.println("List of closed itemsets for the dataset contextPasquier99 w.r.t. freq(x):");
        
        // Print all the closed itemsets with their frequency
        for (Pattern closed : closedPatterns) {
            System.out.println(Arrays.toString(closed.getItems()) +
                    ", freq=" + closed.getMeasures()[0]);
        }

        // Display the number of resulting patterns
        System.out.println("Total number of closed itemsets: " + closedPatterns.size());
        
        // Display the execution time
        System.out.println("Execution time: " + executionTime + " ms");

        System.out.println("\nItems in each category:");
        for (Map.Entry<Integer, List<Integer>> entry : categoryItemsMap.entrySet()) {
            System.out.println("Category " + entry.getKey() + ": " + entry.getValue());
        }
    }
}

// Total number of closed itemsets: 109166
// Execution time: 9172 ms

//  mvn exec:java -Dexec.mainClass="io.gitlab.chaver.mining.examples.ExampleClosedItemsetMining"
//  mvn clean compile exec:java -Dexec.mainClass="io.gitlab.chaver.mining.examples.ExampleClosedItemsetMining"
