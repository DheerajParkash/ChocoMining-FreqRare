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

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import io.gitlab.chaver.mining.patterns.constraints.FreqRareConstraint;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.util.MISCalculator;


public class TestMISAndFreqRareConstraint {

    public static void main(String[] args)throws Exception{
        // Step 1: Read the dataset
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();

        // Step 2: Calculate MIS values (β=0.5, MIS_min=1)
        double beta = 0.3;
        int misMin = 1;
        MISCalculator misCalculator = new MISCalculator(database, beta, misMin);
        int[] misValues = misCalculator.computeMIS();
        System.out.println("Calculated MIS values:");
        misCalculator.printMIS();

        // Step 3: Run FreqRare constraint-based mining
        System.out.println("=== Running FreqRare with MIS values ===");
        testFreqRare(database, misValues);
    }

    private static void testFreqRare(TransactionalDatabase database, int[] mis) {
        // Step 1: Precompute item covers
        Map<Integer, BitSet> itemCovers = new HashMap<>();
        int[][] transactions = database.getValues();
        int[] items = database.getItems();

        for (int item : items) {
            BitSet cover = new BitSet(transactions.length);
            for (int txn = 0; txn < transactions.length; txn++) {
                if (contains(transactions[txn], item)) cover.set(txn);
            }
            itemCovers.put(item, cover);
        }
        
        // Step 2: Create model and variables
        Model model = new Model("FreqRare Test");
        BoolVar[] itemsVars = model.boolVarArray("I", items.length);
        IntVar[] intItems = Arrays.stream(itemsVars).map(BoolVar::asIntVar).toArray(IntVar[]::new);

        // Step 3: Post FreqRare constraint
        FreqRareConstraint.post(itemsVars, mis, itemCovers, transactions.length);

        // Step 4: Configure solver with IntVar-compatible search
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(new MinMisSelector(intItems, mis), new IntDomainMax(), intItems));

        // Step 5: Solve and print solutions
        boolean hasSolution = false;
        while (solver.solve()) {
            hasSolution = true;
            System.out.print("Solution: ");
            for (int i = 0; i < itemsVars.length; i++) {
                if (itemsVars[i].getValue() == 1) System.out.print("I" + items[i] + " ");
            }
            System.out.println();
        }
        if (!hasSolution) {
            System.out.println("No solutions found");
        }
    }

    private static class MinMisSelector implements VariableSelector<IntVar> {
        private final IntVar[] items;
        private final int[] mis;

        public MinMisSelector(IntVar[] items, int[] mis) {
            this.items = items;
            this.mis = mis;
        }

        @Override
        public IntVar getVariable(IntVar[] variables) {
            IntVar next = null;
            int minMis = Integer.MAX_VALUE;
            for (int i = 0; i < items.length; i++) {
                if (!items[i].isInstantiated() && mis[i] < minMis) {
                    minMis = mis[i];
                    next = items[i];
                }
            }
            return next;
        }
    }

    private static boolean contains(int[] transaction, int item) {
        for (int i : transaction) if (i == item) return true;
        return false;
    }


    // public static void main(String[] args) throws Exception {
    //     // 1. Read the transactional database from file.
    //     TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();

    //     // 2. Calculate MIS values (using β = 0.5 and minimum support = 1)
    //     MISCalculator misCalculator = new MISCalculator(database, 0.3, 1);
    //     int[] misValues = misCalculator.computeMIS();
    //     System.out.println("Calculated MIS values:");
    //     misCalculator.printMIS();

    //     // 3. Build the item cover map.
    //     // The vertical representation from the database is used directly,
    //     // where each index corresponds to an item’s cover (i.e., the set of transactions where the item appears).
    //     Map<Integer, BitSet> itemCoverMap = new HashMap<>();
    //     BitSet[] verticalRepresentation = database.getVerticalRepresentation();
    //     for (int i = 0; i < database.getNbItems(); i++) {
    //         itemCoverMap.put(i, verticalRepresentation[i]);
    //     }

    //     // // 4. Set up the Choco model and decision variables.
    //     Model model = new Model("MIS and FreqRare Test");
    //     BoolVar[] items = model.boolVarArray("I", database.getNbItems());

    //     // // 5. Post the FreqRareConstraint (this also posts a non-emptiness constraint).
    //     FreqRareConstraint.post(items, misValues, itemCoverMap, database.getNbTransactions());

    //     // // 6. Solve the model and print all solutions.
    //     Solver solver = model.getSolver();
    //     boolean foundSolution = false;
    //     System.out.println("\nSolutions:");
    //     while (solver.solve()) {
    //         foundSolution = true;
    //         System.out.print("Solution: { ");
    //         for (int i = 0; i < items.length; i++) {
    //             if (items[i].getValue() == 1) {
    //                 // Print the item as defined in the database.
    //                 System.out.print("I" + database.getItems()[i] + " ");
    //             }
    //         }
    //         System.out.println("}");
    //     }
    //     if (!foundSolution) {
    //         System.out.println("No solution found.");
    //     }
    // }
}
