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

import java.util.Set;

public class Sub extends BinaryOperand {

    public Sub(MeasureOperand o1, MeasureOperand o2) {
        super(o1, o2);
    }

    @Override
    public Set<Measure> maxConvert() {
        Set<Measure> convertResult = getO1().maxConvert();
        convertResult.addAll(getO2().minConvert());
        return convertResult;
    }

    @Override
    public Set<Measure> minConvert() {
        Set<Measure> convertResult = getO1().minConvert();
        convertResult.addAll(getO2().maxConvert());
        return convertResult;
    }
}
