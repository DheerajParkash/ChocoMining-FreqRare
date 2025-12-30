/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.io;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.rules.measure.RuleMeasure;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

/**
 * Represents an association rule with antecedent x and consequent y, such that z = x U y
 */
@AllArgsConstructor
public class AssociationRule implements IRule {

    /**
     * Items in the antecedent of the rule
     */
    private @Getter int[] x;

    /**
     * Items in the consequence of the rule
     */
    private @Getter int[] y;

    /**
     * Frequency of x (antecedent of the rule)
     */
    private @Getter int freqX;

    /**
     * Frequency of y (consequence of the rule)
     */
    private @Getter int freqY;

    /**
     * Frequency of z (union between the antecedent and the consequence of the rule)
     */
    private @Getter int freqZ;

    private String convertToString(int[] pattern) {
        if (pattern.length == 0) return "{}";
        StringBuilder str = new StringBuilder("{").append(pattern[0]);
        for (int i = 1; i < pattern.length; i++) {
            str.append(", ").append(pattern[i]);
        }
        str.append("}");
        return str.toString();
    }

    private String convertToString(int[] pattern, String[] labels, TransactionalDatabase database) {
        if (pattern.length == 0) return "{}";
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        StringBuilder str = new StringBuilder("{").append(labels[itemsMap.get(pattern[0])]);
        for (int i = 1; i < pattern.length; i++) {
            str.append(", ").append(labels[itemsMap.get(pattern[i])]);
        }
        str.append("}");
        return str.toString();
    }

    @Override
    public String toString() {
        return "AssociationRule{" +
                "x=" + convertToString(x) +
                ", y=" + convertToString(y) +
                ", freqX=" + freqX +
                ", freqY=" + freqY +
                ", freqZ=" + freqZ +
                '}';
    }

    private String computeMeasures(List<RuleMeasure> measures, int nbTransactions, DecimalFormat measureFormat) {
        StringBuilder str = new StringBuilder("{");
        boolean begin = true;
        for (RuleMeasure measure : measures) {
            if (!begin) {
                str.append(", ");
            }
            begin = false;
            str.append(measure.getName()).append("=").append(measureFormat.format(measure.compute(this, nbTransactions)));
        }
        str.append("}");
        return str.toString();
    }

    /**
     * Convert rule to String
     * @param database database to consider
     * @param labels label of each item
     * @return corresponding string
     */
    public String toString(TransactionalDatabase database, String[] labels, List<RuleMeasure> measures, DecimalFormat measureFormat) {
        int nbTransactions = database.getNbTransactions();
        if (labels == null) {
            return convertToString(x) + " => " + convertToString(y) + ", measures=" +
                    computeMeasures(measures, nbTransactions, measureFormat);
        }
        return convertToString(x, labels, database) + " => " + convertToString(y, labels, database) +
                ", measures=" + computeMeasures(measures, nbTransactions, measureFormat);
    }
}
