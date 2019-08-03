import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class HashCodeUtilsTest {

    @Test
    public void collisionForPrefix() {
        Random random = new Random(42);
        for (int i = 0; i < 1000000; ++i) {
            byte[] prefixBytes = new byte[random.nextInt(20)];
            random.nextBytes(prefixBytes);
            String prefix = new String(prefixBytes, StandardCharsets.ISO_8859_1);
            int target = random.nextInt();

            String collision = HashCodeUtils.collisionForPrefix(prefix, target);
            assertEquals("Failed on i = " + i, target, collision.hashCode());
            assertThat(collision, CoreMatchers.startsWith(prefix));
        }
    }
}