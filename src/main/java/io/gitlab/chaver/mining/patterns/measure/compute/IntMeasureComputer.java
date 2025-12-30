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
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;

public abstract class IntMeasureComputer extends MeasureComputer {

    protected IStateInt value;

    public IntMeasureComputer(TransactionalDatabase database, Model model) {
        super(database);
        this.value = model.getEnvironment().makeInt(getInitValue());
    }

    public abstract int getInitValue();
}
