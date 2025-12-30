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

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.io.Pattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class PatternUtil {

    /**
     * Read a stream of patterns in .txt format
     * @param stream input stream of skypatterns
     * @return The list of patterns contained in the stream
     */
    public static List<Pattern> readPatternStream(InputStream stream) throws IOException {
        List<Pattern> skypatterns = new LinkedList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                if (line.startsWith("-")) break;
                String[] lineSplit = line.split(":");
                String[] itemString = lineSplit[0].split(" ");
                int[] itemNames = new int[itemString.length];
                for (int i = 0; i < itemString.length; i++) {
                    itemNames[i] = Integer.parseInt(itemString[i]);
                }
                int[] measures = new int[lineSplit.length - 1];
                for (int i = 0; i < lineSplit.length - 1; i++) {
                    String currentMeasure = lineSplit[i + 1].replaceAll("\\s", "");
                    measures[i] = Integer.parseInt(currentMeasure);
                }
                skypatterns.add(new Pattern(itemNames, measures));
            }
        }
        return skypatterns;
    }

    /**
     * Find closed pattern (w.r.t. {freq}) of p
     * @param p pattern
     * @param database database
     * @return the closed pattern associated to p
     */
    public static int[] findClosedPattern(Pattern p, TransactionalDatabase database) {
        BitSet cover = new BitSet(database.getNbTransactions());
        Map<Integer, Integer> itemIndexes = database.getItemsMap();
        cover.set(0, database.getNbTransactions());
        for (int i : p.getItems()) {
            cover.and(database.getVerticalRepresentation()[itemIndexes.get(i)]);
        }
        Set<Integer> closure = new TreeSet<>();
        for (int i = database.getNbClass(); i < database.getNbItems(); i++) {
            BitSet temp = (BitSet) cover.clone();
            temp.and(database.getVerticalRepresentation()[i]);
            if (temp.cardinality() == cover.cardinality()) {
                closure.add(database.getItems()[i]);
            }
        }
        return closure.stream().mapToInt(Integer::intValue).toArray();
    }
}
