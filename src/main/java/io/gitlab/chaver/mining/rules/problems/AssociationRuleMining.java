/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.rules.problems;

import io.gitlab.chaver.chocotools.io.JsonResultReader;
import io.gitlab.chaver.chocotools.io.ProblemResult;
import io.gitlab.chaver.chocotools.io.ProblemResultReader;
import io.gitlab.chaver.chocotools.problem.ChocoProblem;
import io.gitlab.chaver.chocotools.problem.SetUpException;
import io.gitlab.chaver.mining.patterns.constraints.PropCoverClosure;
import io.gitlab.chaver.mining.patterns.constraints.PropCoverSize;
import io.gitlab.chaver.mining.patterns.constraints.PropGenerator;
import io.gitlab.chaver.mining.patterns.io.DatReader;
import io.gitlab.chaver.mining.patterns.io.TransactionalDatabase;
import io.gitlab.chaver.mining.patterns.io.Pattern;
import io.gitlab.chaver.mining.patterns.io.PatternProblemProperties;
import io.gitlab.chaver.mining.rules.io.ArMeasuresView;
import io.gitlab.chaver.mining.rules.io.AssociationRule;
import io.gitlab.chaver.mining.rules.io.RuleType;
import io.gitlab.chaver.mining.rules.measure.RuleMeasure;
import io.gitlab.chaver.mining.rules.search.loop.monitors.ArMonitor;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.expression.discrete.relational.ReExpression;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.InputOrder;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static io.gitlab.chaver.mining.patterns.util.PatternUtil.findClosedPattern;
import static io.gitlab.chaver.mining.rules.measure.SimpleRuleMeasures.*;
import static org.chocosolver.solver.search.strategy.Search.intVarSearch;

@Command(name = "arm", description = "Association rule mining", mixinStandardHelpOptions = true)
public class AssociationRuleMining extends ChocoProblem<AssociationRule, ArMeasuresView> {

    @Option(names = {"-d", "--data"}, required = true, description = "Datafile to use")
    private String dataPath;
    //@Option(names = {"--nc"}, description = "Ignore classes of transactions")
    private boolean noClasses = true;
    @Option(names = {"--rt"}, description = "Rule type : ${COMPLETION-CANDIDATES} (default : ${DEFAULT-VALUE})")
    private RuleType ruleType = RuleType.ar;
    @Option(names = "--fmin", description = "Min frequency of the rule (absolute value)")
    private int minFreq;
    @Option(names = "--rfmin", description = "Min frequency of the rule (relative value)")
    private double relativeMinFreq;
    @Option(names = "--cmin", description = "Min confidence of the rule")
    private double minConf;
    @Option(names = "--sky", description = "Skypatterns file (impose constraint)")
    private String skyPath;
    @Option(names = "--0a", description = "Items to exclude in the antecedent (path of a file where each line " +
            "represents an item to exclude)")
    private String zeroItemsAntecedentPath;
    @Option(names = "--0c", description = "Items to exclude in the consequent (path of a file where each line " +
            "represents an item to exclude)")
    private String zeroItemsConsequentPath;
    @Option(names = "--or", description = "Items to include in the antecedent or the consequent (path of a file where" +
            "each line represents an item to include")
    private String orItemsPath;
    @Option(names = "--lab", description = "File path with the label of items (each line corresponds to one item)")
    private String labelsPath;
    private String[] labels;
    private List<RuleMeasure> measures = Arrays.asList(sup, rsup, conf, lift);
    private DecimalFormat measureFormat = new DecimalFormat("0.000");

    private TransactionalDatabase database;
    private ArMonitor arMonitor;
    private Map<Set<Integer>, Set<Integer>> closedPatterns;
    private int[] zeroItemsAntecedent = new int[0];
    private int[] zeroItemsConsequent = new int[0];
    private int[] orItems = new int[0];

