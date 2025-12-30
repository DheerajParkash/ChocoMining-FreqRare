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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import io.gitlab.chaver.mining.patterns.constraints.FreqRareConstraint;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class TestFreqRareConstraint {

    public static void main(String[] args) {
        System.out.println("=== Test Case 1: Valid Solutions ===");
        testFreqRare(new int[][]{
                    {0, 1}, 
                    {0, 2}, 
                    {1, 2, 3}, 
                    {0, 3}}, 
                    new int[]{2, 2, 1, 1});

        System.out.println("\n=== Test Case 2: No Solutions ===");
        testFreqRare(new int[][]{{0, 1}, {0, 2}, {1, 2}}, new int[]{3, 3, 3});

    }

    private static void testFreqRare(int[][] transactions, int[] mis) {
        

        Map<Integer, BitSet> itemCovers = new HashMap<>();
        for (int item = 0; item < mis.length; item++) {
            BitSet cover = new BitSet();
            for (int txn = 0; txn < transactions.length; txn++) {
                if (contains(transactions[txn], item)) cover.set(txn);
            }
            itemCovers.put(item, cover);
        }

        Model model = new Model("FreqRare Test");
        BoolVar[] items = model.boolVarArray("I", mis.length);
        IntVar[] intItems = Arrays.stream(items).map(BoolVar::asIntVar).toArray(IntVar[]::new);

        FreqRareConstraint.post(items, mis, itemCovers, transactions.length);

        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(new MinMisSelector(intItems, mis), new IntDomainMax(), intItems));

        boolean hasSolution = false;
        while (solver.solve()) {
            hasSolution = true;
            System.out.print("Solution: ");
            for (int i = 0; i < items.length; i++) {
                if (items[i].getValue() == 1) System.out.print("I" + i + " ");
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
            for (int i = 0; i < items.length; i++) { // Iterate by index
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
}