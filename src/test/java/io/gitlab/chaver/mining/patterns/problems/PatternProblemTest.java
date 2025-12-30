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

import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.util.MeasureListConverter;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static io.gitlab.chaver.mining.patterns.util.PatternUtil.readPatternStream;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class PatternProblemTest {

    private final String resourcesPath = "src/test/resources/";

    // Datasets
    protected final String acd = "acd";
    protected final String ex1 = "ex1";
    protected final String ex2 = "ex2";
    protected final String glass = "glass";
    protected final String iris = "iris";
    protected final String zoo = "zoo";
    protected final String noBack = "no-back";
    protected final String mushroom = "mushroom";
    protected final String max = "max";

    // Expected results
    // Closed patterns relatively to...
    protected final String closed_f = "closed_f"; // {freq}
    protected final String closed_fm = "closed_fm"; // {freq, min}
    protected final String closed_fM = "closed_fM"; // {freq, max}
    // Skypatterns relatively to...
    protected final String sky_fm = "sky_fm"; // {freq, min}
    protected final String sky_fa = "sky_fa"; // {freq, area}
    protected final String sky_fg = "sky_fg"; // {freq, growthRate}
    protected final String sky_fan = "sky_fan"; // {freq, area, mean}
    protected final String sky_fagnM = "sky_fagnM"; // {freq, area, gr, mean, max}
    protected final String sky_fgnM = "sky_fgnM"; // {freq, gr, mean, max}

    // Pattern measures
    protected final String freqList = "f";
    protected final String freqAreaList = "fa";
    protected final String freqGrList = "fg";
    protected final String freqAreaGrList = "fag";

    // Attribute measures
    protected final String minList = "m0";
    protected final String meanList = "n0";
    protected final String noAttributeMeasuresList = "";
    protected final String meanMaxList = "n0M1";
    protected final String maxList = "M0";

    public abstract PatternProblem getProblem();

    private List<Pattern> readPatterns(String patternPath, List<String> measuresId) throws IOException {
        return readPatternStream(new FileInputStream(patternPath));
    }

    private void testEqualsPatterns(List<Pattern> p1, List<Pattern> p2) {
        assertEquals(new HashSet<>(p1), new HashSet<>(p2));
    }

    private String[] getArgs(String datasetName, String patternMeasures, String attributeMeasures, boolean hasClasses,
                             boolean wc, String patternType) {
        String dataPath = resourcesPath + datasetName + "/" + datasetName + ".dat";
        String measureCmd = patternType.equals("closed") ? "--clom" : "--skym";
        List<String> args = new LinkedList<>(Arrays.asList("-d", dataPath, measureCmd, patternMeasures + attributeMeasures));
        if (! hasClasses) {
            args.add("--nc");
        }
        if (wc) {
            args.add("--wc");
        }
        if (patternType.equals("closed")) {
            args.addAll(Arrays.asList("--lmin", "0"));
        }
        return args.stream().toArray(String[]::new);
    }

    private List<String> convertMeasureToString(String patternMeasures, String attributeMeasures) throws Exception {
        return new MeasureListConverter()
                .convert(patternMeasures + attributeMeasures)
                .stream()
                .map(Measure::getId)
                .collect(Collectors.toList());
    }

    protected void testClosedPatterns(String datasetName, String expectedResultsPath, String patternMeasures,
                                    String attributeMeasures, boolean hasClasses, boolean wc) throws Exception {
        List<String> measures = convertMeasureToString(patternMeasures, attributeMeasures);
        List<Pattern> patterns = readPatterns(resourcesPath + datasetName + "/" + expectedResultsPath + ".txt", measures);
        PatternProblem problem = getProblem();
        String[] args = getArgs(datasetName, patternMeasures, attributeMeasures, hasClasses, wc, "closed");
        new CommandLine(problem).execute(args);
        testEqualsPatterns(patterns, problem.getSolutions());
    }

    protected void testSkyPatterns(String datasetName, String expectedResultsPath, String patternMeasures,
                                 String attributeMeasures, boolean hasClasses, boolean wc) throws Exception {
        List<String> measures = convertMeasureToString(patternMeasures, attributeMeasures);
        List<Pattern> patterns = readPatterns(resourcesPath + datasetName + "/" + expectedResultsPath + ".txt", measures);
        PatternProblem problem = getProblem();
        String[] args = getArgs(datasetName, patternMeasures, attributeMeasures, hasClasses, wc, "sky");
        new CommandLine(problem).execute(args);
        testEqualsPatterns(patterns, problem.getSolutions());
    }

    @Test
    public void testClosedPatternAcd() throws Exception {
        testClosedPatterns(acd, closed_fm, freqList, minList, true, false);
    }

    @Test
    public void testClosedPatternEx1() throws Exception {
        testClosedPatterns(ex1, closed_fm, freqList, minList, true, false);
    }

    @Test
    public void testClosedPatternsEx2() throws Exception {
        testClosedPatterns(ex2, closed_f, freqList, noAttributeMeasuresList, true, false);
        testClosedPatterns(ex2, closed_fm, freqList, minList, true, false);
    }

    @Test
    public void testSkyPatternAcd() throws Exception {
        testSkyPatterns(acd, sky_fm, freqList, minList, true, false);
    }

    @Test
    public void testSkyPatternEx1() throws Exception {
        testSkyPatterns(ex1, sky_fm, freqList, minList, true, false);
    }

    @Test
    public void testSkyPatternsIris() throws Exception {
        testSkyPatterns(iris, sky_fa, freqAreaList, noAttributeMeasuresList, true, false);
        testSkyPatterns(iris, sky_fg, freqGrList, noAttributeMeasuresList, true, false);
        testSkyPatterns(iris, sky_fan, freqAreaList, meanList, true, false);
        testSkyPatterns(iris, sky_fagnM, freqAreaGrList, meanMaxList, true, false);
    }

    @Test
    public void testSkyPatternsZoo() throws Exception {
        testSkyPatterns(zoo, sky_fa, freqAreaList, noAttributeMeasuresList, true, false);
        testSkyPatterns(zoo, sky_fg, freqGrList, noAttributeMeasuresList, true, false);
        testSkyPatterns(zoo, sky_fan, freqAreaList, meanList, true, false);
        testSkyPatterns(zoo, sky_fagnM, freqAreaGrList, meanMaxList, true, false);
    }

    @Test
    public void testNoBack() throws Exception {
        testClosedPatterns(noBack, closed_f, freqList, noAttributeMeasuresList, false, false);
    }

    @Test
    public void testClosedPatternsMax() throws Exception {
        testClosedPatterns(max, closed_fM, freqList, maxList, true, false);
    }
}
