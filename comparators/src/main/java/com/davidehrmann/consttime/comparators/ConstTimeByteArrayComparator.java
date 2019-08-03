package com.davidehrmann.consttime.comparators;

import java.util.Comparator;

public final class ConstTimeByteArrayComparator implements Comparator<byte[]> {

    public static final ConstTimeByteArrayComparator INSTANCE = new ConstTimeByteArrayComparator();

    private ConstTimeByteArrayComparator() {

    }

    @Override
    public int compare(byte[] o1, byte[] o2) {
        int diff = 0;
        int minLen = Math.min(o1.length, o2.length);
        for (int i = 0; i < minLen; ++i) {
            int byteDiff = o1[i] - o2[i];

            // ~((-x >> 31) | (x >> 31)) is 0 for x != 0, 0xffffffff otherwise
            diff = diff | (~((-diff >> 31) | (diff >> 31)) & byteDiff);
        }

        int lenDiff = o1.length - o2.length;
        diff = diff | (~((-diff >> 31) | (diff >> 31)) & lenDiff);

        return diff;
    }
}
