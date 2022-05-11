package nessusTools.data.persistence;

import nessusTools.data.entity.objectLookup.*;
import org.apache.logging.log4j.*;

import javax.persistence.*;
import java.nio.charset.*;
import java.security.*;
import java.util.*;

/**
 * Represents a "lazy" or "dirty" SHA-512 hash of a string lookup or object lookup.
 * This is referred to as "lazy/drity" because if the string being hashed is less than 64
 * bytes, then the string itself is used in place of the hash.  This still guarantees
 * the uniqueness of the hash, while saving the need to calculate the SHA-512 hash
 * when it would be unnecessary
 *
 *
 */
public class Hash implements Comparable<Hash> {
    private static Logger logger = LogManager.getLogger(Hash.class);
    /**
     * The algorithm which we are using -- SHA-512
     */
    public static final String ALGORITHM = "SHA-512";
    /**
     * The size of a SHA-512 hash -- 64 bytes.
     */
    public static final int HASH_SIZE = 64;

    static {
        makeDigest(); //confirm SHA-512 is available
    }

    private static MessageDigest makeDigest() {
        try {
            return MessageDigest.getInstance(ALGORITHM);

        } catch (NoSuchAlgorithmException e) {
            logger.error(e);
            throw new IllegalStateException(e);
        }
    }


    private final byte[] bytes;

    /**
     * Instantiates a new Hash using the given array of bytes
     *
     * @param bytes the bytes
     */
    public Hash(byte[] bytes) {
        if (bytes == null) throw new NullPointerException();
        this.bytes = bytes.clone();
    }

    /**
     * Instantiates a new Hash, by making a "lazy hash" the given string
     *
     * @param str the str
     */
    public Hash(String str) {
        if (str == null) throw new NullPointerException();
        this.bytes = lazyHash(str);
    }

    /**
     * Lazily hashes the provided string into an array bytes
     *
     * @param str the str
     * @return the byte [ ]
     */
    public static byte[] lazyHash(String str) {
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

    public String toString() {
        return "[Hash] 0x" + hexString(this.bytes);
    }

    /**
     * Converts an array of bytes into a hexadecimal encoded string
     *
     * @param bytes the bytes
     * @return the string
     */
    public static String hexString(byte[] bytes) {
        StringBuilder hash = new StringBuilder();
        for (byte bt : bytes) {
            String hex = Integer.toString(bt & 0xFF, 16);
            while (hex.length() < 2) {
                hex = "0" + hex;
            }
            hash.append(hex);
        }
        return hash.toString();
    }

    /**
     * Lazily hashes the provided string and returns the resulting
     * hash as a hexadecimal encoded
     *
     * @param str the str
     * @return the string
     */
    public static String hexString(String str) {
        byte[] bytes = lazyHash(str);
        return hexString(bytes);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }

        return Hash.equals(this.bytes, ((Hash)o).bytes);
    }

    private Integer javaHashCode = null;

    @Override
    public int hashCode() {
        if (javaHashCode != null) return javaHashCode;
        else return this.javaHashCode = hashCode(this.bytes);
    }

    /**
     * Generates a unique java hashCode from the bytes array in the provided
     * string
     *
     * @param str the str
     * @return the int
     */
    public static int hashCode(String str) {
        if (str == null) return 0;
        return hashCode(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a unique java hashCode from the provided bytes array
     * by applying a cumulative bitwise XOR operation on each sequential
     * set of 4 bytes
     *
     * @param bytes the bytes
     * @return the int
     */
    public static int hashCode(byte[] bytes) {
        if (bytes == null) return 0;
        int result = 0;
        for (int i = 0; i < bytes.length; i += 4) {
            int mask = 0;
            int endIndex = Math.min(bytes.length - i, 4);
            for (int j = 0; j < endIndex; j++) {
                int b = 0xFF & bytes[j + i];
                b <<= j * 8;
                mask |= b;
            }

            result ^= mask;
        }
        return result;
    }

    /**
     * Determines if two byte arrays are equivalent
     *
     * @param mine   byte array1
     * @param theirs byte array2
     * @return the boolean
     */
    public static boolean equals(byte[] mine, byte[] theirs) {
        if (mine == null) {
            return theirs == null;

        } else if (theirs == null || theirs.length != mine.length) {
            return false;

        } else if (mine == theirs) {
            return true;
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

    /**
     * Returns a comparison of two byte arrays
     *
     * @param mine   the comparable whose compare method was called
     * @param theirs the comparable being compared against
     * @return the int
     */
    public static int compareTo(byte[] mine, byte[] theirs) {
        if (mine == null) {
            return theirs == null ? 0 : 1;
        } else if (theirs == null) {
            return -1;
        }
        return compareTo(mine, theirs);
    }

    private static int checkedCompareTo(byte[] mine, byte[] theirs) {
        if (mine == null) {
            return theirs == null ? 0 : 1;

        } else if (theirs == null) {
            return -1;

        } else if (theirs.length != mine.length) {
            return mine.length < theirs.length ? -1 : 1;
        }

        for (int i = 0; i < mine.length; i++) {
            if (mine[i] == theirs[i]) continue;

            //1's compliment ... need to consider negatives as *larger* than positives/zero
            if (mine[i] < 0) {
                if (theirs[i] >= 0) return 1;

            } else if (theirs[i] < 0) {
                return -1;
            }

            return mine[i] < theirs[i] ? -1 : 1;
        }
        return 0;
    }

    /**
     * Converts Hash instances back and forth between the database varbinary datatype
     * and the hash fields in the ORM.
     */
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
