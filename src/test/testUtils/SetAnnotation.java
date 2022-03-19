package testUtils;

import nessusTools.data.entity.template.*;

import java.util.*;

public class SetAnnotation {

    public static void jsonUseId(Class<? extends Pojo> pojoClass) {
        jsonIgnore(pojoClass, "id", true);
    }

    public static void jsonIgnoreId(Class<? extends Pojo> pojoClass) {
        jsonIgnore(pojoClass, "id", false);
    }

    public static void jsonIgnore(Class<? extends Pojo> pojoClass, Map<String, Boolean> fieldMap) {
        for (Map.Entry<String, Boolean> field : fieldMap.entrySet()) {
            Boolean value = field.getValue();
            if (value == null) {
                value = Boolean.FALSE;
            }
            jsonIgnore(pojoClass, field.getKey(), value);
        }
    }

    public static void jsonIgnore(Class<? extends Pojo> pojoClass, String fieldname, boolean jsonIgnore) {
        // TODO make this method
    }

}