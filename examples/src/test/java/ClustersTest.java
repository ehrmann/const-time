import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertEquals;

public class ClustersTest {

    @Test
    public void test() {
        long[] data = new long[] {2, 3, 12, 13, 14, 15, 16, 17};
        assertEquals(2, Clusters.cluster1DIn2(data));
    }

    @Test
    public void test2() {
        Random random = new Random(0);
        int runs = 100;
        double breaks = 0;
        for (int j = 0; j < runs; ++j) {
            long[] data = new long[200];
            for (int i = 0; i < data.length; ++i) {
                if (random.nextLong() % 3 == 0) {
                    data[i] = 100 + (long) (20.0 * random.nextGaussian());
                } else {
                    data[i] = 200 + (long) (20.0 * random.nextGaussian());
                }
            }

            Arrays.sort(data);
            breaks += Clusters.cluster1DIn2(data);
        }

        assertEquals(66, breaks / runs, 10.0);
    }




}