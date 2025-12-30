/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.measure.compute;

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.attribute.Max;
import io.gitlab.chaver.mining.patterns.measure.attribute.Mean;
import io.gitlab.chaver.mining.patterns.measure.attribute.Min;
import io.gitlab.chaver.mining.patterns.measure.pattern.Freq;
import io.gitlab.chaver.mining.patterns.measure.pattern.Freq1;
import io.gitlab.chaver.mining.patterns.measure.pattern.MaxFreq;
import org.chocosolver.solver.Model;

public class MeasureComputerFactory implements IMeasureComputerFactory {

    @Override
    public MeasureComputer getMeasureComputer(Measure m, TransactionalDatabase database, Model model) {
        if (m.getClass() == Freq.class) {
            return new FreqComputer(database, model);
        }
        if (m.getClass() == Freq1.class) {
            return new Freq1Computer(database, model);
        }
        if (m.getClass() == Min.class) {
            return new MinValComputer(database, model, ((Min) m).getNum());
        }
        if (m.getClass() == Max.class) {
            return new MaxValComputer(database, model, ((Max) m).getNum());
        }
        if (m.getClass() == Mean.class) {
            return new MeanValComputer(database, model, ((Mean) m).getNum());
        }
        if (m.getClass() == MaxFreq.class) {
            return new MaxFreqComputer(database, model);
        }
        throw new RuntimeException("MeasureComputer doesn't exists for this measure : " + m.getId());
    }
}
