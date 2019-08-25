package com.davidehrmann.consttime.comparators;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertThat;

@RunWith(Enclosed.class)
public class ConstTimeComparatorBuilderTest {

    public static class BuilderTest {

        @Test
        public void testComparingInt() {
            Comparator<Integer> comparator = new ConstTimeComparatorBuilder<Integer>()
                    .comparingInt(Integer::intValue)
                    .build();

            assertThat(comparator.compare(0, 1), Matchers.lessThan(0));
            assertThat(comparator.compare(1, 0), Matchers.greaterThan(0));
            assertThat(comparator.compare(0, 0), Matchers.equalTo(0));
        }

        @Test
        public void testComparingIntDescending() {
            Comparator<Integer> comparator = new ConstTimeComparatorBuilder<Integer>()
                    .comparingIntDescending(Integer::intValue)
                    .build();

            assertThat(comparator.compare(0, 1), Matchers.greaterThan(0));
            assertThat(comparator.compare(1, 0), Matchers.lessThan(0));
            assertThat(comparator.compare(0, 0), Matchers.equalTo(0));
        }

        @Test
        public void testComparingLong() {
            Comparator<Long> comparator = new ConstTimeComparatorBuilder<Long>()
                    .comparingLong(Long::longValue)
                    .build();

            assertThat(comparator.compare(0L, 1L), Matchers.lessThan(0));
            assertThat(comparator.compare(1L, 0L), Matchers.greaterThan(0));
            assertThat(comparator.compare(0L, 0L), Matchers.equalTo(0));
        }

        @Test
        public void testComparingLongDescending() {
            Comparator<Long> comparator = new ConstTimeComparatorBuilder<Long>()
                    .comparingLongDescending(Long::longValue)
                    .build();

            assertThat(comparator.compare(0L, 1L), Matchers.greaterThan(0));
            assertThat(comparator.compare(1L, 0L), Matchers.lessThan(0));
            assertThat(comparator.compare(0L, 0L), Matchers.equalTo(0));
        }

        @Test
        public void testComparingString() {
            Comparator<String> comparator = new ConstTimeComparatorBuilder<String>()
                    .comparingString(String::toString)
                    .build();

            assertThat(comparator.compare("a", "b"), Matchers.lessThan(0));
            assertThat(comparator.compare("b", "a"), Matchers.greaterThan(0));
            assertThat(comparator.compare("a", "a"), Matchers.equalTo(0));
        }

        @Test
        public void testComparingStringDescending() {
            Comparator<String> comparator = new ConstTimeComparatorBuilder<String>()
                    .comparingStringDescending(String::toString)
                    .build();

            assertThat(comparator.compare("a", "b"), Matchers.greaterThan(0));
            assertThat(comparator.compare("b", "a"), Matchers.lessThan(0));
            assertThat(comparator.compare("a", "a"), Matchers.equalTo(0));
        }

        @Test
        public void testComparingByteArray() {
            Comparator<String> comparator = new ConstTimeComparatorBuilder<String>()
                    .comparingByteArray(s -> s.getBytes(StandardCharsets.US_ASCII))
                    .build();

            assertThat(comparator.compare("a", "b"), Matchers.lessThan(0));
            assertThat(comparator.compare("b", "a"), Matchers.greaterThan(0));
            assertThat(comparator.compare("a", "a"), Matchers.equalTo(0));
        }

        @Test
        public void testComparingByteArrayDescending() {
            Comparator<String> comparator = new ConstTimeComparatorBuilder<String>()
                    .comparingByteArrayDescending(s -> s.getBytes(StandardCharsets.US_ASCII))
                    .build();

            assertThat(comparator.compare("a", "b"), Matchers.greaterThan(0));
            assertThat(comparator.compare("b", "a"), Matchers.lessThan(0));
            assertThat(comparator.compare("a", "a"), Matchers.equalTo(0));
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Test
        public void testChain() {
            // Use ArrayLists so we can spy on them
            List<String> left = Mockito.spy(new ArrayList<>(Arrays.asList("a", "b")));
            List<String> right = Mockito.spy(new ArrayList<>(Arrays.asList("b", "b")));

            Comparator<List<String>> comparator = new ConstTimeComparatorBuilder<List<String>>()
                    .comparingString(l -> l.get(0))
                    .comparingString(l -> l.get(1))
                    .build();

            assertThat(comparator.compare(left, right), Matchers.lessThan(0));

            // Verify that the comparison wasn't short circuited
            Mockito.verify(left).get(0);
            Mockito.verify(left).get(1);
            Mockito.verify(right).get(0);
            Mockito.verify(right).get(1);
            Mockito.verifyNoMoreInteractions(left, right);
        }
    }

