import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class HashMapUtils {

    // TODO: method map -> likely hash code prefixes

    private static int warmupResult = 0;

    // Initializer to warm up HashMap in the JVM
    static {
        Random random = ThreadLocalRandom.current();
        Map<String, String> warmup = new HashMap<>();

        for (int i = 0; i < 30000; ++i) {
            if (i % 1000 == 0) {
                warmup.clear();
            }

            String key = Integer.toHexString(random.nextInt());
            warmup.put(key, "placeholder");
            warmupResult = Objects.hash(warmupResult, warmup.containsKey(key));
            warmupResult = Objects.hash(warmupResult, warmup.containsKey(Integer.toHexString(random.nextInt())));
            warmupResult = Objects.hash(warmupResult, warmup.get(key));
        }
    }

    public static int findSize(HashMap<?, ?> hashMap) {
        Random random = ThreadLocalRandom.current();
        // (h = key.hashCode()) ^ (h >>> 16)

        int baselineIterations = 100;
        long hashCodePresentBaseline = 0;
        long hashCodeAbsentBaseline = 0;

        Map<Object, Object> temp = new HashMap<>();
        temp.put("key", 42);
        int targetHashCode = "key".hashCode();

        for (int i = 0; i < baselineIterations; ++i) {
            MockedHashCode mockedHashCode = new MockedHashCode(ThreadLocalRandom.current().nextInt());
            long start = System.nanoTime();
            warmupResult ^= Boolean.hashCode(temp.containsKey(mockedHashCode));
            hashCodePresentBaseline = hashCodePresentBaseline + (System.nanoTime() - start);

            mockedHashCode = new MockedHashCode(targetHashCode);
            start = System.nanoTime();
            warmupResult ^= Boolean.hashCode(temp.containsKey(mockedHashCode));
            hashCodeAbsentBaseline = hashCodeAbsentBaseline + (System.nanoTime() - start);
        }

        //System.out.printf("present / absent = %.3f%n", (double) hashCodePresentBaseline / hashCodeAbsentBaseline);


        int[] bucketCounts = new int[5]; //31

        for (int i = 4; i < bucketCounts.length; ++i) {
            long[] times = new long[1 << i];
            long[] counts = new long[1 << i];

            for (long attempt = 0; attempt < (1 << i) * 100000L; ++attempt) {
                int targetBucket = random.nextInt() & (times.length - 1);
                int highOrder = targetBucket & 0xffff0000;
                int lowOrder = (targetBucket & 0xffff) ^ highOrder;
                String probe = HashCodeUtils.collisionForPrefix("", highOrder | lowOrder);

                long start = System.nanoTime();
                warmupResult ^= Boolean.hashCode(hashMap.containsKey(probe));
                times[targetBucket] += System.nanoTime() - start;
                ++counts[targetBucket];

                if (attempt > 0 && attempt % 100000 == 0) {
                    double[] averages = new double[times.length];
                    for (int j = 0; j < times.length; ++j) {
                        averages[j] = (double) times[j] / counts[j];
                    }

                    double var = 0.0;

                    double average = Arrays.stream(averages).average().getAsDouble();
                    double min = Arrays.stream(averages).min().getAsDouble();
                    double max = Arrays.stream(averages).max().getAsDouble();

                    double[] normalized = Arrays.stream(averages)
                            .map(v -> (v - min) / (max - min))
                            .toArray();

                    for (int j = 0; j < times.length; ++j) {
                        System.out.printf("%d: %.4f%n", j, normalized[j]);

                        if (j != 0 && j != 1 && j != 4 && j != 9) {
                            var += normalized[j] * normalized[j];
                        }
                    }

                    var = var / (times.length - 4);

                    double avgWithExtra = Arrays.stream(normalized)
                            .sorted()
                            .limit(times.length - 3)
                            .average()
                            .getAsDouble();

                    double varWithExtra = Arrays.stream(normalized)
                            .sorted()
                            .limit(times.length - 3)
                            .map(a -> (a - avgWithExtra) * (a - avgWithExtra))
                            .average()
                            .getAsDouble();

                    double avgWithFewer = Arrays.stream(normalized)
                            .sorted()
                            .limit(times.length - 5)
                            .average()
                            .getAsDouble();

                    double varWithFewer = Arrays.stream(normalized)
                            .sorted()
                            .limit(times.length - 5)
                            .map(a -> (a - avgWithFewer) * (a - avgWithFewer))
                            .average()
                            .getAsDouble();

                    System.out.printf("var = %.4f%n", var / (times.length - 4));
                    System.out.printf("var with extra = %.4f%n", varWithExtra);
                    System.out.printf("var with fewer = %.4f%n", varWithFewer);
                    System.out.println();


                }
            }

            for (int j = 0; j < times.length; ++j) {
                times[j] = times[j] / counts[j];
            }

            TimeIndexPair[] timeIndexPairs = new TimeIndexPair[times.length];
            for (int j = 0; j < times.length; ++j) {
                timeIndexPairs[j] = new TimeIndexPair(times[j], j);
            }

            Arrays.sort(timeIndexPairs);

            long[] data = Arrays.stream(timeIndexPairs).mapToLong(p -> p.time).toArray();

            Arrays.sort(times);
            int b = Clusters.cluster1DIn2(data);
            bucketCounts[i] = times.length - b;

            System.out.printf("Cluster i = %d, attempt = %d%n", i, -1);
            System.out.printf("  %d%n", bucketCounts[i]);

            System.out.print("  Buckets: ");
            System.out.println(Arrays.stream(timeIndexPairs)
                    .sorted()
                    .skip(b)
                    .map(p -> p.index)
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")));
        }


        return 10;
    }

    public static Set<Integer> findHashCollisions(HashMap<?, ?> hashMap) {
        int historySize = 1 << 16;
        Deque<Long> recentTimes = new ArrayDeque<>(historySize);

        long[] times;
        long[] counts;


        return null;


    }



    private static class MockedHashCode {
        private final int hashCode;

        private MockedHashCode(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    private static double cluster(long[] times, AtomicInteger atomicB) {
        if (times.length <= 1) {
            atomicB.set(0);
            return 1.0;
        }

        times = LongStream.of(times).sorted().toArray();

        // Jenks natural breaks
        double mean = LongStream.of(times).average().getAsDouble();
        double sdam = LongStream.of(times)
                .mapToDouble(l -> (mean - l) * (mean - l))
                .sum();
        double lastSum;
        double sum = Double.MAX_VALUE;
        int b = times.length / 2;
        double gvf;
        do {
            lastSum = sum;
            double leftMean = LongStream.of(times).limit(b).average().getAsDouble();
            double rightMean = LongStream.of(times).skip(b).average().getAsDouble();

            double leftSdbc = LongStream.of(times).limit(b)
                    .mapToDouble(l -> (leftMean - l) * (leftMean - l))
                    .sum();

            double rightSdbc = LongStream.of(times).skip(b)
                    .mapToDouble(l -> (rightMean - l) * (rightMean - l))
                    .sum();

            double sdcm = sdam - (leftSdbc + rightSdbc);

            gvf = (sdam - sdcm) / sdam;

            if (leftSdbc > rightSdbc) {
                --b;
            } else if (leftSdbc < rightSdbc) {
                ++b;
            } else {
                break;
            }

            sum =  leftSdbc + rightSdbc;
        } while (sum < lastSum);

        atomicB.set(b);
        return gvf;
    }

    // (SDAM - SDCM) / SDAM

    private static class TimeIndexPair implements Comparable<TimeIndexPair> {
        private final long time;
        private final int index;

        private TimeIndexPair(long time, int index) {
            this.time = time;
            this.index = index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            } else {
                return compareTo((TimeIndexPair) o) == 0;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(time, index);
        }

        @Override
        public int compareTo(TimeIndexPair o) {
            int diff = Long.compare(this.time, o.time);
            if (diff == 0) {
                diff = Integer.compare(this.index, o.index);
            }
            return diff;
        }

    }
}
