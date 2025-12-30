/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */

package io.gitlab.chaver.mining.patterns.util;

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import io.gitlab.chaver.mining.patterns.constraints.factory.ConstraintFactory;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;

public class MISCalculator {

    /**
     * Calculate MIS values for all items using the formula:
     * MIS[i] = max(β * frequency(i), MIS_min)
     * 
     * @param database Transactional dataset
     * @param beta Parameter β ∈ [0,1]
     * @param MIS_min Minimum allowed support
     * @return Array of MIS values for each item
     */
    private final TransactionalDatabase database;
    private final double beta;
    private final int misMin;

    public MISCalculator(TransactionalDatabase database, double beta, int misMin) {
        this.database = database;
        this.beta = beta;
        this.misMin = misMin;
    }


    public int[] computeMIS() {
        int[] itemFrequencies = database.computeItemFreq(); // Get frequencies of items
        int[] misValues = new int[itemFrequencies.length]; // Array to store MIS values

        for (int i = 0; i < itemFrequencies.length; i++) {
            // Compute MIS for item i using the formula: MIS(i) = max(beta * freq(i), MIS_min)
            misValues[i] = (int) Math.ceil(Math.max(beta * itemFrequencies[i], misMin));
        }

        return misValues;
    }

    /**
     * Prints the MIS values for all items.
     */
    public void printMIS() {
        int[] misValues = computeMIS();
        int[] items = database.getItems();

        System.out.println("Item\tFrequency\tMIS");
        for (int i = 0; i < items.length; i++) {
            int frequency = database.computeItemFreq()[i];
            System.out.printf("%d\t%d\t\t%d%n", items[i], frequency, misValues[i]);
        }
    }
    

}