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

import io.gitlab.chaver.mining.patterns.constraints.PropFreqRare;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import java.util.*;

public class TestFreqRare {

    // Sample transactions dataset (each transaction contains a set of item indexes)
    static int[][] transactions = {
        {0, 1, 2},  // Transaction 1
        {0, 2},     // Transaction 2
        {1, 2},     // Transaction 3
        {0, 1},     // Transaction 4
        {2},        // Transaction 5
    };

    // Extended transactions dataset with more transactions and items
    static int[][] transactions2 = {
        {0, 1, 2, 4},  // Transaction 1
        {0, 2, 3},     // Transaction 2
        {1, 3, 4},     // Transaction 3
        {0, 1, 2},     // Transaction 4
        {2, 3},        // Transaction 5
        {1, 4},        // Transaction 6
        {0, 2, 3},     // Transaction 7
        {1, 2, 3},     // Transaction 8
        {0, 4},        // Transaction 9
        {3, 4},        // Transaction 10
        {0, 3, 4},     // Transaction 11
        {1, 3},        // Transaction 12
        {0, 1},        // Transaction 13
        {2, 4},        // Transaction 14
        {0, 2, 4},     // Transaction 15
    };

    // Test 1: Basic case with valid frequent itemset
    public static void testValidFreqItemset() {
        System.out.println("Test 1: Valid Frequent Itemset");
        Model model = new Model("FreqRare Test");
        BoolVar[] vars = model.boolVarArray("X", 3);
        int[] minSupports = {2, 2, 1};  // Minimum supports for items {0, 1, 2}

        model.post(new Constraint("FreqRare", new PropFreqRare(vars, minSupports, transactions)));

        Solver solver = model.getSolver();
        while (solver.solve()) {
            System.out.println(Arrays.toString(Arrays.stream(vars).mapToInt(v -> v.getValue()).toArray()));
        }
    }

    // Test 2: Edge case - No valid itemset (fails immediately)
    public static void testNoValidItemset() {
        System.out.println("\nTest 2: No Valid Itemset (Failure Case)");
        Model model = new Model("FreqRare No Valid");
        BoolVar[] vars = model.boolVarArray("X", 3);
        int[] minSupports = {6, 6, 6};  // More than the number of transactions available

        model.post(new Constraint("FreqRare", new PropFreqRare(vars, minSupports, transactions)));

        Solver solver = model.getSolver();
        if (!solver.solve()) {
            System.out.println("No valid solutions found (expected).");
        }
    }

    // Test 3: Larger dataset with more frequent and rare itemsets
    public static void testLargeDataset() {
        System.out.println("Test 3: Larger Dataset with Frequent and Rare Itemsets");
        Model model = new Model("FreqRare Test Large");
        BoolVar[] vars = model.boolVarArray("X", 5);  // 5 items in total
        int[] minSupports = {4, 3, 5, 2, 4};  // Minimum supports for items {0, 1, 2, 3, 4}


        model.post(new Constraint("FreqRare", new PropFreqRare(vars, minSupports, transactions2)));

        Solver solver = model.getSolver();
        while (solver.solve()) {
            System.out.println(Arrays.toString(Arrays.stream(vars).mapToInt(v -> v.getValue()).toArray()));
        }
    }

    public static void main(String[] args) {
        testValidFreqItemset();
        testNoValidItemset();
        testLargeDataset();
    }
}

//  mvn clean compile exec:java -Dexec.mainClass="io.gitlab.chaver.mining.examples.TestFreqRare"
// set JAVA_HOME=C:\Program Files\Java\jdk-19
// set PATH=%JAVA_HOME%\bin;%PATH%
