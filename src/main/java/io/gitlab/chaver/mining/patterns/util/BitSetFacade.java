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

import java.util.BitSet;

public interface BitSetFacade {

    boolean isEmpty();
    int cardinality();
    int maskCardinality();
    void and(int i);
    int andCount(int i);
    void andMask(int i);
    void resetMask();
    boolean isSubsetOf(int i);
    boolean maskIsSubsetOf(int i);
    BitSet getWords();
}
