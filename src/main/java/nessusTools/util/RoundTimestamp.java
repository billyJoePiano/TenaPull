package nessusTools.util;

import java.sql.*;

public class RoundTimestamp {
    public static boolean equals(Timestamp mine, Timestamp theirs) {
        if (mine == null) return theirs == null;
        else if (theirs == null) return false;

        return (long)Math.round((double)mine.getTime() / 1000) * 1000
                == (long)Math.round((double)theirs.getTime() / 1000) * 1000;
    }

}
