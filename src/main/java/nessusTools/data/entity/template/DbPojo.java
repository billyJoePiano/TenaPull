package nessusTools.data.entity.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface DbPojo {
    public int getId();
    public void setId(int id);

    public JsonNode toJsonNode();
    public String toJsonString() throws JsonProcessingException;
    public void _prepare();
}
