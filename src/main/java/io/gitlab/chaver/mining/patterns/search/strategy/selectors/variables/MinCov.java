/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.search.strategy.selectors.variables;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.util.BitSetFacade;
import io.gitlab.chaver.mining.patterns.util.RSparseBitSetFacade;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.selectors.variables.VariableSelector;
import org.chocosolver.solver.variables.IntVar;

/**
 * Select the item i such that freq(x+ U {i}) is minimal
 */
public class MinCov implements VariableSelector<IntVar> {

    private final BitSetFacade cover;

    public MinCov(Model model, TransactionalDatabase database) {
        this.cover = new RSparseBitSetFacade(database, model, database.getNbTransactions());
    }

    @Override
    public IntVar getVariable(IntVar[] variables) {
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].asBoolVar().isInstantiatedTo(1)) {
                cover.and(i);
            }
        }
        int minCov = cover.cardinality() + 1;
        int minCovId = -1;
        for (int i = 0; i < variables.length; i++) {
            if (variables[i].isInstantiated()) continue;
            int coverIntCardinality = cover.andCount(i);
            if (coverIntCardinality < minCov) {
                minCov = coverIntCardinality;
                minCovId = i;
            }
        }
        return minCovId > -1 ? variables[minCovId] : null;
    }
}
