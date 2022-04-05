package nessusTools.web;

import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.template.*;

import java.util.*;

public interface FieldChecker {
    default public void checkFields(List<ExtensibleJsonPojo> entityList, List<String> fieldList, int baseline) {
        Set<String> extras = new LinkedHashSet<>();

        for (ExtensibleJsonPojo entity : entityList) {
            Map<String, JsonNode> extraJson = entity.getExtraJsonMap();
            if (extraJson == null) {
                continue;
            }

            for (String fieldName : extraJson.keySet()) {
                extras.add(fieldName);
            }
        }

        synchronized (fieldList) {
            for (int i = baseline; i < fieldList.size(); i++) {
                String fieldName = fieldList.get(i);
                if (!extras.contains(fieldName)) {
                    fieldList.remove(i);
                    i--;
                }
            }

            for (String fieldName : extras) {
                if (!fieldList.contains(fieldName)) {
                    fieldList.add(fieldName);
                }
            }
        }
    }
}
