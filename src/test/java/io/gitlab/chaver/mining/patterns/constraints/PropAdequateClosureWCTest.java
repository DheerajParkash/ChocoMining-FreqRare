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
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.variables.BoolVar;

import java.util.List;

public class PropAdequateClosureWCTest extends PropAdequateClosureTest {

    @Override
    protected PropAdequateClosure getAdequateClosure(TransactionalDatabase database, List<Measure> measures, BoolVar[] x) {
        return new PropAdequateClosureWC(database, measures, x);
    }
}
