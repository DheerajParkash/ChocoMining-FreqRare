/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.examples;

import io.gitlab.chaver.mining.patterns.constraints.factory.ConstraintFactory;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Example of generator mining (a generator is an itemset which has no subset with the same frequency)
 */
public class ExampleGeneratorMining {

    public static void main(String[] args) throws Exception {
        TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
        Model model = new Model("Generator Mining");
        IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
        IntVar length = model.intVar("length", 1, database.getNbItems());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.sum(x, "=", length).post();
        ConstraintFactory.coverSize(database, freq, x).post();
        // Ensures that x is a generator
        ConstraintFactory.generator(database, x).post();
        List<Pattern> generators = new LinkedList<>();
        while (model.getSolver().solve()) {
            int[] itemset = IntStream.range(0, x.length)
                    .filter(i -> x[i].getValue() == 1)
                    .map(i -> database.getItems()[i])
                    .toArray();
            generators.add(new Pattern(itemset, new int[]{freq.getValue()}));
        }
        System.out.println("List of generators for the dataset contextPasquier99:");
        for (Pattern generator : generators) {
            System.out.println(Arrays.toString(generator.getItems()) + ", freq=" + generator.getMeasures()[0]);
        }
    }
}
