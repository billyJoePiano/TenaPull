package tenapull.util;

import java.sql.*;

/**
 * A static utility for comparing java.sql.Timestamps by rounding to the nearest second.
 * MySQL rounds timestamps when it stores them, so in order to determine equality between
 * a persisted timestamp and a newly deserialized one, the deserialized timestamp needs to
 * be rounded to the nearest second
 */
public class RoundTimestamp {
    /**
     * Compare two timestamps by rounding them to the nearest second
     *
     * @param mine   the first timestamp to compare
     * @param theirs the second timestamp to compare
     * @return the boolean
     */
    public static boolean equals(Timestamp mine, Timestamp theirs) {
        if (mine == null) return theirs == null;
        else if (theirs == null) return false;

        return (long)Math.round((double)mine.getTime() / 1000) * 1000
                == (long)Math.round((double)theirs.getTime() / 1000) * 1000;
    }

}
