/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.pattern;

import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.operand.MeasureOperand;

import java.util.Set;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;
import static io.gitlab.chaver.mining.patterns.measure.operand.OperandFactory.*;

public class GrowthRate extends PatternMeasure {

    private final MeasureOperand op = mul(div(constant(), constant()), div(freq1(), freq2()));

    @Override
    public Set<Measure> maxConvert() {
        return op.maxConvert();
    }

    @Override
    public Set<Measure> minConvert() {
        return op.minConvert();
    }

    @Override
    public String getId() {
        return "grate";
    }
}
