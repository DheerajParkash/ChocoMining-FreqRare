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
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeasureOperandTest {

    @Test
    public void testConvertArea() {
        assertEquals(new HashSet<>(Arrays.asList(freq())), area().maxConvert());
        assertEquals(new HashSet<>(Arrays.asList(length())), area().minConvert());
    }

    @Test
    public void testConvertFreqAreaGr() {
        List<Measure> mList = Arrays.asList(freq(), area(), growthRate());
        assertEquals(new HashSet<>(Arrays.asList(freq(), freq1())), MeasureOperand.maxConvert(mList));
        assertEquals(new HashSet<>(Arrays.asList(length(), freq2())), MeasureOperand.minConvert(mList));
    }

    @Test
    public void testConvertMean() {
        assertEquals(new HashSet<>(Arrays.asList(min(0))), mean(0).maxConvert());
        assertEquals(new HashSet<>(Arrays.asList(max(0))), mean(0).minConvert());
    }

    @Test
    public void testConvertAllConf() {
        assertEquals(new HashSet<>(Arrays.asList(freq(), maxFreq())), allConf().maxConvert());
        assertEquals(new HashSet<>(Arrays.asList()), allConf().minConvert());
    }

    @Test
    public void testConvertAnyConf() {
        assertEquals(new HashSet<>(Arrays.asList(freq())), anyConf().maxConvert());
        assertEquals(new HashSet<>(Arrays.asList(minFreq())), anyConf().minConvert());
    }

}
