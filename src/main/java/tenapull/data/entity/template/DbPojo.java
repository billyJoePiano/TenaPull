package tenapull.data.entity.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public interface DbPojo {
    /**
     * Get the surrogate or natural primary key id for the lookup
     * @return the id
     */
    public int getId();

    /**
     * Set the surrogate or natural primary key id for the lookup
     * @param id the id
     */
    public void setId(int id);

    /**
     * Convert the pojo into a JsonNode
     * @return a JsonNode representing the serialization of this pojo
     */
    public JsonNode toJsonNode();

    /**
     * Convert the pojo into a Json string
     * @return a string representing the Json serialization of this pojo
     * @throws JsonProcessingException
     */
    public String toJsonString() throws JsonProcessingException;

    /**
     * Perform any operations necessary to prepare this pojo for insertion or updating
     * in the database
     */
    public void _prepare();
}
