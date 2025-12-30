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

import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class PropAdequateClosureTest {

    private final String resPath = "src/test/resources/";

    protected abstract PropAdequateClosure getAdequateClosure(TransactionalDatabase database, List<Measure> measures, BoolVar[] x);

    private void testFindClosedPatterns(String dataPath, int nbExpectedClosed, List<Measure> measures, int nbValMeasures)
            throws IOException {
        Model model = new Model("closed test");
        TransactionalDatabase database = new DatReader(dataPath, nbValMeasures, true).read();
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new PropCoverSize(database, freq, x)));
        model.post(new Constraint("Closed", getAdequateClosure(database, measures, x)));
        List<Solution> sols = model.getSolver().findAllSolutions();
        assertEquals(nbExpectedClosed, sols.size());
    }

    @Test
    public void testFindClosedSdm() throws IOException {
        testFindClosedPatterns(resPath + "sdm/sdm.dat", 17, Arrays.asList(freq(), min(0)), 1);
    }
}
