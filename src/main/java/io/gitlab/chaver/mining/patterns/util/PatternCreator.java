/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.chocotools.util.Creator;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import lombok.AllArgsConstructor;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@AllArgsConstructor
public class PatternCreator implements Creator<Pattern> {

    private TransactionalDatabase database;
    private BoolVar[] items;
    private List<String> allMeasuresId;
    private Map<String, IntVar> measureVars;
    private TransactionGetter transactionGetter;

    @Override
    public Pattern create() {
        int[] itemSave = IntStream
                .range(0, items.length)
                .filter(i -> items[i].isInstantiatedTo(1))
                .map(i -> database.getItems()[i])
                .toArray();
        int[] measureSave = new int[allMeasuresId.size()];
        for (int i = 0; i < allMeasuresId.size(); i++) {
            measureSave[i] = measureVars.get(allMeasuresId.get(i)).getValue();
        }
        Pattern p = new Pattern(itemSave, measureSave);
        if (transactionGetter != null) p.setTransactions(transactionGetter.getTransactions());
        return p;
    }
}
