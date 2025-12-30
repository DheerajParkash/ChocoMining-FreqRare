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

public class MeanValComputer extends MeasureComputer {

    private MinValComputer minValComputer;
    private MaxValComputer maxValComputer;

    public MeanValComputer(TransactionalDatabase database, Model model, int num) {
        super(database);
        this.minValComputer = new MinValComputer(database, model, num);
        this.maxValComputer = new MaxValComputer(database, model, num);
    }

    @Override
    public void compute(int i) {
        minValComputer.compute(i);
        maxValComputer.compute(i);
    }

    @Override
    public boolean isConstant(int i) {
        return minValComputer.isConstant(i) && maxValComputer.isConstant(i);
    }

    @Override
    public boolean isConstant(int i, int j) {
        return minValComputer.isConstant(i, j) && maxValComputer.isConstant(i, j);
    }
}
