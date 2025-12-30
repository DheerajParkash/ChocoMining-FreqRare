/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints.factory;

import io.gitlab.chaver.mining.patterns.constraints.*;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.List;

/**
 * A Constraint Factory to instantiate different data-mining constraints
 */
public class ConstraintFactory {

    /**
     * Given a transactional database, an integer variable freq and an array of Boolean variables that represents
     * the itemset x, ensures that freq is equal to the frequency of x in the database.
     * @param database Transactional database
     * @param freq Integer variable that represents the frequency of the itemset in the database
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @return The CoverSize constraint
     */
    public static Constraint coverSize(TransactionalDatabase database, IntVar freq, BoolVar[] items) {
        return new Constraint("CoverSize", new PropCoverSize(database, freq, items));
    }

    /**
     * Given a transactional database and an array of Boolean variables that represents
     * the itemset x, ensures that x is closed w.r.t. the frequency, i.e. there exists no superset of x that has
     * the same frequency.
     * @param database Transactional database
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @return The CoverClosure constraint
     */
    public static Constraint coverClosure(TransactionalDatabase database, BoolVar[] items) {
        return new Constraint("CoverClosure", new PropCoverClosure(database, items));
    }

    /**
     * Given a transactional database, a list of measures M and an array of Boolean variables that represents
     * the itemset x, ensures that x is closed w.r.t. M, i.e. there exists no superset y of x such that for each
     * measure m in M we have m(x) = m(y).
     * Two versions of the propagator are available : DC (Domain Consistency) and WC (Weak Consistency).
     * It is recommended to use the WC version since it was proven to be more time efficient that the DC one.
     * @param database Transactional database
     * @param measures List of measures
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @param dc true if we want to use the DC version of the constraint, else the WC version is used
     * @return the AdequateClosure constraint
     */
    public static Constraint adequateClosure(TransactionalDatabase database, List<Measure> measures, BoolVar[] items, boolean dc) {
        if (dc) {
            return new Constraint("AdequateClosureDC", new PropAdequateClosureDC(database, measures, items));
        }
        return new Constraint("AdequateClosureWC", new PropAdequateClosureWC(database, measures, items));
    }

    /**
     * Given a transactional database, a threshold s and an array of Boolean variables that represents
     * the itemset x, ensures that each subset y of x is frequent, i.e. freq(y) &ge; s.
     * @param database Transactional database
     * @param s Frequency threshold
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @return the FrequentSubs constraint
     */
    public static Constraint frequentSubs(TransactionalDatabase database, int s, BoolVar[] items) {
        return new Constraint("FrequentSubs", new PropFrequentSubs(database, s, items));
    }

    /**
     * Given a transactional database, a threshold s and an array of Boolean variables that represents
     * the itemset x, ensures that each superset y of x is infrequent, i.e. freq(y) &lt; s.
     * @param database Transactional database
     * @param s Frequency threshold
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @return the InfrequentSupers constraint
     */
    public static Constraint infrequentSupers(TransactionalDatabase database, int s, BoolVar[] items) {
        return new Constraint("InfrequentSupers", new PropInfrequentSupers(database, s, items));
    }

    /**
     * Given a transactional database and an array of Boolean variables that represents
     * the itemset x, ensures that x is a generator, i.e. there exists no subset of x that has the same
     * frequency.
     * @param database Transactional database
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @return the Generator constraint
     */
    public static Constraint generator(TransactionalDatabase database, BoolVar[] items) {
        return new Constraint("Generator", new PropGenerator(database, items));
    }

    /**
     * Given a transactional database, an array of Boolean variables that represents
     * the itemset x, a diversity threshold jmax and a frequency threshold theta, ensures that x is a diverse itemset,
     * i.e. there exists no itemset y in the history H such that jaccard(x,y) &gt; jmax.
     * Each time a new itemset is found, it is added to the history H if it is a diverse itemset.
     * @param database Transactional database
     * @param items Array of Boolean variables where items[i] == 1 indicates that i belongs to the itemset x
     * @param jmax Diversity threshold
     * @param theta Frequency threshold
     * @return the Overlap constraint
     */
    public static Overlap overlap(TransactionalDatabase database, BoolVar[] items, double jmax, int theta) {
        return new Overlap(database, items, jmax, theta);
    }
}
