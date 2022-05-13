package tenapull.util;

import java.util.*;

/**
 * Static utility for making a map from an array of Objects.  Used as a substitute for
 * Map.of() since that throws exceptions if any of the arguments are null
 */
public class MakeMap {
    /**
     * Creates a map from the passed array of Objects {key1, value1, key2, value2, etc...}
     *
     * @param entries key-value pairs to construct the map with
     * @return the constructed map
     * @throws IllegalArgumentException if the entries array has an odd-numbered length
     */
    static public LinkedHashMap of(Object[] entries) throws IllegalArgumentException {
        if (entries == null) return null;
        if (entries.length % 2 != 0) throw new IllegalArgumentException("MakeMap.of(entries) must have an even numbered length!");

        LinkedHashMap map = new LinkedHashMap();

        for (int i = 0; i < entries.length; i++) {
            map.put(entries[i], entries[++i]);
        }

        return map;
    }
}
