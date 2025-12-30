# ChocoMining-FreqRare

Algorithmic implementation of the FREQRARE constraint for itemset mining using **Choco Solver**.

This repository contains a Java implementation of the FREQRARE global constraint for mining frequent itemsets with **Multiple Minimum Supports (MIS)**, based on the paper:

**Belaid, M.-B., & Lazaar, N. (2021).** *Constraint Programming for Itemset Mining with Multiple Minimum Supports.* ICTAI 2021.

The implementation extends the **Choco-Mining** library by integrating a scalable, propagator-based approach that follows the algorithmic design described in the paper.

## Features

- **FREQRARE Constraint**  
  Mines frequent itemsets with multiple MIS without the need for auxiliary variables or reified constraints.

- **Propagator-based Algorithm**  
  Implements Algorithm 1 from the paper for backtrack-free search with `minMIS` variable ordering heuristic.

- **Flexible Queries**  
  Supports user-defined constraints such as:
  - Distance between MIS values
  - Minimum itemset size
  - k-patterns mining (distinct sets of itemsets)

- **MIS Calculator**  
  Computes minimum item supports from transactional data using the formula from the paper:  
  `MIS(i) = max(beta * frequency(i), MIS_min)`

- **Example Tests**  
  Demonstrates usage on real datasets with `ExampleMISFreqRareTest.java`.

## Algorithmic Overview

The `FreqRareConstraint` implementation follows **Algorithm 1** from Belaid & Lazaar (2021). The key steps are:

## Algorithm 1: Propagator for FREQRARE

**Input:** `S` = minimum item supports (MIS)  
**InOut:** `x = [x1, ..., xn]` (Boolean variables representing items)

```text
1.  P ← {i | xi = 1}          // Selected items
2.  U ← {i | xi not instantiated} // Unassigned items
3.  cover(P) ← intersection of transactions containing items in P
4.  s ← min(MIS of P ∪ U)      // Minimum MIS among P and unassigned
5.  if |cover(P)| < s then
6.      fail()
7.  for i in U do
8.      if |cover(P ∪ {i})| < s then
9.          xi ← 0

```

1. **Initialization**  
   - Input: `MIS[i]` for all items, `BoolVar[] x` representing items, vertical representation of item covers.  
   - Partition variables into instantiated (`P`) and uninstantiated (`U`) sets.

2. **Cover Computation**  
   - Compute the cover of the current partial pattern `P` (intersection of transaction sets for items in `P`).  
   - If `P` is empty, the cover is all transactions.

3. **Minimum MIS Calculation**  
   - Determine `s = min(MIS[i])` among all items in `P ∪ U`.

4. **Frequency Check**  
   - If `|cover(P)| < s`, fail the propagation (prune this branch).

5. **Domain Pruning**  
   - For each unassigned item `i ∈ U`, check if adding `i` would violate the frequency constraint:  
     - If yes, set `x[i] = false`.

6. **Entailment Check**  
   - If all variables satisfy the frequency requirement, the constraint is entailed.

> This propagator ensures that only itemsets respecting multiple minimum supports are explored during the search.



## Installation

* Clone the repository:

```bash
git clone https://github.com/your-username/ChocoMining-FreqRare.git
cd ChocoMining-FreqRare
```

* Ensure you have Java 17+ and Maven installed.

* Build the project:

```bash
mvn clean install
```

## Usage

1. **Prepare a transactional dataset**

* Use `.dat` files as in FIMI format (e.g., `data/contextPasquier99.dat`).

2. **Compute MIS values**

```java
MISCalculator misCalculator = new MISCalculator(database, 0.6, 1);
int[] misValues = misCalculator.computeMIS();
```

3. **Build item covers map**

```java
Map<Integer, BitSet> itemCovers = new HashMap<>();
BitSet[] verticalRepresentation = database.getVerticalRepresentation();
for (int i = 0; i < verticalRepresentation.length; i++) {
    itemCovers.put(i, verticalRepresentation[i]);
}
```

4. **Create Boolean decision variables and post the FREQRARE constraint**

```java
Model model = new Model("MIS and FreqRare Test");
BoolVar[] boolItems = model.boolVarArray("I", database.getNbItems());
FreqRareConstraint.post(boolItems, misValues, itemCovers, database.getNbTransactions());
```

5. **Solve**

* Use a custom search strategy based on `minMIS` for efficient backtrack-free enumeration:

```java
Solver solver = model.getSolver();
solver.setSearch(Search.intVarSearch(new MinMisSelector(intItems, misValues), new IntDomainMax(), intItems));

while (solver.solve()) {
    // Print itemsets
}
```

## Folder Structure

```
ChocoMining-FreqRare/
│
├─ src/main/java/io/gitlab/chaver/mining/patterns/constraints/
│   └─ FreqRareConstraint.java    # FREQRARE implementation
│
├─ src/main/java/io/gitlab/chaver/mining/patterns/util/
│   └─ MISCalculator.java         # Multiple Minimum Support calculator
│
├─ src/main/java/io/gitlab/chaver/mining/examples/
│   └─ ExampleMISFreqRareTest.java  # Test example with real dataset
│
└─ data/
    └─ contextPasquier99.dat      # Example dataset
```

## References

* Belaid, M.-B., & Lazaar, N. (2021). *Constraint Programming for Itemset Mining with Multiple Minimum Supports*. ICTAI 2021.
* Choco Solver: [https://choco-solver.org/](https://choco-solver.org/)
* FIMI Repository: [http://fimi.ua.ac.be/data/](http://fimi.ua.ac.be/data/)

## License

* This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.