package com.davidehrmann.consttime.comparators;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public final class ConstTimeComparatorBuilder<T> {

    private List<Comparator<T>> comparators = new ArrayList<>();

    public ConstTimeComparatorBuilder() {

    }

    public ConstTimeComparatorBuilder<T> comparingInt(ToIntFunction<T> toInt) {
        comparators.add(new IntExtractingComparator<>(toInt));
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingIntDescending(ToIntFunction<T> toInt) {
        comparators.add(new IntExtractingComparator<>(toInt).reversed());
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingLong(ToLongFunction<T> toLong) {
        comparators.add(new LongExtractingComparator<>(toLong));
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingLongDescending(ToLongFunction<T> toLong) {
        comparators.add(new LongExtractingComparator<>(toLong).reversed());
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingString(Function<T, String> toString) {
        comparators.add(new StringExtractingComparator<>(toString));
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingStringDescending(Function<T, String> toString) {
        comparators.add(new StringExtractingComparator<>(toString).reversed());
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingByteArray(Function<T, byte[]> toByteArray) {
        comparators.add(new ByteArrayExtractingComparator<>(toByteArray));
        return this;
    }

    public ConstTimeComparatorBuilder<T> comparingByteArrayDescending(Function<T, byte[]> toByteArray) {
        comparators.add(new ByteArrayExtractingComparator<>(toByteArray).reversed());
        return this;
    }

    public Comparator<T> build() {
        return new ChainedComparator<T>(comparators);
    }

    static class ChainedComparator<T> implements Comparator<T> {

        private final List<Comparator<T>> comparators;

        ChainedComparator(List<Comparator<T>> comparators) {
            this.comparators = new ArrayList<>(comparators);
        }

        @Override
        public int compare(T o1, T o2) {
            int diff = 0;

            for (Comparator<T> comparator : comparators) {
                int tDiff = comparator.compare(o1, o2);
                diff = diff | (~((-diff >> 31) | (diff >> 31)) & tDiff);
            }

            return diff;
        }
    }

    static class StringExtractingComparator<T> implements Comparator<T> {

        private final Function<T, String> toString;

        StringExtractingComparator(Function<T, String> toString) {
            this.toString = Objects.requireNonNull(toString);
        }


        @Override
        public int compare(T o1, T o2) {
            String s1 = toString.apply(o1);
            String s2 = toString.apply(o2);
            return ConstTimeStringComparator.INSTANCE.compare(s1, s2);
        }
    }

    static class ByteArrayExtractingComparator<T> implements Comparator<T> {

        private final Function<T, byte[]> toByteArray;

        ByteArrayExtractingComparator(Function<T, byte[]> toByteArray) {
            this.toByteArray = Objects.requireNonNull(toByteArray);
        }


        @Override
        public int compare(T o1, T o2) {
            byte[] s1 = toByteArray.apply(o1);
            byte[] s2 = toByteArray.apply(o2);
            return ConstTimeByteArrayComparator.INSTANCE.compare(s1, s2);
        }
    }

    static class IntExtractingComparator<T> implements Comparator<T> {

        private final ToIntFunction<T> toInt;

        IntExtractingComparator(ToIntFunction<T> toInt) {
            this.toInt = Objects.requireNonNull(toInt);
        }

        @Override
        public int compare(T o1, T o2) {
            long i1 = toInt.applyAsInt(o1);
            long i2 = toInt.applyAsInt(o2);

            long diff = i1 - i2;

            // Normalize diff so it's either -1, 0, or 1 (safe to cast to an int)
            return (int) ((diff >> 63) | ((-diff >> 63) & 1));
        }
    }

    static class LongExtractingComparator<T> implements Comparator<T> {

        private final ToLongFunction<T> toLong;

        LongExtractingComparator(ToLongFunction<T> toLong) {
            this.toLong = Objects.requireNonNull(toLong);
        }

        @Override
        public int compare(T o1, T o2) {
            long l1 = toLong.applyAsLong(o1);
            long l2 = toLong.applyAsLong(o2);

            // The ordeal below is to avoid branching
            long diff = l1 - l2;

            // Overflow checking is awkward if l2 is Long.MIN_VALUE
            // nonMinLong is -1 if l2 is Long.MIN_VALUE, 0 otherwise
            long l2MinLongDiff = l2 - Long.MIN_VALUE;
            long nonMinLong = l2MinLongDiff >> 63 | -l2MinLongDiff >> 63;

            // -1 if diff has an overflow, 0 otherwise
            // Overflow checking doesn't work if l2 is Long.MIN_VALUE
            // If l2 is Long.MIN_VALUE, overflow is set if l1 is non-negative
            // if (l1 ^ diff) & (-l2 ^ diff) is less than zero, diff overflowed
            long overflow = nonMinLong & (((l1 ^ diff) & (-l2 ^ diff)) >> 63)
                    | (~nonMinLong & (~l1 >> 63));

            // Normalize diff so it's either -1, 0, or 1 (safe to cast to an int)
            long normalizedDiff = (int) ((diff >> 63) | ((-diff >> 63) & 1));

            // If diff has an overflow and l2 is negative, return 1
            // If diff has an overflow and l1 is negative, return -1
            // Otherwise, return the normalized diff
            return (int) (overflow & ((l2 >> 63) & 1)
                    | overflow & (l1 >> 63)
                    | ~overflow & normalizedDiff);
        }
    }
}
