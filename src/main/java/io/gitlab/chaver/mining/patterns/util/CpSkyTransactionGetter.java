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

import lombok.AllArgsConstructor;
import org.chocosolver.solver.variables.BoolVar;

import java.util.stream.IntStream;

@AllArgsConstructor
public class CpSkyTransactionGetter implements TransactionGetter {

    private BoolVar[] transactions;

    @Override
    public int[] getTransactions() {
        return IntStream.range(0, transactions.length).filter(i -> transactions[i].getValue() == 1).toArray();
    }
}
