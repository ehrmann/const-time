import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Demo {

    public static void main(String[] args) {

        HashMap<String, String> hashMap = new HashMap<>();

        hashMap.put("mysecret", "password");

        int size = 16;

        String[] x = new String[size];
        int remaining = 16;
        while (remaining > 0) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 16; ++i) {
                sb.append('a' + ThreadLocalRandom.current().nextInt(26));
                int sbHash = sb.toString().hashCode();
                int hash = sbHash ^ (sbHash >>> 16);
                if (x[hash & (size - 1)] == null) {
                    x[hash & (size - 1)] = sb.toString();
                    --remaining;
                    break;
                }
            }
        }

        long[] j = new long[size];

        boolean y = false;
        for (int i = 0; i < 1000000; ++i) {
            if (i == 20000) {
                j = new long[size];
            }
            int s = ThreadLocalRandom.current().nextInt(x.length);
            long start = System.nanoTime();
            y = y ^ hashMap.containsKey(x[s]);
            j[s] += System.nanoTime() - start;
        }

        System.out.println(Arrays.toString(j));
        System.out.printf("Min: %s%n", Arrays.stream(j).max());
        System.out.println(("mysecret".hashCode() ^ ("mysecret".hashCode() >>> 16)) & (size - 1));
    }


    // estimate size
    // which buckets have something
    // pick the key

    // algo
    // neat demo project; cracking hashmap?


}
