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


import java.util.BitSet;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.util.MISCalculator;

public class MISExample {

    public static void main(String[] args) {
        // Example transactional dataset
        int[] items = {0, 1, 2, 3}; // Item IDs
        int[][] transactions = {
            {0, 1},     // Transaction 0
            {0, 2},     // Transaction 1
            {1, 2, 3},  // Transaction 2
            {0, 3}      // Transaction 3
        };

        // Create a TransactionalDatabase object
        BitSet[] verticalRepresentation = createVerticalRepresentation(items, transactions);
        TransactionalDatabase database = new TransactionalDatabase(
            items, transactions, 0, verticalRepresentation, transactions.length
        );

        // Compute MIS values
        double beta = 0.5; // Example beta value
        int misMin = 1;    // Example MIS_min value
        MISCalculator misCalculator = new MISCalculator(database, beta, misMin);

        // Print MIS values
        misCalculator.printMIS();
    }

    /**
     * Helper method to create a vertical representation of the dataset.
     */
    private static BitSet[] createVerticalRepresentation(int[] items, int[][] transactions) {
        BitSet[] verticalRep = new BitSet[items.length];
        for (int i = 0; i < items.length; i++) {
            verticalRep[i] = new BitSet(transactions.length);
            for (int t = 0; t < transactions.length; t++) {
                if (contains(transactions[t], items[i])) {
                    verticalRep[i].set(t);
                }
            }
        }
        return verticalRep;
    }

    /**
     * Helper method to check if an array contains an item.
     */
    private static boolean contains(int[] transaction, int item) {
        for (int i : transaction) {
            if (i == item) return true;
        }
        return false;
    }
}