import org.junit.Test;

import java.util.HashMap;

public class HashMapUtilsTest {

    @Test
    public void findSize() {
        HashMap<String, String> passwords = new HashMap<>();
        passwords.put("alice", "123");
        passwords.put("bob", "abc");
        passwords.put("carol", "1334");
        passwords.put("dan", "1334");
        passwords.put("frank", "1334");

        // len = 16, [0, 1, 4, 9]

        HashMapUtils.findSize(passwords);
    }
}