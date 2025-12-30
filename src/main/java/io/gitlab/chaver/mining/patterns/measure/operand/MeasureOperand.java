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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a measure operand (see Ugarte et al. - Skypattern mining: From pattern condensed representations to dynamic constraint satisfaction problems)
 */
public interface MeasureOperand {
    /**
     * Given a measure operand m, compute a set of measures M' such that m is maximally M'-skylineable
     * @return the set of measures M' such that m is maximally M'-skylineable
     */
    Set<Measure> maxConvert();
    /**
     * Given a measure operand m, compute a set of measures M' such that m is minimally M'-skylineable
     * @return the set of measures M' such that m is minimally M'-skylineable
     */
    Set<Measure> minConvert();

    /**
     * Given a set of measures M, compute a set of measures M' such that M is maximally M'-skylineable
     * @return the set of measures M' such that M is maximally M'-skylineable
     */
    static Set<Measure> maxConvert(Collection<Measure> mSet) {
        Set<Measure> skyM = new HashSet<>();
        mSet.forEach(m -> skyM.addAll(m.maxConvert()));
        return skyM;
    }

    /**
     * Given a set of measures M, compute a set of measures M' such that M is minimally M'-skylineable
     * @return the set of measures M' such that M is minimally M'-skylineable
     */
    static Set<Measure> minConvert(Collection<Measure> mSet) {
        Set<Measure> skyM = new HashSet<>();
        mSet.forEach(m -> skyM.addAll(m.minConvert()));
        return skyM;
    }
}
