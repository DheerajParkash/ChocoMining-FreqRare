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

public class OperandFactory {

    public static MeasureOperand add(MeasureOperand o1, MeasureOperand o2) {
        return new Add(o1, o2);
    }

    public static MeasureOperand mul(MeasureOperand o1, MeasureOperand o2) {
        return new Mul(o1, o2);
    }

    public static MeasureOperand div(MeasureOperand o1, MeasureOperand o2) {
        return new Div(o1, o2);
    }

    public static MeasureOperand sub(MeasureOperand o1, MeasureOperand o2) {
        return new Sub(o1, o2);
    }

    public static MeasureOperand constant() {
        return new Constant();
    }
}
