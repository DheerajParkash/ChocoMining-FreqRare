# Choco-Mining: A Java library for Itemset Mining with Choco Solver

Choco-Mining is a Java library for solving itemset mining problems that is based on [Choco-solver](https://github.com/chocoteam/choco-solver). Choco-solver is an open-source Java library designed for Constraint Programming (CP). One of the key benefits of using CP in pattern mining is the flexibility it provides to add custom constraints to the problem without requiring modifications to the underlying system. [Seq2Pat](https://github.com/fidelity/seq2pat) [KadiogluWHH23] is another example of library relevant for declarative (sequential) pattern mining based on CP.

## Illustrative example

In itemset mining, we are working on *Transactional databases*. Consider the example of the transactional database in the file `data/contextPasquier99.dat`:

```
1 3 4
2 3 5
1 2 3 5
2 5
1 2 3 5
```

In this example, we have 5 items denoted by `I = {1,2,3,4,5}`. Each row of the file represents a transaction (a transaction is a subset of `I`). The first transaction contains the items `{1,3,4}`, the second the items `{2,3,5}`, etc... An itemset (or pattern) is a subset of `I`. The frequency of an itemset is the number of transactions in which it appears in the database. For example, the itemset `{1,3,5}` has a frequency of `2` since it appears in the 3rd and 5th rows of the database. 

Using this database, we want to extract all the closed itemsets w.r.t. the frequency that have a frequency `>= 1`. We say that an itemset `x` is closed w.r.t. the frequency if it has no superset `y` with the same frequency. For example, `{3}` is a closed itemset w.r.t. the frequency but `{1}` is not since `freq({1}) = freq({1,3}) = 3`.

Using our library, we can model the closed itemset mining task in the [following way](https://gitlab.com/chaver/choco-mining/-/blob/master/src/main/java/io/gitlab/chaver/mining/examples/ExampleClosedItemsetMining.java):

```java
// Read the transactional database
TransactionalDatabase database = new DatReader("data/contextPasquier99.dat").read();
// Create the Choco model
Model model = new Model("Closed Itemset Mining");
// Array of Boolean variables where x[i] == 1 represents the fact that i belongs to the itemset
BoolVar[] x = model.boolVarArray("x", database.getNbItems());
// Integer variable that represents the frequency of x with the bounds [1, nbTransactions]
IntVar freq = model.intVar("freq", 1, database.getNbTransactions());
// Integer variable that represents the length of x with the bounds [1, nbItems]
IntVar length = model.intVar("length", 1, database.getNbItems());
// Ensures that length = sum(x)
model.sum(x, "=", length).post();
// Ensures that freq = frequency(x)
ConstraintFactory.coverSize(database, freq, x).post();
// Ensures that x is a closed itemset
ConstraintFactory.coverClosure(database, x).post();
Solver solver = model.getSolver();
// Variable heuristic : select item i such that freq(x U i) is minimal
// Value heuristic : instantiate it first to 0
solver.setSearch(Search.intVarSearch(
        new MinCov(model, database),
        new IntDomainMin(),
        x
));
// Create a list to store all the closed itemsets
List<Pattern> closedPatterns = new LinkedList<>();
while (solver.solve()) {
    int[] itemset = IntStream.range(0, x.length)
            .filter(i -> x[i].getValue() == 1)
            .map(i -> database.getItems()[i])
            .toArray();
    // Add the closed itemset with its frequency to the list
    closedPatterns.add(new Pattern(itemset, new int[]{freq.getValue()}));
}
System.out.println("List of closed itemsets for the dataset contextPasquier99 w.r.t. freq(x):");
// Print all the closed itemsets with their frequency
for (Pattern closed : closedPatterns) {
    System.out.println(Arrays.toString(closed.getItems()) +
            ", freq=" + closed.getMeasures()[0]);
}
```

After running this bloc of code, we get the following message in the console:

```
List of closed itemsets for the dataset contextPasquier99 w.r.t. freq(x):
[3], freq=4
[2, 5], freq=4
[2, 3, 5], freq=3
[1, 3], freq=3
[1, 2, 3, 5], freq=2
[1, 3, 4], freq=1
```

We have 6 closed itemsets w.r.t. the frequency in the dataset `contextPasquier99`.

## Architecture of the library

![Summary of constraints implemented with Choco-mining \label{fig:app}](paper/app.svg)

The above figure illustrates the architecture of Choco-Mining library. Multiple task examples(in blue) are linked to the constraints(in red) that can be used to perform them. The following constraints are available in the library:

- `CoverSize(x,f)` [SchausAG17]: Given an integer variable `f` that represents the frequency (noted `freq`) of an itemset `x`, the constraint ensures that `f = freq(x)`.
- `CoverClosure(x)` [SchausAG17]: The constraint ensures that `x` is closed w.r.t. the frequency, i.e. there exists no superset `y` of `x` such that `freq(x) = freq(y)`.
- `AdequateClosure(M,x)` [VernereyLAL22]: Given a set of measures `M`, the constraint ensures that `x` is closed w.r.t. `M`, i.e. there exists no superset `y` of `x` such that for each measure `m` in `M`, we have `m(x) = m(y)`.
- `FrequentSubs(s,x)` [Belaid2BL19]: Given a frequency threshold `s`, the constraint ensures that each subset `y` of `x` is frequent, i.e. `freq(y) >= s`.
- `InfrequentSupers(s,x)` [Belaid2BL19]: Given a frequency threshold `s`, the constraint ensures that each superset `y` of `x` is infrequent, i.e. `freq(y) <= s`.
- `Generator(x)` [BelaidBL19]: The constraint ensures that `x` is a generator, i.e. there exists no subset `y` of `x` such that `freq(y) = freq(x)`.
- `ClosedDiversity(H,j,s,x)` [HienLALLOZ20]: Given a history of itemsets `H`, a diversity threshold `j` and a minimum frequency threshold `s`, the constraint ensures that `x` is a diverse itemset (i.e. there exists no itemset `y` in `H` such that  `jaccard(x,y) >= j`), `x` is closed w.r.t. the frequency and `freq(x) >= s`.

Example of tasks that can be performed using these constraints include:

- Frequent Itemset Mining: Given a threshold `s`, find all the itemsets `x` such that `freq(x) >= s`.
- Closed Itemset Mining: Given a threshold `s`, find all the itemsets `x` such that `freq(x) >= s` and that are closed w.r.t. the frequency.
- Skypattern Mining: Given a set of measures `M`, find all the itemsets `x` such that there exists no other itemset `y` that dominates `x`. We say that `y` dominates `x` iff for each measure `m` in `M` we have `m(y) >= m(y)` and there exists at least one measure `m` in `M` such that `m(y) > m(x)`.
- Maximal Frequent Itemset Mining: Given a threshold `s`, find all the itemsets `x` such that `freq(x) >= s` and for each superset `y` of `x` we have `freq(y) < s`.
- Minimal Infrequent Itemset Mining: Given a threshold `s`, find all the itemsets `x` such that `freq(x) < s` and for each subset `y` of `x` we have `freq(y) >= s`.
- Generator Mining: Find all the itemsets `x` that are generators.
- Association Rule Mining: Find all the association rules `x => y` that respect the constraints specified by the user.
- Diverse Itemset Mining: Given a diversity threshold `j` and a minimum frequency threshold `s`, find all the diverse itemsets that are closed w.r.t. the frequency and such that `freq(x) >= s`.

## Installation

To use the Choco-mining library, you need to have Java 8+ and [Maven 3](https://maven.apache.org/) installed on your computer. Then, you can simply install the library with the following command:

```bash
make install
```

After that, [create a new Maven project](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) and add a new dependency in the pom.xml file:

```xml
<dependency>
    <groupId>io.gitlab.chaver</groupId>
    <artifactId>choco-mining</artifactId>
    <version>1.0.2</version>
</dependency>
```

That's it ! You can now use all the available constraints in your project.

## Documentation

Examples on how to use the available constraints to perform different tasks can be found in the [Wiki](https://gitlab.com/chaver/choco-mining/-/wikis/home). The Javadoc is available [here](https://chaver.gitlab.io/choco-mining/).

## Support

Please submit bug reports, questions and feature requests as [Issues](https://gitlab.com/chaver/choco-mining/-/issues).

## License

Choco-Mining is licensed under the [MIT License](https://gitlab.com/chaver/choco-mining/-/blob/master/LICENSE.txt).

## Citation

If you use Choco-Mining in a publication, please cite it as:

```bibtex
@article{vernerey2023java,
  title={A Java Library for Itemset Mining with Choco-solver},
  author={Vernerey, Charles and Loudni, Samir},
  journal={Journal of Open Source Software},
  volume={8},
  number={88},
  pages={5654},
  year={2023}
}
```

## Acknowledgements

We would like to thank Noureddine Aribi, Yahia Lebbah, Mohamed-Bachir Belaid and Nadjib Lazaar for all the valuable discussions on the global constraints implemented in Choco-Mining.

## References

- **[SchausAG17]** [Schaus, P., Aoga, J. O., & Guns, T. (2017). Coversize: A global constraint for frequency-based itemset mining. In *Principles and Practice of Constraint Programming: 23rd International Conference, CP 2017, Melbourne, VIC, Australia, August 28–September 1, 2017, Proceedings 23* (pp. 529-546). Springer International Publishing](https://link.springer.com/chapter/10.1007/978-3-319-66158-2_34)
- **[VernereyLAL22]** [Vernerey, C., Loudni, S., Aribi, N., & Lebbah, Y. (2022, July). Threshold-free pattern mining meets multi-objective optimization: Application to association rules. In *IJCAI-ECAI 2022-31ST INTERNATIONAL JOINT CONFERENCE ON ARTIFICIAL INTELLIGENCE*.](https://www.ijcai.org/proceedings/2022/0261)
- **[Belaid2BL19]** [Belaid, M. B., Bessiere, C., & Lazaar, N. (2019, August). Constraint programming for mining borders of frequent itemsets. In *IJCAI 2019-28th International Joint Conference on Artificial Intelligence* (pp. 1064-1070).](https://hal-lirmm.ccsd.cnrs.fr/lirmm-02310629/)
- **[BelaidBL19]** [Belaid, M. B., Bessiere, C., & Lazaar, N. (2019, May). Constraint programming for association rules. In *Proceedings of the 2019 SIAM International Conference on Data Mining* (pp. 127-135). Society for Industrial and Applied Mathematics.](https://epubs.siam.org/doi/abs/10.1137/1.9781611975673.15)
- **[HienLALLOZ20]** [Hien, A., Loudni, S., Aribi, N., Lebbah, Y., Laghzaoui, M. E. A., Ouali, A., & Zimmermann, A. (2021). A relaxation-based approach for mining diverse closed patterns. In *Machine Learning and Knowledge Discovery in Databases: European Conference, ECML PKDD 2020, Ghent, Belgium, September 14–18, 2020, Proceedings, Part I* (pp. 36-54). Springer International Publishing.](https://link.springer.com/chapter/10.1007/978-3-030-67658-2_3)
- **[KadiogluWHH23]** [Kadioglu, S., Wang, X., Hosseininasab, A., & van Hoeve, W. J. (2023). Seq2Pat: Sequence‐to‐pattern generation to bridge pattern mining with machine learning. AI Magazine.](https://doi.org/10.1002/aaai.12081)
