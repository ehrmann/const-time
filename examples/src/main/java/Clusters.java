import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;

public class Clusters {

    public static double cluster(long[] times, AtomicInteger atomicBreak) {
        if (times.length <= 1) {
            atomicBreak.set(0);
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

            sum = leftSdbc + rightSdbc;
        } while (sum < lastSum);

        atomicBreak.set(b);
        return gvf;
    }

    public static int cluster1DIn2(long[] data) {
        long previous = Long.MIN_VALUE;
        for (int i = 0; i < data.length; ++i) {
            if (data[i] < previous) {
                throw new IllegalArgumentException("data must be sorted");
            }
            previous = data[i];
        }

        if (data.length <= 1) {
            return 0;
        }

        int bestBreak = 0;
        double bestVar = Double.MAX_VALUE;
        for (int i = 1; i < data.length - 1; ++i) {
            double lMean = Arrays.stream(data).limit(i).average().getAsDouble();
            double lVar = Arrays.stream(data).limit(i).mapToDouble(l -> (l - lMean) * (l - lMean)).average().getAsDouble();

            double rMean = Arrays.stream(data).skip(i).average().getAsDouble();
            double rVar = Arrays.stream(data).skip(i).mapToDouble(l -> (l - rMean) * (l - rMean)).average().getAsDouble();

            //System.out.printf("%d: %.0f%n", i, lVar + rVar);

            if (lVar - rVar < bestVar) {
                bestVar = lVar - rVar;
                bestBreak = i;
            }
        }

        return bestBreak;
    }
}
