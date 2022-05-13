package tenapull.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.scan.*;

import java.io.*;
import java.util.*;

/**
 * Serializer for simplifying the list of SeverityLevelCount beans into a series of single text values,
 * used for serializing the output.
 */
public class SeverityCountsReducer extends JsonSerializer<SeverityCount> {


    /**
     * Converts a SeverityCount's list of SeverityLevelCounts into a series of text fields,
     * with a key of "level_#" (where # represents the level value of the SeverityLevelCount)
     * and a value taken from the "count" field representing the SeverityLevelCount
     *
     * @param sc the SeverityCount wrapper which contains the list of SeverityLevelCounts
     * @param jg
     * @param sp
     * @throws IOException
     */
    @Override
    public void serialize(SeverityCount sc,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        if (sc == null) {
            jg.writeNull();
            return;
        }

        List<SeverityLevelCount> levels = sc.getList();
        Map<Integer, List<Integer>> map = new TreeMap<>();
        List<Integer> nullList = null; //TreeMap doesn't allow null key ... comparator throws exception
        List<SeverityLevelCount> other = null;

        for (SeverityLevelCount levelCount : levels) {
            if (levelCount == null) continue;
            Integer level = levelCount.getSeverityLevel();
            Integer count = levelCount.getCount();

            ExtraJson extraJson = levelCount.getExtraJson();
            if (extraJson != null) {
                other.add(levelCount);
            }

            if (level == null) {
                if (count == null) continue;
                if (nullList == null) {
                    nullList = new ArrayList(1);
                }
                nullList.add(count);
                continue;
            }

            List<Integer> list = map.computeIfAbsent(level, k -> new ArrayList<>(1));

            list.add(count);
        }

        for (Map.Entry<Integer, List<Integer>> entry : map.entrySet()) {
            Integer key = entry.getKey();
            List<Integer> values = entry.getValue();
            if (values.size() == 1) {
                jg.writeNumberField("level_" + key.toString(), values.get(0));

            } else {
                jg.writeFieldName("level_" + key.toString());
                jg.writeObject(values);
            }
        }

        if (nullList != null) {
            jg.writeFieldName("level_null");
            jg.writeObject(nullList);
        }

        if (other != null) {
            jg.writeFieldName("other");
            if (other.size() == 1) {
                jg.writeObject(other.get(0));

            } else {
                jg.writeObject(other);
            }
        }

        jg.writeEndObject();
    }
}