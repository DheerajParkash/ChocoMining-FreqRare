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

import io.gitlab.chaver.mining.patterns.constraints.PropCoverSize;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ClosedSkyTransactionGetter implements TransactionGetter {

    private PropCoverSize coverSize;

    @Override
    public int[] getTransactions() {
        return coverSize.getCover().stream().toArray();
    }
}
