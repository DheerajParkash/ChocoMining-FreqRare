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

import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.util.MISCalculator;
import io.gitlab.chaver.mining.patterns.constraints.FreqRareConstraint;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class ExampleMISFreqRareTest {

    public static void main(String[] args) throws Exception{
        // 1. Read the transactional database from file.
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();

        // 2. Compute MIS values with beta = 0.5 and minimum support = 1.
        MISCalculator misCalculator = new MISCalculator(database, 0.6, 1);
        int[] misValues = misCalculator.computeMIS();
        System.out.println("MIS values:");
        misCalculator.printMIS();

        int[] itemsArray = database.getItems();
 
        // 3. Build the item covers map using the vertical representation.
        Map<Integer, BitSet> itemCovers = new HashMap<>();
        BitSet[] verticalRepresentation = database.getVerticalRepresentation();
        for (int i = 0; i < verticalRepresentation.length; i++) {
            itemCovers.put(i, verticalRepresentation[i]);
        }

        // 4. Create the Choco model and Boolean decision variables.
        Model model = new Model("MIS and FreqRare Test");
        int nbItems = database.getNbItems();
        BoolVar[] boolItems = model.boolVarArray("I", nbItems);
        // Convert to IntVar array for our search heuristic.
        IntVar[] intItems = Arrays.stream(boolItems).map(BoolVar::asIntVar).toArray(IntVar[]::new);
        
        // 5. Post the FreqRareConstraint.
        //    This constraint ensures that the itemset’s cover (frequency) is not lower than the minimum MIS value.
        FreqRareConstraint.post(boolItems, misValues, itemCovers, database.getNbTransactions());

        // 6. Set a custom search strategy using a variable selector based on the minimum MIS.
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(new MinMisSelector(intItems, misValues),
                                               new IntDomainMax(), intItems));

        // 7. Solve and print all solutions.
        boolean hasSolution = false;
        while (solver.solve()) {
            hasSolution = true;
            System.out.print("Solution: ");
            for (int i = 0; i < boolItems.length; i++) {
                if (boolItems[i].getValue() == 1) {
                    // Print the actual item identifier from the database.
                    System.out.print("I" + itemsArray[i] + " ");
                }
            }
            System.out.println();
        }
        if (!hasSolution) {
            System.out.println("No solutions found.");
        }
    }

    /**
     * A custom variable selector that chooses the next variable based on the smallest MIS value among the uninstantiated ones.
     */
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
}
