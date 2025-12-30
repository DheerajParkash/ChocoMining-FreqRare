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

public class MaxValComputer extends AttributeMeasureComputer {

    public MaxValComputer(TransactionalDatabase database, Model model, int num) {
        super(database, model, num);
    }

    @Override
    public int getInitValue() {
        return 0;
    }

    @Override
    public void compute(int i) {
        value.set(Math.max(getItemValue(i), value.get()));
    }

    @Override
    public boolean isConstant(int i) {
        return getItemValue(i) <= value.get();
    }

    @Override
    public boolean isConstant(int i, int j) {
        return getItemValue(i) <= Math.max(value.get(), getItemValue(j));
    }
}