    @RunWith(Parameterized.class)
    public static class IntExtractingComparatorTest {

        private final int left;
        private final int right;

        public IntExtractingComparatorTest(int left, int right) {
            this.left = left;
            this.right = right;
        }

        @Parameterized.Parameters(name = "compare({0}, {1})")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {0, 0},
                    {1, 2},
                    {Integer.MIN_VALUE, Integer.MIN_VALUE},
                    {Integer.MAX_VALUE, Integer.MAX_VALUE},
                    {Integer.MAX_VALUE, -1},
                    {Integer.MAX_VALUE, Integer.MAX_VALUE - 1},
                    {Integer.MIN_VALUE, 1},
                    {Integer.MIN_VALUE, Integer.MIN_VALUE + 1},
                    {Integer.MIN_VALUE, Integer.MAX_VALUE}
            });
        }

        private final Comparator<Integer> intComparator =
                new ConstTimeComparatorBuilder.IntExtractingComparator<>(i -> i);

        @Test
        public void testCompare() {
            int expected = Integer.compare(left, right);
            int actual = intComparator.compare(left, right);
            if (expected < 0) {
                assertThat(actual, Matchers.lessThan(0));
            } else if (expected > 0) {
                assertThat(actual, Matchers.greaterThan(0));
            } else {
                assertThat(actual, Matchers.equalTo(0));
            }
        }

        @Test
        public void testSwappedCompare() {
            int expected = Integer.compare(right, left);
            int actual = intComparator.compare(right, left);
            if (expected < 0) {
                assertThat(actual, Matchers.lessThan(0));
            } else if (expected > 0) {
                assertThat(actual, Matchers.greaterThan(0));
            } else {
                assertThat(actual, Matchers.equalTo(0));
            }
        }
    }

    @RunWith(Parameterized.class)
    public static class LongExtractingComparatorTest {

        private final long left;
        private final long right;

        public LongExtractingComparatorTest(long left, long right) {
            this.left = left;
            this.right = right;
        }

        @Parameterized.Parameters(name = "compare({0}, {1})")
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    {0, 0},
                    {1, 2},
                    {Long.MIN_VALUE, Long.MIN_VALUE},
                    {Long.MAX_VALUE, Long.MAX_VALUE},
                    {Long.MAX_VALUE, -1},
                    {Long.MAX_VALUE, Long.MAX_VALUE - 1},
                    {Long.MIN_VALUE, 1},
                    {Long.MIN_VALUE, Long.MIN_VALUE + 1},
                    {Long.MIN_VALUE, Long.MAX_VALUE}
            });
        }

        private final Comparator<Long> longComparator =
                new ConstTimeComparatorBuilder.LongExtractingComparator<>(i -> i);

        @Test
        public void testCompare() {
            int expected = Long.compare(left, right);
            int actual = longComparator.compare(left, right);
            if (expected < 0) {
                assertThat(actual, Matchers.lessThan(0));
            } else if (expected > 0) {
                assertThat(actual, Matchers.greaterThan(0));
            } else {
                assertThat(actual, Matchers.equalTo(0));
            }
        }

        @Test
        public void testSwappedCompare() {
            int expected = Long.compare(right, left);
            int actual = longComparator.compare(right, left);
            if (expected < 0) {
                assertThat(actual, Matchers.lessThan(0));
            } else if (expected > 0) {
                assertThat(actual, Matchers.greaterThan(0));
            } else {
                assertThat(actual, Matchers.equalTo(0));
            }
        }
    }

    public static class LongExtractingComparatorRandomTest {

        private final Comparator<Long> longComparator =
                new ConstTimeComparatorBuilder.LongExtractingComparator<>(i -> i);

        @Test
        public void testCompare() {
            Random random = new Random();
            for (int i = 0; i < 1_000_000; ++i) {
                long left = random.nextLong();
                long right = random.nextLong();

                int expected = Long.compare(left, right);
                int actual = longComparator.compare(left, right);
                if (expected < 0) {
                    assertThat(actual, Matchers.lessThan(0));
                } else if (expected > 0) {
                    assertThat(actual, Matchers.greaterThan(0));
                } else {
                    assertThat(actual, Matchers.equalTo(0));
                }

                expected = Long.compare(right, left);
                actual = longComparator.compare(right, left);
                if (expected < 0) {
                    assertThat(actual, Matchers.lessThan(0));
                } else if (expected > 0) {
                    assertThat(actual, Matchers.greaterThan(0));
                } else {
                    assertThat(actual, Matchers.equalTo(0));
                }
            }
        }
    }
}
