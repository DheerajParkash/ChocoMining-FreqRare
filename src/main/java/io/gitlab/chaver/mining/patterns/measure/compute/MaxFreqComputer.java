/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;

public class MaxFreqComputer extends IntMeasureComputer {

    private final int[] itemFreq;

    public MaxFreqComputer(TransactionalDatabase database, Model model) {
        super(database, model);
        itemFreq = database.computeItemFreq();
    }

    @Override
    public int getInitValue() {
        return 0;
    }

    @Override
    public void compute(int i) {
        value.set(Math.max(value.get(), itemFreq[i]));
    }

    @Override
    public boolean isConstant(int i) {
        return itemFreq[i] <= value.get();
    }

    @Override
    public boolean isConstant(int i, int j) {
        return itemFreq[i] <= Math.max(value.get(), itemFreq[j]);
    }
}
