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

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataReaderTest {

    private final String binPath = "src/test/resources/read/read.txt";
    private final String datPath = "src/test/resources/read/read.dat";

    private final boolean T = true;
    private final boolean F = false;

    private final boolean[][] verticalRep = new boolean[][] {
            {T,F,F},
            {F,T,T},
            {T,F,F},
            {F,F,T},
            {T,T,F}
    };
    private final int[][] values = new int[][] {
            {1, 2, 10, 50, 90},
            {1, 2, 25, 50, 75}
    };

    private void testRead(DataReader reader, int nbClass, boolean[][] vRep, int[][] values) throws IOException {
        TransactionalDatabase d = reader.read();
        assertEquals(vRep.length, d.getVerticalRepresentation().length);
        for (int i = 0; i < vRep.length; i++) {
            for (int j = 0; j < vRep[i].length; j++) {
                assertEquals(vRep[i][j], d.getVerticalRepresentation()[i].get(j));
            }
        }
        assertEquals(nbClass, d.getNbClass());
        assertArrayEquals(values, d.getValues());
    }

    @Test
    public void testBinReader() throws IOException {
        DataReader reader = new BinReader(binPath, 2);
        testRead(reader, 2, verticalRep, values);
    }

    @Test
    public void testBinReader2() throws IOException {
        DataReader reader = new BinReader(binPath, 2);
        testRead(reader, 2, verticalRep, values);
    }

    @Test
    public void testDatReader() throws IOException {
        DataReader reader = new DatReader(datPath, 2);
        testRead(reader, 2, verticalRep, values);
    }

    @Test
    public void testDatReader2() throws IOException {
        DataReader reader = new DatReader(datPath, 2);
        testRead(reader, 2, verticalRep, values);
    }

    @Test
    public void testDatReader3() throws IOException {
        DataReader reader = new DatReader("src/test/resources/read/read2.dat", 0);
        boolean[][] vRep = new boolean[][] {
                {T, T},
                {F, T},
                {T, F}
        };
        testRead(reader, 1, vRep, new int[][]{});
    }


}
