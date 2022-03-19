package nessusTools.data.entity.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

public interface Pojo {
    public int getId();
    public void setId(int id);

    public Map<String, JsonNode> _getExtraJson();
    public void _setExtraJson(Map<String, JsonNode> _extraJson);
    public void _putExtraJson(String key, Object value);

    public JsonNode toJsonNode();
    public String toJsonString() throws JsonProcessingException;
}
