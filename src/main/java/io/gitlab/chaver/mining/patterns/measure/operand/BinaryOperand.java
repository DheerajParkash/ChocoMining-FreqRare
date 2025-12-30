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

public abstract class BinaryOperand implements MeasureOperand {

    private MeasureOperand o1;
    private MeasureOperand o2;

    public BinaryOperand(MeasureOperand o1, MeasureOperand o2) {
        this.o1 = o1;
        this.o2 = o2;
    }

    public MeasureOperand getO1() {
        return o1;
    }

    public MeasureOperand getO2() {
        return o2;
    }
}
