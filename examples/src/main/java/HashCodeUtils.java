public final class HashCodeUtils {

    public static String collisionForPrefix(String prefix, int target) {
        StringBuilder sb = new StringBuilder(prefix);

        for (int i = 0; i < 80; ++i) {
            sb.append((char) 0);
            if (i >= 10 && (target - sb.toString().hashCode() >= 0 || sb.toString().hashCode() == 0)) {
                break;
            }
        }

        for (int i = 0; i < sb.length() - prefix.length(); ++i) {
            int j = 0;
            // int diff = target - sb.toString().hashCode()
            // sb.toString().hashCode() < target && j
            while (target - sb.toString().hashCode() > 0 && j < 31) {
                sb.setCharAt(prefix.length() + i, (char) j);
                ++j;
            }

            // sb.toString().hashCode() > target
            if (target - sb.toString().hashCode() < 0) {
                sb.setCharAt(prefix.length() + i, (char) (sb.charAt(prefix.length() + i) - 1));
            }

            // System.out.printf("i = %d, diff = %d%n", i, target - sb.toString().hashCode());
        }

        return sb.toString();
    }

}
