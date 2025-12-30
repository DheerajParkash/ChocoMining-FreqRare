/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.search.loop.monitors;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.rules.io.AssociationRule;
import lombok.AllArgsConstructor;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.LinkedList;
import java.util.List;

/**
 * Class used for storing association rules with x as antecedent and y as consequent
 */
@AllArgsConstructor
public class ArMonitor implements IMonitorSolution {

    private TransactionalDatabase database;
    private BoolVar[] x;
    private BoolVar[] y;
    private IntVar freqX;
    private IntVar freqY;
    private IntVar freqZ;
    private final List<AssociationRule> associationRules = new LinkedList<>();

    private int[] getPattern(BoolVar[] p) {
        List<Integer> pattern = new LinkedList<>();
        for (int i = 0; i < p.length; i++) {
            if (p[i].isInstantiatedTo(1)) pattern.add(database.getItems()[i]);
        }
        return pattern.stream().mapToInt(i -> i).toArray();
    }

    @Override
    public void onSolution() {
        associationRules.add(
                new AssociationRule(
                        getPattern(x), getPattern(y), freqX.getValue(), freqY.getValue(), freqZ.getValue()
                )
        );
    }

    public List<AssociationRule> getAssociationRules() {
        return associationRules;
    }
}
