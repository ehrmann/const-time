package com.davidehrmann.consttime.comparators;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class ConstantTimeStringComparatorTest {

    @Test
    public void testRandomStrings() {
        for (int i = 0; i < 100000; ++i) {
            int l1 = ThreadLocalRandom.current().nextInt(10);
            int l2 = ThreadLocalRandom.current().nextInt(10);

            byte[] b1 = new byte[l1];
            byte[] b2 = new byte[l2];

            ThreadLocalRandom.current().nextBytes(b1);
            ThreadLocalRandom.current().nextBytes(b2);

            String s1 = new String(b1, StandardCharsets.ISO_8859_1);
            String s2 = new String(b2, StandardCharsets.ISO_8859_1);

            int d1 = ConstantTimeStringComparator.INSTANCE.compare(s1, s2);
            int d2 = s1.compareTo(s2);

            assertEquals(Math.signum(d2), Math.signum(d1), .001);
        }
    }
}