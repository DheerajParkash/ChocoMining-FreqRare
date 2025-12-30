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
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.Model;

public interface IMeasureComputerFactory {

    /**
     * Map a measure m to its computer
     * @param m measure
     * @param database database
     * @param model Model
     * @return the associated measure computer
     */
    MeasureComputer getMeasureComputer(Measure m, TransactionalDatabase database, Model model);
}
