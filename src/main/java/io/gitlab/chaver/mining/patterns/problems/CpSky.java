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

import io.gitlab.chaver.chocotools.problem.BuildModelException;
import io.gitlab.chaver.mining.patterns.measure.Measure;
import io.gitlab.chaver.mining.patterns.measure.attribute.AttributeMeasure;
import io.gitlab.chaver.mining.patterns.measure.attribute.Max;
import io.gitlab.chaver.mining.patterns.measure.attribute.Min;
import io.gitlab.chaver.mining.patterns.measure.pattern.Freq;
import io.gitlab.chaver.mining.patterns.measure.pattern.Freq1;
import io.gitlab.chaver.mining.patterns.measure.pattern.MaxFreq;
import io.gitlab.chaver.mining.patterns.util.CpSkyTransactionGetter;
import io.gitlab.chaver.mining.patterns.util.TransactionGetter;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import static io.gitlab.chaver.mining.patterns.measure.MeasureFactory.*;

@Command(name = "cpsky", mixinStandardHelpOptions = true, description = "CP+SKY implementation")
public class CpSky extends PatternProblem {

    private BoolVar[] transactions;
    private BoolVar[] transactions1;

    @Override
    protected Model createModel() {
        return new Model("CP+SKY", Settings.prod());
    }

    private int[][] row0() {
        int[][] row0 = new int[database.getNbTransactions()][database.getNbItems()];
        for (int i = 0; i < database.getNbTransactions(); i++) {
            for (int j = 0; j < database.getNbItems(); j++) {
                row0[i][j] = database.getVerticalRepresentation()[j].get(i) ? 0 : 1;
            }
        }
        return row0;
    }

    private void transactionVars() {
        transactions = model.boolVarArray("transactions", database.getNbTransactions());
        int[][] row0 = row0();
        for (int i = 0; i < database.getNbTransactions(); i++) {
            transactions[i] = model.scalar(items, row0[i], "=", 0).reify();
        }
    }

    @Override
    protected void freqVar() {
        transactionVars();
        String freqId = freq().getId();
        IntVar freq = model.intVar(freqId, freqMin, database.getNbTransactions());
        model.count(1, transactions, freq).post();
        measureVars.put(freqId, freq);
    }

    private void transactions1Var() {
        transactions1 = model.boolVarArray("transactions1", database.getNbTransactions());
        int[][] row0 = row0();
        for (int i = 0; i < database.getNbTransactions(); i++) {
            if (database.getVerticalRepresentation()[0].get(i)) {
                transactions1[i] = model.scalar(items, row0[i], "=", 0).reify();
            }
            else {
                model.arithm(transactions1[i], "=", 0).post();
            }
        }
    }

    @Override
    protected void freq1Var() {
        transactions1Var();
        String freq1Id = freq1().getId();
        IntVar freq1 = model.intVar(freq1Id, 0, database.getClassCount()[0]);
        model.count(1, transactions1, freq1).post();
        measureVars.put(freq1Id, freq1);
    }

    @Override
    protected TransactionGetter transactionGetter() {
        return new CpSkyTransactionGetter(transactions);
    }

    @Override
    protected void closedConstraint() throws BuildModelException {
        int nbClosedMeasures = closedMeasures.size();
        if (nbClosedMeasures == 0) return;
        BoolVar[][] isClosedFor = model.boolVarMatrix("isClosedFor", nbClosedMeasures, database.getNbItems());
        BoolVar[] closedExpression = model.boolVarArray("closedExpression", database.getNbItems());
        for (int i = 0; i < database.getNbClass(); i++) {
            for (int j = 0; j < nbClosedMeasures; j++) {
                model.arithm(isClosedFor[j][i], "=", 0).post();
            }
            closedExpression[i] = model.and(ArrayUtils.getColumn(isClosedFor, i)).reify();
            model.arithm(items[i], "=", closedExpression[i]).post();
        }
        int[] itemFreq = database.computeItemFreq();
        for (int i = database.getNbClass(); i < database.getNbItems(); i++) {
            int[] colFreq = new int[database.getNbTransactions()];
            int[] colFreq1 = new int[database.getNbTransactions()];
            for (int j = 0; j < database.getNbTransactions(); j++) {
                int colValue = database.getVerticalRepresentation()[i].get(j) ? 0 : 1;
                colFreq[j] = colValue;
                colFreq1[j] = database.getVerticalRepresentation()[0].get(j) ? colValue : 1;
            }
            for (int j = 0; j < nbClosedMeasures; j++) {
                Measure m = closedMeasures.get(j);
                // FREQUENT CLOSED
                if (m.getClass() == Freq.class) {
                    isClosedFor[j][i] = model.scalar(transactions, colFreq, "=", 0).reify();
                }
                // FREQUENT1 CLOSED
                else if (m.getClass() == Freq1.class) {
                    isClosedFor[j][i] = model.scalar(transactions1, colFreq1, "=", 0).reify();
                }
                // MIN CLOSED
                else if (m.getClass() == Min.class) {
                    int ind = ((AttributeMeasure)m).getNum();
                    IntVar min = measureVars.get(min(ind).getId());
                    isClosedFor[j][i] = model.arithm(min, "<=", database.getValues()[ind][i]).reify();
                }
                // MAX CLOSED
                else if (m.getClass() == Max.class) {
                    int ind = ((AttributeMeasure)m).getNum();
                    IntVar max = measureVars.get(max(ind).getId());
                    isClosedFor[j][i] = model.arithm(max, ">=", database.getValues()[ind][i]).reify();
                }
                else if (m.getClass() == MaxFreq.class) {
                    IntVar maxFreq = measureVars.get(maxFreq().getId());
                    isClosedFor[j][i] = model.arithm(maxFreq, ">=", itemFreq[i]).reify();
                }
                else {
                    throw new BuildModelException("This measure can't be closed : " + m);
                }
            }
            closedExpression[i] = model.and(ArrayUtils.getColumn(isClosedFor, i)).reify();
            model.arithm(items[i], "=", closedExpression[i]).post();
        }
    }

    public static void main(String[] args) {
        new CommandLine(new CpSky()).execute(args);
    }
}
