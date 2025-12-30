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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A pattern is a set of items
 */
@EqualsAndHashCode(of = {"items", "measures"})
@ToString(exclude = "transactions")
public class Pattern {

    /** Items that belong to the pattern */
    private @Getter int[] items;
    /** Value of the measures of the pattern */
    private @Getter int[] measures;
    /** Transactions that contain the pattern */
    private @Getter @Setter int[] transactions;

    public Pattern(int[] items, int[] measures) {
        this.items = items;
        this.measures = measures;
    }

    /**
     * Check if the pattern is dominated by p
     * @param p pattern
     * @param lastIdx last index of the measure
     * @return true if the pattern is dominated by p for measures from index 0 to lastIdx (excluded)
     */
    public boolean isDominatedBy(Pattern p, int lastIdx) {
        boolean isWorse = false;
        for (int i = 0; i < lastIdx; i++) {
            if (p.measures[i] < measures[i]) return false;
            if (p.measures[i] > measures[i]) isWorse = true;
        }
        return isWorse;
    }

    /**
     * Convert pattern to String with the id of each measure
     * @param measuresId id of the measures
     * @param labels label of each item
     * @param database database to consider
     * @return the string representation
     */
    public String toString(List<String> measuresId, String[] labels, TransactionalDatabase database) {
        String[] itemLabels = Arrays.stream(items).mapToObj(Integer::toString).toArray(String[]::new);
        if (labels != null) {
            Map<Integer, Integer> itemMap = database.getItemsMap();
            itemLabels = Arrays.stream(items).mapToObj(i -> labels[itemMap.get(i)]).toArray(String[]::new);
        }
        StringBuilder str = new StringBuilder("Pattern(items=").append(Arrays.toString(itemLabels)).append(", measures={");
        for (int i = 0; i < measuresId.size(); i++) {
            str.append(measuresId.get(i)).append("=").append(measures[i]).append(i == measuresId.size() - 1 ? "" : ", ");
        }
        str.append("})");
        return str.toString();
    }
}
