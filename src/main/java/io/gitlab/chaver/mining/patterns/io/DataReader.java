/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.io;

import io.gitlab.chaver.mining.patterns.io.values.DoubleValuesReader;
import io.gitlab.chaver.mining.patterns.io.values.IValuesReader;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class used to read data :
 * - transactions (i.e. sets of items)
 * - value of items
 */
public abstract class DataReader {

    /** Path of the file which contains the transactions data */
    protected String dataPath;
    /** Extension of the file which contains the transactions data (e.g. txt) */
    protected String extension;
    /** number of measures which require a values file (e.g. mean, max, min) */
    protected int nbValueMeasures;
    /** number of transactions in the dataset */
    protected int nbTransactions;
    /** number of items in the dataset */
    protected int nbItems;
    /** TRUE if classes are ignored (i.e. database.nbClass = 0) */
    protected boolean noClasses;
    /** Used to read the values of each item */
    protected @Setter IValuesReader valuesReader;

    public DataReader(String dataPath, int nbValueMeasures) {
        this.dataPath = dataPath;
        this.nbValueMeasures = nbValueMeasures;
        String[] pathSplit = dataPath.split("\\.");
        this.extension = pathSplit[pathSplit.length - 1];
        valuesReader = new DoubleValuesReader(nbValueMeasures, extension, dataPath);
    }

    public DataReader(String dataPath, int nbValueMeasures, boolean noClasses) {
        this(dataPath, nbValueMeasures);
        this.noClasses = noClasses;
    }

    public DataReader(String dataPath) {
        this(dataPath, 0, true);
    }

    /**
     * Read files which contain transactions and values of items
     * @return a database object with the data
     */
    public abstract TransactionalDatabase read() throws IOException;

    /**
     * Read files which contain values of items
     * For instance, if we want to read three files of values of zoo.txt, the files
     *   zoo.val0, zoo.val1 and zoo.val2 will be read
     * @throws IOException if a file doesn't exist
     */
    protected int[][] readValueFiles() throws IOException {
        return valuesReader.readValueFiles();
    }

    protected boolean skipLine(String line) {
        return line.isEmpty() || line.charAt(0) == '#' || line.charAt(0) == '%' || line.charAt(0) == '@';
    }
}
