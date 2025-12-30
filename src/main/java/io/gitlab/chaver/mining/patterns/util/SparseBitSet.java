/*
 * This file is part of io.gitlab.chaver:choco-mining (https://gitlab.com/chaver/choco-mining)
 *
 * Copyright (c) 2025, IMT Atlantique
 *
 * Licensed under the MIT license.
 *
 * See LICENSE file in the project root for full license information.
 */
package io.gitlab.chaver.mining.patterns.util;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Like a reversible sparse BitSet but it is not reversible
 */
public class SparseBitSet {

    private long[] words;
    private int[] index;
    private int limit;

    public SparseBitSet() {}

    public SparseBitSet(int nbits) {
        BitSet b = new BitSet(nbits);
        b.set(0, nbits);
        words = b.toLongArray();
        int size = words.length;
        limit = size - 1;
        index = IntStream.range(0, size).toArray();
    }

    /**
     * Return value of specified index offset
     * @param m array of long
     * @param offset index
     * @return m[offset] if offset < m.length, 0 otherwise
     */
    private long getValue(long[] m, int offset) {
        return offset < m.length ? m[offset] : 0;
    }

    public void and(long[] m) {
        for (int i = limit; i >= 0; i--) {
            int offset = index[i];
            long w = words[offset] & getValue(m, offset);
            words[offset] = w;
            checkWords(offset, i);
        }
    }

    private void checkWords(int offset, int i) {
        if (words[offset] == 0) {
            index[i] = index[limit];
            index[limit] = offset;
            limit--;
        }
    }

    /**
     * Check if words is a subset of m (ex : 100 is a subset of 110)
     * @param m superset
     * @return true if words is a subset of m
     */
    public boolean isSubsetOf(long[] m) {
        for (int i = 0; i <= limit; i++) {
            int offset = index[i];
            if ((~getValue(m, offset) & words[offset]) != 0) {
                return false;
            }
        }
        return true;
    }

    public int cardinality() {
        int sum = 0;
        for (int i = 0; i <= limit; i++) {
            int offset = index[i];
            sum += Long.bitCount(words[offset]);
        }
        return sum;
    }

    public boolean isEmpty() {
        return limit == -1;
    }

    public void reset(long[] mask, int[] indexMask, int limitMask) {
        this.words = mask;
        this.index = indexMask;
        this.limit = limitMask;
    }

    public int andCount(long[] m) {
        int sum = 0;
        for (int i = limit; i >= 0; i--) {
            int offset = index[i];
            sum += Long.bitCount(words[offset] & getValue(m, offset));
        }
        return sum;
    }

    public BitSet toBitSet() {
        return BitSet.valueOf(words);
    }
}
