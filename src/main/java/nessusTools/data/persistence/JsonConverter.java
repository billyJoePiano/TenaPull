package nessusTools.data.persistence;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.lang.reflect.*;
import java.util.*;

@Converter
public class JsonConverter implements AttributeConverter<Map<String, JsonNode>, String> {
    private static final Logger logger = LogManager.getLogger(JsonConverter.class);
    private static final TypeReference<Map<String, JsonNode>> typeRef
            = new TypeReference<Map<String, JsonNode>>() {};

    public static String escapeString(String str) {
        return "\"" + new String(JsonStringEncoder.getInstance().quoteAsString(str)) + "\"";
    }

    public static String escapeStringNoQuotes(String str) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(str));
    }

    @Override
    public String convertToDatabaseColumn(Map<String, JsonNode> json) {
        if (json == null) {
            return null;
        }

        try {
            return new ObjectMapper().writeValueAsString(json);

        } catch (JsonProcessingException e) {
            logger.error(e);

            return "{\n \"error\":"
                        + escapeString(this.getClass().toString() + " could not convert map to json")
                    + ",\n \"map.toString()\":"
                        + escapeString(json.toString())
                    + ",\n \"exception.getMessage()\":"
                        + escapeString(e.getMessage())
                    + ",\n \"exception.toString()\":"
                        + escapeString(e.toString())
                    + ",\n}";
        }
    }

    @Override
    public Map<String, JsonNode> convertToEntityAttribute(String json) {
        try {
            return new ObjectMapper().readValue(json, typeRef);

        } catch (JsonProcessingException e) {
            logger.error(e);
            return null;
        }
    }
}
