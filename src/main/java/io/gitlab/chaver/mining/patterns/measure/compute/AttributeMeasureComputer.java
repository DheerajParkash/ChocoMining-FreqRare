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

public abstract class AttributeMeasureComputer extends IntMeasureComputer {

    protected int num;

    public AttributeMeasureComputer(TransactionalDatabase database, Model model, int num) {
        super(database, model);
        this.num = num;
    }

    protected int getItemValue(int i) {
        return database.getValues()[num][i];
    }
}
