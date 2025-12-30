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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Read a file with 0-1 data
 * ex of a database with two classes and three items :
 *  {1 0 1 0 1}
 *  {0 1 1 1 0}
 *  {1 0 0 1 1}
 */
public class BinReader extends DataReader {

    public BinReader(String dataPath, int numberOfValueMeasures) {
        super(dataPath, numberOfValueMeasures);
    }

    public BinReader(String dataPath, int numberOfValueMeasures, boolean noClasses) {
        super(dataPath, numberOfValueMeasures, noClasses);
    }

    private void loadDataStats() throws IOException {
        nbItems = 0;
        nbTransactions = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (skipLine(line)) continue;
                nbTransactions++;
                int currentNbItems = line.split(" ").length;
                if (nbItems == 0) {
                    nbItems = currentNbItems;
                }
                if (nbItems != currentNbItems) {
                    throw new RuntimeException("Item number is different in line " + nbTransactions);
                }
            }
        }
    }

    @Override
    public TransactionalDatabase read() throws IOException {
        loadDataStats();
        BitSet[] data = new BitSet[nbItems];
        for (int i = 0; i < nbItems; i++) {
            data[i] = new BitSet(nbTransactions);
        }
        int maxClass = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataPath))) {
            int currentTransaction = 0;
            String line;
            while ((line = reader.readLine()) != null) {
                if (skipLine(line)) continue;
                String[] lineItems = line.split(" ");
                int itemClass = -1;
                for (int i = 0; i < lineItems.length; i++) {
                    data[i].set(currentTransaction, lineItems[i].equals("1"));
                    if (itemClass == -1 && data[i].get(currentTransaction)) {
                        itemClass = i + 1;
                        maxClass = Math.max(maxClass, itemClass);
                    }
                }
                currentTransaction++;
            }
        }
        int[] items = IntStream.range(1, nbItems + 1).toArray();
        int[][] values = readValueFiles();
        return new TransactionalDatabase(items, values, noClasses ? 0 : maxClass, data, nbTransactions);
    }

}
