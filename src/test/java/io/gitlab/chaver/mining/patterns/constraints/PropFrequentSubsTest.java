/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.constraints;

import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PropFrequentSubsTest {

    /*@Test
    void test() throws Exception {
        Database database = new DatReader("src/test/resources/mushroom/mushroom.dat", 0, true)
                .readFiles();
        Model model = new Model("FrequentSubsTest");
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        model.post(new Constraint("FrequentSubs", new FrequentSubs(database, 4, x)));
        //model.post(new Constraint("Generator", new Generator(database, x)));
        Solver solver = model.getSolver();
        //solver.showStatisticsDuringResolution(2000);
        solver.findAllSolutions(() -> solver.getNodeCount() == 500000);
        solver.printStatistics();
        *//*while (solver.solve()) {
            System.out.println(Arrays.toString(x));
        }*//*
    }*/

    Model createModel(String dataPath, boolean mfi, int s) throws Exception {
        TransactionalDatabase database = new DatReader(dataPath, 0, true).read();
        Model model = new Model();
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        int freqLB = mfi ? s : 0;
        int freqUB = mfi ? database.getNbTransactions() : (s - 1);
        IntVar freq = model.intVar("freq", freqLB, freqUB);
        model.post(new Constraint("FrequentSubs", new PropFrequentSubs(database, s, x)));
        model.post(new Constraint("InfrequentSupers", new PropInfrequentSupers(database, s, x)));
        model.post(new Constraint("CoverSize", new PropCoverSize(database, freq, x)));
        if (mfi) {
            model.post(new Constraint("CoverClosure", new PropCoverClosure(database, x)));
        }
        else {
            model.post(new Constraint("Generator", new PropGenerator(database, x)));
        }
        int[] itemFreq = database.computeItemFreq();
        BoolVar[] xSorted = IntStream
                .range(0, database.getNbItems())
                .boxed()
                .sorted(Comparator.comparingInt(i -> itemFreq[i]))
                .map(i -> x[i])
                .toArray(BoolVar[]::new);
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                new InputOrder<>(model),
                new IntDomainMax(),
                xSorted
        ));
        return model;

    }

    private List<Integer> convertToPattern(BoolVar[] x) {
        return IntStream
                .range(0, x.length)
                .filter(i -> x[i].isInstantiatedTo(1))
                .boxed()
                .collect(Collectors.toList());
    }

    @Test
    void testMFI() throws Exception {
        TransactionalDatabase database = new DatReader("src/test/resources/mfi/ex.dat", 0, true)
                .read();
        Model model = new Model("MFI");
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        int s = 3;
        model.post(new Constraint("FrequentSubs", new PropFrequentSubs(database, s, x)));
        model.post(new Constraint("InfrequentSupers", new PropInfrequentSupers(database, s, x)));
        IntVar freq = model.intVar("freq", s, database.getNbTransactions());
        model.post(new Constraint("CoverSize", new PropCoverSize(database, freq, x)));
        Solver solver = model.getSolver();
        Set<List<Integer>> solutions = new HashSet<>();
        while (solver.solve()) {
            solutions.add(convertToPattern(x));
        }
        Set<List<Integer>> expectedSolutions = new HashSet<>(
                Arrays.asList(
                        Arrays.asList(0, 1, 4),
                        Arrays.asList(0, 2),
                        Arrays.asList(1, 2, 4)
                )
        );
        assertEquals(expectedSolutions, solutions);
    }

    @Test
    void testMII() throws Exception {
        TransactionalDatabase database = new DatReader("src/test/resources/mfi/ex.dat", 0, true)
                .read();
        Model model = new Model("MII");
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        int s = 3;
        model.post(new Constraint("FrequentSubs", new PropFrequentSubs(database, s, x)));
        model.post(new Constraint("InfrequentSupers", new PropInfrequentSupers(database, s, x)));
        IntVar freq = model.intVar("freq", 1, s - 1);
        model.post(new Constraint("CoverSize", new PropCoverSize(database, freq, x)));
        Solver solver = model.getSolver();
        Set<List<Integer>> solutions = new HashSet<>();
        while (solver.solve()) {
            solutions.add(convertToPattern(x));
        }
        Set<List<Integer>> expectedSolutions = new HashSet<>(
                Arrays.asList(
                        Arrays.asList(0, 1, 2),
                        Arrays.asList(0, 2, 4),
                        Arrays.asList(3)
                )
        );
        assertEquals(expectedSolutions, solutions);
    }

    @Test
    void testMII2() throws Exception {
        Model model = createModel("src/test/resources/mushroom/mushroom.dat", false, 813);
        Solver solver = model.getSolver();
        List<Solution> solutions = solver.findAllSolutions();
        // solver.printStatistics();
        assertEquals(2288, solutions.size());
    }

    @Test
    void testMFI2() throws Exception {
        Model model = createModel("src/test/resources/mushroom/mushroom.dat", true, 813);
        Solver solver = model.getSolver();
        List<Solution> solutions = solver.findAllSolutions();
        // solver.printStatistics();
        assertEquals(444, solutions.size());
    }

    /*@Test
    void testMII3() throws Exception {
        Database database = new DatReader("data/anneal.dat", 0, true)
                .readFiles();
        Model model = new Model("MII", Settings.prod());
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        int s = 100;
        model.post(new Constraint("FrequentSubs", new FrequentSubs(database, s, x)));
        model.post(new Constraint("InfrequentSupers", new InfrequentSupers(database, s, x)));
        IntVar freq = model.intVar("freq", 0, s - 1);
        model.post(new Constraint("CoverSize", new CoverSize(database, freq, x)));
        Solver solver = model.getSolver();
        solver.setSearch(Search.intVarSearch(
                new MinCov(model, database),
                new IntDomainMin(),
                x
        ));
        solver.limitTime("60s");
        List<Solution> solutions = solver.findAllSolutions();
        // solver.printStatistics();
        solver.printStatistics();
    }*/

}