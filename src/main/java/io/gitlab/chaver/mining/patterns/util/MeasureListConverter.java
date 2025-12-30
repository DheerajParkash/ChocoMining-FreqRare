/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.util;

import io.gitlab.chaver.chocotools.problem.SetUpException;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import picocli.CommandLine.ITypeConverter;

import java.util.LinkedList;
import java.util.List;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;

public class MeasureListConverter implements ITypeConverter<List<Measure>> {

    @Override
    public List<Measure> convert(String arg) throws Exception {
        String[] argSplit = arg.split("");
        List<Measure> measures = new LinkedList<>();
        int i = 0;
        while (i < argSplit.length) {
            if (argSplit[i].equals("f")) measures.add(freq());
            else if (argSplit[i].equals("z")) measures.add(freqNeg());
            else if (argSplit[i].equals("1")) measures.add(freq1());
            else if (argSplit[i].equals("a")) measures.add(area());
            else if (argSplit[i].equals("g")) measures.add(growthRate());
            else if (argSplit[i].equals("c")) measures.add(allConf());
            else if (argSplit[i].equals("l")) measures.add(length());
            else if (argSplit[i].equals("F")) measures.add(maxFreq());
            else if (argSplit[i].equals("n")) measures.add(mean(Integer.parseInt(argSplit[++i])));
            else if (argSplit[i].equals("m")) measures.add(min(Integer.parseInt(argSplit[++i])));
            else if (argSplit[i].equals("M")) measures.add(max(Integer.parseInt(argSplit[++i])));
            else throw new SetUpException("This measure doesn't exist : " + argSplit[++i]);
            i++;
        }
        return measures;
    }
}
