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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Frequence of items of class 2, freq(x, T2)
 */
public class Freq2 extends PatternMeasure {

    @Override
    public Set<Measure> maxConvert() {
        return new HashSet<>(Collections.singletonList(this));
    }

    @Override
    public Set<Measure> minConvert() {
        return new HashSet<>();
    }

    @Override
    public String getId() {
        return "freq2";
    }
}