    private int[] readItems(String path) throws IOException {
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        return Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8).stream().mapToInt(s -> itemsMap.get(Integer.parseInt(s))).toArray();
    }

    @Override
    public void parseArgs() throws SetUpException {
        try {
            database = new DatReader(dataPath, 0, noClasses).read();
            if (relativeMinFreq != 0 && minFreq != 0) {
                throw new SetUpException("--fmin and --rfmin are mutually exclusive (specify only one)");
            }
            if (relativeMinFreq != 0) {
                minFreq = (int) Math.round(relativeMinFreq * database.getNbTransactions());
            }
            if ((minFreq == 0 || minConf == 0) && (skyPath == null)) {
                throw new SetUpException("You should precise (--fmin and --cmin) or --sky");
            }
            if (skyPath != null) {
                closedPatterns = getClosedPatterns();
            }
            if (zeroItemsAntecedentPath != null) {
                zeroItemsAntecedent = readItems(zeroItemsAntecedentPath);
            }
            if (zeroItemsConsequentPath != null) {
                zeroItemsConsequent = readItems(zeroItemsConsequentPath);
            }
            if (orItemsPath != null) {
                orItems = readItems(orItemsPath);
            }
            if (labelsPath != null) {
                labels = Files.readAllLines(Paths.get(labelsPath), StandardCharsets.UTF_8).toArray(new String[0]);
            }
        }
        catch (IOException e) {
            throw new SetUpException(e.getMessage());
        }
    }

    /**
     * If skypattern constraint, associate each skypattern to its closure
     * @return a map which associates each skypattern to its closure
     * @throws IOException if the skypattern file doesn't exist
     */
    private Map<Set<Integer>, Set<Integer>> getClosedPatterns() throws IOException {
        if (skyPath == null) return new HashMap<>();
        Map<Set<Integer>, Set<Integer>> closedPatterns = new HashMap<>();
        ProblemResultReader<Pattern, PatternProblemProperties> reader = new JsonResultReader<>(skyPath);
        ProblemResult<Pattern, PatternProblemProperties> result = reader.readResult(Pattern[].class,
                PatternProblemProperties.class);
        for (Pattern p : result.getSolutions()) {
            Set<Integer> x = Arrays.stream(p.getItems()).boxed().collect(Collectors.toSet());
            Set<Integer> y = Arrays.stream(findClosedPattern(p, database)).boxed().collect(Collectors.toSet());
            closedPatterns.put(x, y);
        }
        return closedPatterns;
    }

    private Set<Integer> patternsUnion(Set<Set<Integer>> patterns) {
        Set<Integer> union = new HashSet<>();
        patterns.forEach(union::addAll);
        return union;
    }

    private BoolVar[] skypatternConstraint(BoolVar[] y, BoolVar[] z) {
        if (closedPatterns == null) return new BoolVar[0];
        Set<Integer> patternsUnion = patternsUnion(new HashSet<>(closedPatterns.values()));
        BoolVar[] skyVar = new BoolVar[closedPatterns.size()];
        Map<Integer, Integer> itemsMap = database.getItemsMap();
        for (int i = 0; i < database.getNbItems(); i++) {
            if (!patternsUnion.contains(database.getItems()[i])) {
                model.arithm(z[i], "=", 0).post();
            }
        }
        int i = 0;
        //System.out.println(closedPatterns);
        for (Map.Entry<Set<Integer>, Set<Integer>> entry : closedPatterns.entrySet()) {
            ReExpression sky = null;
            Set<Integer> skypattern = entry.getKey();
            Set<Integer> closedPattern = entry.getValue();
            Set<Integer> itemsOnlyInClosure = new HashSet<>(closedPattern);
            itemsOnlyInClosure.removeAll(skypattern);
            for (int item : patternsUnion) {
                ReExpression temp;
                int idx = itemsMap.get(item);
                if (!closedPattern.contains(item)) {
                    temp = z[idx].eq(0);
                }
                else if (itemsOnlyInClosure.contains(item)) {
                    temp = y[idx].eq(1);
                }
                else {
                    temp = z[idx].eq(1);
                }
                sky = sky == null ? temp : sky.and(temp);
            }
            skyVar[i] = sky.boolVar();
            i++;
        }
        model.sum(skyVar, ">=", 1).post();
        return skyVar;
    }

    private void zeroItemsConstraint(BoolVar[] items, int[] zeroItems) {
        Arrays.stream(zeroItems).forEach(i -> items[i].eq(0).post());
    }

    private void orItemsConstraint(BoolVar[] z) {
        if (orItems.length == 0) return;
        BoolVar[] orItemVars = Arrays.stream(orItems).mapToObj(i -> z[i]).toArray(BoolVar[]::new);
        model.or(orItemVars).post();
    }

    @Override
    public void buildModel() {
        BoolVar[] x = model.boolVarArray("x", database.getNbItems());
        BoolVar[] y = model.boolVarArray("y", database.getNbItems());
        zeroItemsConstraint(x, zeroItemsAntecedent);
        zeroItemsConstraint(y, zeroItemsConsequent);
        BoolVar[] z = model.boolVarArray("z", database.getNbItems());
        for (int i = 0; i < database.getNbItems(); i++) {
            model.arithm(x[i], "+", y[i], "<=", 1).post();
            model.addClausesBoolOrEqVar(x[i], y[i], z[i]);
        }
        orItemsConstraint(z);
        model.addClausesBoolOrArrayEqualTrue(x);
        model.addClausesBoolOrArrayEqualTrue(y);
        IntVar freqZ = model.intVar("freqZ", minFreq, database.getNbTransactions());
        new Constraint("frequent Z", new PropCoverSize(database, freqZ, z)).post();
        IntVar freqX = model.intVar("freqX", minFreq, database.getNbTransactions());
        new Constraint("frequent X", new PropCoverSize(database, freqX, x)).post();
        if (minConf > 0) freqZ.mul(10000).ge(freqX.mul((int) Math.round(minConf * 10000))).post();
        IntVar freqY = model.intVar("freqY", minFreq, database.getNbTransactions());
        new Constraint("frequent Y", new PropCoverSize(database, freqY, y)).post();
        if (ruleType.equals(RuleType.mnr)) {
            new Constraint("generator x", new PropGenerator(database, x))
                    .post();
            new Constraint("closed z", new PropCoverClosure(database, z)).post();
        }
        BoolVar[] skyVars = skypatternConstraint(y, z);
        BoolVar[] heuristicVars = ArrayUtils.append(skyVars, x, y, z);
        model.getSolver().setSearch(intVarSearch(
                new InputOrder<>(model),
                new IntDomainMin(),
                heuristicVars
        ));
        arMonitor = new ArMonitor(database, x, y, freqX, freqY, freqZ);
        model.getSolver().plugMonitor(arMonitor);
    }

    @Override
    protected Model createModel() {
        return new Model("AR mining", Settings.prod());
    }

    @Override
    public List<AssociationRule> getSolutions() {
        return arMonitor.getAssociationRules();
    }

    @Override
    public ArMeasuresView getProperties() {
        ArMeasuresView measures = new ArMeasuresView(solver.getMeasures());
        measures.setNbTransactions(database.getNbTransactions());
        return measures;
    }

    @Override
    protected void printSolutions() {
        getSolutions().forEach(s -> System.out.println(s.toString(database, labels, measures, measureFormat)));
    }

    public static void main(String[] args) throws Exception {
        new CommandLine(new AssociationRuleMining()).execute(args);
    }

}
