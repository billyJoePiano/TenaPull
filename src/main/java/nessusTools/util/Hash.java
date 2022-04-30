package nessusTools.util;

import org.apache.logging.log4j.*;

import java.nio.charset.*;
import java.security.*;

public class Hash {
    private static Logger logger = LogManager.getLogger(Hash.class);

    private Hash() { }

    static {
        makeDigest();
    }

    private static MessageDigest makeDigest() {
        try {
            return MessageDigest.getInstance("SHA-512");

        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }

    // https://stackoverflow.com/questions/33085493/how-to-hash-a-password-with-sha-512-in-java
    public static byte[] Sha512(String str) {
        return makeDigest().digest(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String Sha512HexString(String str) {
        byte[] bytes = Sha512(str);
        StringBuilder hash = new StringBuilder();
        for (byte bt : bytes) {
            String hex = Integer.toString(bt, 16);
            while (hex.length() < 2) {
                hex = "0" + hex;
            }
            hash.append(hex);
        }
        return hash.toString();
    }

    public static boolean equals(byte[] mine, byte[] theirs) {
        if (mine == null) {
            return theirs == null;

        } else if (theirs == null || theirs.length != mine.length) {
            return false;
        }

        for (int i = 0; i < mine.length; i++) {
            if (mine[i] != theirs[i]) return false;
        }
        return true;
    }
}
