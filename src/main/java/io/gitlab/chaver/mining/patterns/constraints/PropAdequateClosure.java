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

import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.compute.IMeasureComputerFactory;
import io.gitlab.chaver.mining.patterns.measure.compute.MeasureComputer;
import lombok.Getter;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.util.ESat;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Given a database D and a set of preserving measures M', the AdequateClosure constraint ensures
 * that x+ is a closed pattern w.r.t. M'
 */
public abstract class PropAdequateClosure extends Propagator<BoolVar> {

    private final BoolVar[] items;
    private final TransactionalDatabase database;
    private @Getter final List<MeasureComputer> computers;
    private final int[] freeItems;
    private final IStateInt lastIndexFree;
    private final int[] absentItems; // items instanciated to 0
    private final IStateInt lastIndexAbs;
    private final int firstIndex;
    private int nFree;
    private int nAbs;

    public PropAdequateClosure(TransactionalDatabase database, List<Measure> measures, BoolVar[] items,
                               IMeasureComputerFactory measureComputerFactory) {
        super(items);
        this.items = items;
        this.database = database;
        this.computers = new LinkedList<>();
        for (Measure m : measures) {
            computers.add(measureComputerFactory.getMeasureComputer(m, database, getModel()));
        }
        this.freeItems = IntStream.range(0, database.getNbItems()).toArray();
        this.lastIndexFree = getModel().getEnvironment().makeInt(items.length);
        this.firstIndex = database.getNbClass();
        this.absentItems = freeItems.clone();
        this.lastIndexAbs = getModel().getEnvironment().makeInt(firstIndex);
    }

    private boolean isConstant(int i) {
        for (MeasureComputer computer : computers) {
            if (!computer.isConstant(i)) return false;
        }
        return true;
    }

    private boolean isConstant(int i, int j) {
        for (MeasureComputer computer : computers) {
            if (!computer.isConstant(i, j)) return false;
        }
        return true;
    }

    protected void rule3() throws ContradictionException {
        for (int i = nAbs - 1; i >= firstIndex; i--) {
            int idx = absentItems[i];
            for (int j = nFree - 1; j >= firstIndex; j--) {
                int idx2 = freeItems[j];
                if (isConstant(idx, idx2)) {
                    nFree = removeItem(j, nFree, idx2);
                    nAbs = addItem(nAbs, idx2);
                    items[idx2].setToFalse(this);
                }
            }
        }
    }

    public abstract void checkDC() throws ContradictionException;

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        nFree = lastIndexFree.get();
        nAbs = lastIndexAbs.get();
        // compute m(x+) for each measure m
        for (int i = nFree - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (items[idx].isInstantiated()) {
                nFree = removeItem(i, nFree, idx);
                if (items[idx].isInstantiatedTo(1)) {
                    computeMeasures(idx);
                }
                else {
                    nAbs = addItem(nAbs, idx);
                }
            }
        }
        // fails if exists absent item idx such that m(x+ U {idx}) = m(x+) for all measures
        for (int i = nAbs - 1; i >= firstIndex; i--) {
            int idx = absentItems[i];
            if (isConstant(idx)) fails();
        }
        // all items idx such that m(x U {idx}) = m(x) for all measures are added in the set of present items
        for (int i = nFree - 1; i >= firstIndex; i--) {
            int idx = freeItems[i];
            if (isConstant(idx)) {
                nFree = removeItem(i, nFree, idx);
                items[idx].setToTrue(this);
            }
        }
        checkDC();
        lastIndexFree.set(nFree);
        lastIndexAbs.set(nAbs);
    }

    private void computeMeasures(int idx) {
        for (MeasureComputer computer : computers) {
            computer.compute(idx);
        }
    }

    private int removeItem(int i, int nFree, int idx) {
        int lastFree = nFree - 1;
        freeItems[i] = freeItems[lastFree];
        freeItems[lastFree] = idx;
        return lastFree;
    }

    private int addItem(int nP, int idx) {
        absentItems[nP] = idx;
        return nP + 1;
    }

    @Override
    public ESat isEntailed() {
        return ESat.UNDEFINED;
    }
}

