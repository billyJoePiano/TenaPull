package nessusData.entity.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface Pojo {
    public int getId();
    public void setId(int id);

    public JsonNode toJsonNode();
    public String toJsonString() throws JsonProcessingException;
}
