package nessusTools.util;

import java.util.*;

public class MakeMap {
    static public LinkedHashMap of(Object[] entries) {
        if (entries == null) return null;
        if (entries.length % 2 != 0) throw new IllegalArgumentException("MakeMap.of(entries) must have an even numbered length!");

        LinkedHashMap map = new LinkedHashMap();

        for (int i = 0; i < entries.length; i++) {
            map.put(entries[i], entries[++i]);
        }

        return map;
    }
}
