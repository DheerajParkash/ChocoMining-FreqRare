/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.measure;

import io.gitlab.chaver.mining.rules.io.IRule;

/**
 * Classic rule measures
 */
public enum SimpleRuleMeasures implements RuleMeasure {

    sup {
        @Override
        public String getName() {
            return "support";
        }
        @Override
        public double compute(IRule rule, int nbTransactions) {
            return rule.getFreqZ();
        }
    },
    rsup {
        @Override
        public String getName() {
            return "relative support";
        }

        @Override
        public double compute(IRule rule, int nbTransactions) {
            return (double) rule.getFreqZ() / nbTransactions;
        }
    },
    conf {
        @Override
        public String getName() {
            return "confidence";
        }
        @Override
        public double compute(IRule rule, int nbTransactions) {
            return (double) rule.getFreqZ() / rule.getFreqX();
        }
    },
    lift {
        @Override
        public String getName() {
            return "lift";
        }
        @Override
        public double compute(IRule rule, int nbTransactions) {
            return conf.compute(rule, nbTransactions) * nbTransactions / rule.getFreqY();
        }
    }


}
