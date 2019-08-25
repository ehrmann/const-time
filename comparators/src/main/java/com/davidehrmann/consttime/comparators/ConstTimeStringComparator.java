package com.davidehrmann.consttime.comparators;

import java.util.Comparator;

public final class ConstTimeStringComparator implements Comparator<String> {

    public static final ConstTimeStringComparator INSTANCE = new ConstTimeStringComparator();

    private ConstTimeStringComparator() {

    }

    @Override
    public int compare(String o1, String o2) {
        int diff = 0;
        int minLen = Math.min(o1.length(), o2.length());
        for (int i = 0; i < minLen; ++i) {
            int charDiff = o1.charAt(i) - o2.charAt(i);

            // ~((-x >> 31) | (x >> 31)) is 0 for x != 0, 0xffffffff otherwise
            diff = diff | (~((-diff >> 31) | (diff >> 31)) & charDiff);
        }

        int lenDiff = o1.length() - o2.length();
        diff = diff | (~((-diff >> 31) | (diff >> 31)) & lenDiff);

        return diff;
    }
}
