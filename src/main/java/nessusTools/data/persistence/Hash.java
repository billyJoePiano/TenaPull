package nessusTools.data.persistence;

import nessusTools.data.entity.objectLookup.*;
import org.apache.logging.log4j.*;

import javax.persistence.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

public class Hash implements Comparable<Hash> {
    private static Logger logger = LogManager.getLogger(Hash.class);
    public static final int HASH_SIZE = 64;

    static {
        makeDigest(); //confirm SHA-512 is available
    }

    private static MessageDigest makeDigest() {
        try {
            return MessageDigest.getInstance("SHA-512");

        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }


    private final byte[] bytes;

    public Hash(byte[] bytes) {
        if (bytes == null) throw new NullPointerException();
        this.bytes = bytes.clone();
    }

    public Hash(String str) {
        if (str == null) throw new NullPointerException();
        this.bytes = dirtyHash(str);
    }

    public static byte[] dirtyHash(String str) {
        if (str == null) return null;
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < HASH_SIZE) {
            return bytes;
        }
        return makeDigest().digest(bytes);
    }


    /* //Benchmarked version below, for debugging / testing
    // benchmarks showed the "shortcut" was over 10x faster on average... ~4.3 microseconds vs ~65 microseconds
    // and was used about 1/4th of the time
    public static final double BILLION = 1000000000;

    // https://stackoverflow.com/questions/33085493/how-to-hash-a-password-with-sha-512-in-java
    public static byte[] dirtySha512(String str) {
        long start = System.nanoTime();
        Boolean status = null;
        try {
            byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            if (bytes.length < HASH_SIZE) {
                status = Boolean.TRUE;
                return bytes;
            }

            status = Boolean.FALSE;
            return makeDigest().digest(bytes);

        } finally {
            long end = System.nanoTime();
            double time = (end - start) / BILLION;
            if (status == null) {
                logger.info("HASH NULL: " + time);
                NULL.add(time);

            } else if (status) {
                logger.info("HASH SHORTCUT: " + time);
                TRUE.add(time);

            } else {
                logger.info(" HASH SHA-512: " + time);
                FALSE.add(time);
            }
        }
    }

    static List<Double> NULL = new LinkedList<>();
    static List<Double> TRUE = new LinkedList<>();
    static List<Double> FALSE = new LinkedList<>();


    public static void printAverages() {
        logger.info("HASH BENCHMARK AVERAGES");

        if (NULL.size() > 0) {
            double n = NULL.stream().reduce((a, b) -> a + b).get() / NULL.size();
            logger.info("HASH NULL Average: " + n + "  (out of " + NULL.size() + ")");

        }

        if (TRUE.size() > 0) {
            double n = TRUE.stream().reduce((a, b) -> a + b).get() / TRUE.size();
            logger.info("HASH SHORTCUT Average: " + n + "  (out of " + TRUE.size() + ")");

        }

        if (FALSE.size() > 0) {
            double n = FALSE.stream().reduce((a, b) -> a + b).get() / FALSE.size();
            logger.info(" HASH SHA-512 Average: " + n + "  (out of " + FALSE.size() + ")");

        }
    }
     */

    public static String hexString(String str) {
        byte[] bytes = dirtyHash(str);
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

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }

        return equals(this.bytes, ((Hash)o).bytes);
    }

    public static boolean equals(byte[] mine, byte[] theirs) {
        if (mine == null) {
            return theirs == null;

        } else if (theirs == null || theirs.length != mine.length) {
            return checkedCompareTo(mine, theirs) == 0;
        }

        for (int i = 0; i < mine.length; i++) {
            if (mine[i] != theirs[i]) return false;
        }
        return true;
    }

    public int compareTo(Hash other) {
        if (other == null) return -1;
        if (other == this) return 0;
        return checkedCompareTo(this.bytes, other.bytes);
    }

    public static int compareTo(byte[] mine, byte[] theirs) {
        if (mine == null) {
            return theirs == null ? 0 : 1;
        } else if (theirs == null) {
            return -1;
        }
        return compareTo(mine, theirs);
    }

    private static int checkedCompareTo(byte[] t, byte[] o) {
        int diff = t.length - o.length;
        int i = 0, j = 0;

        if (diff > 0) {
            for (; i < diff; i++) {
                if (t[i] != 0) return 1;
            }

        } else if (diff < 0) {
            diff = -diff;
            for (; j < diff; j++) {
                if (o[j] != 0) return -1;
            }
        }

        for (; i < t.length; i++, j++) {
            if (t[i] == o[j]) continue;
            //1's compliment ... need to consider negatives as *larger* than positives
            if (t[i] < 0) {
                if (o[j] >= 0) return 1;

            } else if (o[j] < 0) {
                return -1;
            }

            return t[i] < o[j] ? -1 : 1;
        }
        return 0;
    }

    @javax.persistence.Converter
    public static class Converter implements AttributeConverter<Hash, byte[]> {
        private static final Logger logger = LogManager.getLogger(ExtraJson.Converter.class);

        @Override
        public byte[] convertToDatabaseColumn(Hash hash) {
            if (hash == null) return null;
            return hash.bytes.clone();
        }

        @Override
        public Hash convertToEntityAttribute(byte[] bytes) {
            if (bytes == null) return null;
            return new Hash(bytes);
        }
    }
}
