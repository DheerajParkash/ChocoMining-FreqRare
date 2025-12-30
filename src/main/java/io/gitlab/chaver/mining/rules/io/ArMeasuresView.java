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

import io.gitlab.chaver.chocotools.io.MeasuresView;
import lombok.Getter;
import lombok.Setter;
import org.chocosolver.solver.search.measure.Measures;

public class ArMeasuresView extends MeasuresView {

    /**
     * Number of transactions of the database (useful for computing measures like lift)
     */
    protected @Getter @Setter int nbTransactions;

    public ArMeasuresView(Measures measures) {
        super(measures);
    }
}
