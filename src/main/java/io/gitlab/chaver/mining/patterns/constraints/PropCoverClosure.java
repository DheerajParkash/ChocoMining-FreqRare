/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.variables.BoolVar;

import java.util.Arrays;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.freq;


/**
 * Given a set of boolean variables x, ensures that x is a closed pattern w.r.t. {freq}
 * Fore more information, see Schaus et al. - CoverSize : A global constraint for frequency-based itemset mining
 */
public class PropCoverClosure extends PropAdequateClosureWC {

    public PropCoverClosure(TransactionalDatabase database, BoolVar[] items) {
        super(database, Arrays.asList(freq()), items);
    }
}
