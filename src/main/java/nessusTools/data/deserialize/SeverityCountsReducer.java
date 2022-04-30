package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;

import javax.ws.rs.core.*;
import java.io.*;
import java.util.*;

public class SeverityCountsReducer extends JsonSerializer<SeverityCount> {


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
                jg.writeStringField(key.toString(), values.get(0).toString());

            } else {
                jg.writeFieldName(key.toString());
                jg.writeObject(values);
            }
        }

        if (nullList != null) {
            jg.writeFieldName("null");
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