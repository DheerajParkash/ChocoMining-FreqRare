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

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;

public class BitSetFactory {

    public static BitSetFacade getBitSet(String type, TransactionalDatabase database, Model model) {
        if (type.equals(RSparseBitSetFacade.TYPE)) {
            return new RSparseBitSetFacade(database, model, database.getNbTransactions());
        }
        throw new RuntimeException("Incorrect BitSet type : " + type);
    }

    public static BitSetFacade getBitSet1(String type, TransactionalDatabase database, Model model) {
        if (type.equals(RSparseBitSetFacade.TYPE)) {
            return new RSparseBitSetFacade(database, model, database.getDatasetAsLongArray()[0]);
        }
        throw new RuntimeException("Incorrect BitSet type : " + type);
    }
}
