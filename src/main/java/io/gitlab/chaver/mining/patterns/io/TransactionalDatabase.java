/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.io;

import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a transactional database where each transaction is a set of items
 */
public class TransactionalDatabase {

    /** Name of items */
    private int[] items;
    /** Value of items */
    private int[][] values;
    /** Number of classes */
    private int nbClass;
    /** Vertical representation of the dataset */
    private BitSet[] verticalRepresentation;
    /** Number of transactions in the dataset */
    private int nbTransactions;
    /** Map each item to its position in items array */
    private Map<Integer, Integer> itemsMap;

    public TransactionalDatabase(int[] items, int[][] values, int nbClass, BitSet[] verticalRepresentation, int nbTransactions) {
        this.items = items;
        this.values = values;
        this.nbClass = nbClass;
        this.verticalRepresentation = verticalRepresentation;
        this.nbTransactions = nbTransactions;
    }

    public BitSet[] getVerticalRepresentation() {
        return verticalRepresentation;
    }

    public int[][] getValues() {
        return values;
    }

    public int[] getItems() {
        return items;
    }

    public int getNbClass() {
        return nbClass;
    }

    public int getNbTransactions() {
        return nbTransactions;
    }

    public int getNbItems() {
        return items.length;
    }

    public long[][] getDatasetAsLongArray() {
        long[][] dataset = new long[items.length][];
        for (int i = 0; i < verticalRepresentation.length; i++) {
            dataset[i] = verticalRepresentation[i].toLongArray();
        }
        return dataset;
    }

    public double getDensity() {
        double nbSetTransactions = Arrays.stream(verticalRepresentation).mapToInt(BitSet::cardinality).sum();
        return nbSetTransactions / (items.length * nbTransactions);
    }

    /**
     * Associate each item to its index in the array
     * @return map
     */
    public Map<Integer, Integer> getItemsMap() {
        if (itemsMap == null) {
            itemsMap = new HashMap<>();
            for (int i = 0; i < items.length; i++) {
                itemsMap.put(items[i], i);
            }
        }
        return itemsMap;
    }

    /**
     * Compute the frequency of each item
     * @return an array with the frequency of each item
     */
    public int[] computeItemFreq() {
        return Arrays.stream(verticalRepresentation).mapToInt(BitSet::cardinality).toArray();
    }

    /**
     * Get class count
     * @return an array of size 2 : the first index represents the number of transactions of the first class and
     * the second one represents the number of transactions that are not in the first class
     */
    public int[] getClassCount() {
        int d1 = verticalRepresentation[0].cardinality();
        int d2 = nbTransactions - d1;
        return new int[]{d1, d2};
    }
}
