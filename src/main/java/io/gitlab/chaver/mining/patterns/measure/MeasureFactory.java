/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure;

import io.gitlab.chaver.mining.patterns.measure.attribute.*;
import io.gitlab.chaver.mining.patterns.measure.pattern.*;

public class MeasureFactory {

    public static PatternMeasure freq() {
        return new Freq();
    }

    public static PatternMeasure length() {
        return new Length();
    }

    public static PatternMeasure area() {
        return new Area();
    }

    public static PatternMeasure growthRate() {
        return new GrowthRate();
    }

    public static PatternMeasure freq1() {
        return new Freq1();
    }

    public static PatternMeasure freq2() {
        return new Freq2();
    }

    public static PatternMeasure minFreq() {
        return new MinFreq();
    }

    public static PatternMeasure maxFreq() { return new MaxFreq(); }

    public static PatternMeasure allConf() { return new AllConf(); }

    public static PatternMeasure anyConf() {
        return new AnyConf();
    }

    public static PatternMeasure freqNeg() {
        return new FreqNeg();
    }

    public static AttributeMeasure min(int num) {
        return new Min(num);
    }

    public static AttributeMeasure max(int num) {
        return new Max(num);
    }

    public static AttributeMeasure mean(int num) {
        return new Mean(num);
    }

    public static AttributeMeasure sum(int num) {
        return new Sum(num);
    }

    public static AttributeMeasure avg(int num) {
        return new Avg(num);
    }

}
