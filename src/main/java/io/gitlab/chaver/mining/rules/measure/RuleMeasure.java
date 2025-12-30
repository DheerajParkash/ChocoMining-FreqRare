/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.measure;

import io.gitlab.chaver.mining.rules.io.IRule;

/**
 * Measure to compute for an association rule
 */
public interface RuleMeasure {

    /**
     * Name of the measure
     * @return the name of the measure
     */
    String getName();

    /**
     * Compute the measure
     * @param rule given association rule
     * @param nbTransactions number of transactions in the database
     * @return the value of the measure for this association rule
     */
    double compute(IRule rule, int nbTransactions);
}
