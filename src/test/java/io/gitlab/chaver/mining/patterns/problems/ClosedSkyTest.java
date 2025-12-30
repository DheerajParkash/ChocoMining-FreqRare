/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.problems;

import org.junit.jupiter.api.Test;

public class ClosedSkyTest extends PatternProblemTest {

    @Override
    public PatternProblem getProblem() {
        return new ClosedSky();
    }

    @Test
    public void testClosedPatternAcdWC() throws Exception {
        testClosedPatterns(acd, closed_fm, freqList, minList, true, true);
    }

    @Test
    public void testClosedPatternEx1WC() throws Exception {
        testClosedPatterns(ex1, closed_fm, freqList, minList, true, true);
    }

    @Test
    public void testClosedPatternsEx2WC() throws Exception {
        testClosedPatterns(ex2, closed_f, freqList, noAttributeMeasuresList, true, true);
        testClosedPatterns(ex2, closed_fm, freqList, minList, true, true);
    }

    @Test
    public void testSkyPatternAcdWC() throws Exception {
        testSkyPatterns(acd, sky_fm, freqList, minList, true, true);
    }

    @Test
    public void testSkyPatternEx1WC() throws Exception {
        testSkyPatterns(ex1, sky_fm, freqList, minList, true, true);
    }

    @Test
    public void testSkyPatternsIrisWC() throws Exception {
        testSkyPatterns(iris, sky_fa, freqAreaList, noAttributeMeasuresList, true, true);
        testSkyPatterns(iris, sky_fg, freqGrList, noAttributeMeasuresList, true, true);
        testSkyPatterns(iris, sky_fan, freqAreaList, meanList, true, true);
        testSkyPatterns(iris, sky_fagnM, freqAreaGrList, meanMaxList, true, true);
    }

    @Test
    public void testSkyPatternsZooWC() throws Exception {
        testSkyPatterns(zoo, sky_fa, freqAreaList, noAttributeMeasuresList, true, true);
        testSkyPatterns(zoo, sky_fg, freqGrList, noAttributeMeasuresList, true, true);
        testSkyPatterns(zoo, sky_fan, freqAreaList, meanList, true, true);
        testSkyPatterns(zoo, sky_fagnM, freqAreaGrList, meanMaxList, true, true);
    }

    @Test
    public void testNoBackWC() throws Exception {
        testClosedPatterns(noBack, closed_f, freqList, noAttributeMeasuresList, false, true);
    }

    @Test
    public void testClosedPatternsMaxWC() throws Exception {
        testClosedPatterns(max, closed_fM, freqList, maxList, true, true);
    }
}
