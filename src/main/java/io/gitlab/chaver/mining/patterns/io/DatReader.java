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
import java.util.*;

/**
 * Read file with number of items
 * ex of a database with two classes and three items:
 *  {1 3 4}
 *  {2 4 5}
 *  {1 3}
 */
public class DatReader extends DataReader {

    private int[] sortedItems;
    private Map<Integer, Integer> itemMap;

    public DatReader(String dataPath, int numberOfValueMeasures) {
        super(dataPath, numberOfValueMeasures);
    }

    public DatReader(String dataPath, int numberOfValueMeasures, boolean noClasses) {
        super(dataPath, numberOfValueMeasures, noClasses);
    }

    public DatReader(String dataPath) {
        super(dataPath);
    }

    private void loadItems() throws IOException {
        String line;
        Set<Integer> itemSet = new HashSet<>();
        nbTransactions = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataPath))) {
            while ((line = reader.readLine()) != null) {
                if (skipLine(line)) continue;
                String[] itemsLine = line.split(" ");
                for (int i = 0; i < itemsLine.length; i++) {
                    String item = itemsLine[i];
                    itemSet.add(Integer.valueOf(item));
                }
                nbTransactions++;
            }
            sortedItems = itemSet.stream().mapToInt(Integer::intValue).toArray();
            Arrays.sort(sortedItems);
            nbItems = sortedItems.length;
            itemMap = new HashMap<>();
            for (int i = 0; i < sortedItems.length; i++) {
                itemMap.put(sortedItems[i], i);
            }
        }
    }

    @Override
    public TransactionalDatabase read() throws IOException {
        loadItems();
        BitSet[] data = new BitSet[nbItems];
        for (int i = 0; i < nbItems; i++) {
            data[i] = new BitSet(nbTransactions);
        }
        int maxClass = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(dataPath))) {
            String line;
            int currentTransaction = 0;
            while ((line = reader.readLine()) != null) {
                if (skipLine(line)) continue;
                String[] itemsLine = line.split(" ");
                int class1 = itemMap.get(Integer.parseInt(itemsLine[0])) + 1;
                maxClass = Math.max(class1, maxClass);
                for (int i = 0; i < itemsLine.length; i++) {
                    int currentItem = itemMap.get(Integer.parseInt(itemsLine[i]));
                    data[currentItem].set(currentTransaction);
                }
                currentTransaction++;
            }
        }
        return new TransactionalDatabase(sortedItems, readValueFiles(), noClasses ? 0 : maxClass, data, nbTransactions);
    }
}
