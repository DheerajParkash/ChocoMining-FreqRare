/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.problems;

import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AssociationRuleMiningTest {

    private final String resourcesPath = "src/test/resources/";

    // Datasets
    private final String zoo = "zoo";
    private final String vote = "vote";

    private void testFindMnr(String dataset, double rfmin, double conf, int nbAssociationRules) throws IOException {
        String dataPath = resourcesPath + dataset +"/" + dataset + ".basenum";
        AssociationRuleMining arm = new AssociationRuleMining();
        String[] args = new String[]{"-d", dataPath, "--rfmin", String.valueOf(rfmin), "--cmin", String.valueOf(conf),
                "--rt", "mnr"};
        new CommandLine(arm).execute(args);
        assertEquals(nbAssociationRules, arm.getSolutions().size());
    }

    private void testFindMnrSkypattern(String dataset, String extension, int nbAssociationRules) throws IOException {
        String dataPath = resourcesPath + dataset +"/" + dataset + extension;
        TransactionalDatabase database = new DatReader(dataPath, 0, true)
                .read();
        Map<String, String> constraints = new HashMap<>();
        constraints.put("sky", resourcesPath + dataset + "/sky_fat.json");
        AssociationRuleMining arm = new AssociationRuleMining();
        String[] args = new String[]{"-d", dataPath, "--sky", resourcesPath + dataset + "/sky_fat.json", "--rt", "mnr"};
        new CommandLine(arm).execute(args);
        assertEquals(nbAssociationRules, arm.getSolutions().size());
    }

    @Test
    public void testFindMnrZoo() throws IOException {
        testFindMnr(zoo, 0.80, 0.90, 1);
        testFindMnr(zoo, 0.50, 0.90, 176);
        testFindMnr(zoo, 0.30, 0.90, 2260);
    }

    @Test
    public void testFindMnrVote() throws IOException {
        testFindMnr(vote, 0.35, 0.90, 271);
        testFindMnr(vote, 0.20, 0.90, 44562);
    }

    @Test
    public void testFindMnrSkyVote() throws IOException {
        testFindMnrSkypattern(vote, ".basenum", 1790);
    }

    @Test
    public void testFindMnrSkyMushroom() throws IOException {
        testFindMnrSkypattern("mushroom", ".dat", 24);
    }
}
