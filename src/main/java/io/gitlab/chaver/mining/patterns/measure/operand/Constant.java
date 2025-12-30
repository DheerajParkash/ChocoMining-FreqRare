/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.operand;

import io.gitlab.chaver.mining.patterns.measure.Measure;

import java.util.HashSet;
import java.util.Set;

public class Constant implements MeasureOperand {

    @Override
    public Set<Measure> maxConvert() {
        return new HashSet<>();
    }

    @Override
    public Set<Measure> minConvert() {
        return new HashSet<>();
    }
}
