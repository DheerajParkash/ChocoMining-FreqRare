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
import io.gitlab.chaver.mining.patterns.io.Pattern;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.gitlab.chaver.mining.patterns.util.PatternUtil.readPatternStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropCoverSizeTest {

    private final String resourcesPath = "src/test/resources/";

    private void testFindFrequentPatterns(String dataPath, Set<Pattern> expectedPatterns, int freqLB) throws IOException {
        Set<Pattern> filteredExpected = expectedPatterns.stream()
                .filter(p -> p.getMeasures()[0] >= freqLB)
                .collect(Collectors.toSet());
        Model model = new Model("frequent test");
        TransactionalDatabase database = new DatReader(dataPath, 0, true).read();
        IntVar freq = model.intVar("freq", freqLB, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        model.post(new Constraint("Cover Size", new PropCoverSize(database, freq, x)));
        List<Solution> sols = model.getSolver().findAllSolutions();
        assertEquals(filteredExpected.size(), sols.size());
        for (Solution sol : sols) {
            int[] itemSave = IntStream.range(0, x.length)
                    .filter(i -> sol.getIntVal(x[i]) == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            int freqSave = sol.getIntVal(freq);
            assertTrue(filteredExpected.contains(new Pattern(itemSave, new int[]{freqSave})));
        }
    }

    @Test
    public void testCoverSize() throws IOException {
        String dataPath = resourcesPath + "contextPasquier99/contextPasquier99.dat";
        String resPath = resourcesPath + "contextPasquier99/frequent.txt";
        Set<Pattern> expectedPatterns = new HashSet<>(readPatternStream(new FileInputStream(resPath)));
        for (int i = 2; i <= 4; i++) {
            testFindFrequentPatterns(dataPath, expectedPatterns, i);
        }
    }
}
