package nessusTools.data.persistence;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import net.bytebuddy.dynamic.scaffold.*;
import org.apache.logging.log4j.*;

import javax.json.*;
import javax.persistence.*;
import java.util.*;

public class ExtraJson {
    private static final Logger logger = LogManager.getLogger(ExtraJson.class);

    public static String escapeString(String str) {
        return "\"" + new String(JsonStringEncoder.getInstance().quoteAsString(str)) + "\"";
    }

    public static String escapeStringNoQuotes(String str) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(str));
    }


    private final Map<String, JsonNode> map;
    private Map<String, JsonNode> immutable = null;

    public ExtraJson() {
        this.map = new LinkedHashMap();
    }

    public ExtraJson(Map<String, JsonNode> map)
            throws IllegalArgumentException {

        if (map == null) {
            throw new IllegalArgumentException(
                    "ExtraJson(map) constructor cannot pass null map.  "
                            + "Set the DbPojo's extraJson property to null instead");
        }
        this.map = map;
    }

    public ExtraJson(String json)
            throws JsonProcessingException, IllegalArgumentException {

        this();
        if (json == null) {
            throw new IllegalArgumentException(
                    "ExtraJson(String json) cannot pass null json string.  "
                    + "Set the DbPojo's extraJson property to null instead");
        }

        JsonNode top = new ObjectMapper().readValue(json, JsonNode.class);
        if (!(top instanceof ObjectNode)) {
            throw new IllegalArgumentException(
                    "Could not convert top-level node to ObjectNode:\n" + json);
        }

        Iterator<String> iterator = top.fieldNames();
        while (iterator.hasNext()) {
            String key = iterator.next();
            this.map.put(key, top.get(key));
        }
    }

    public Map<String, JsonNode> getMap() {
        if (this.immutable == null) {
            this.immutable = Collections.unmodifiableMap(this.map);
        }
        return this.immutable;
    }

    public JsonNode put(String key, JsonNode value) {
        return map.put(key, value);
    }

    public JsonNode get(String key) {
        return map.get(key);
    }

    public String toString() {
        if (map == null) {
            return null;
        }

        try {
            return new ObjectMapper().writeValueAsString(map);

        } catch (JsonProcessingException e) {
            logger.error(e);

            return "{\n \"error\":"
                    + escapeString(this.getClass().toString() + " could not convert map to json")
                    + ",\n \"map.toString()\":"
                    + escapeString(map.toString())
                    + ",\n \"exception.getMessage()\":"
                    + escapeString(e.getMessage())
                    + ",\n \"exception.toString()\":"
                    + escapeString(e.toString())
                    + ",\n}";
        }
    }


    @javax.persistence.Converter
    public static class Converter implements AttributeConverter<ExtraJson, String> {
        private static final Logger logger = LogManager.getLogger(Converter.class);

        @Override
        public String convertToDatabaseColumn(ExtraJson json) {
            if (json != null) {
                return json.toString();

            } else {
                return null;
            }

        }

        @Override
        public ExtraJson convertToEntityAttribute(String json) {
            if (json == null) {
                return null;
            }

            try {
                return new ExtraJson(json);

            } catch (Throwable e) {
                logger.error(e);
                return null;
            }
        }
    }
}
