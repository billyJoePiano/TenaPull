package tenapull.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.data.entity.objectLookup.*;

import javax.persistence.*;
import java.util.*;

/**
 * Abstract base class that implements ExtraJson behavior for most pojos other than string lookups.
 * Note that 'implements DbPojo' interface is delayed until NaturalIdPojo and GeneratedIdPojo,
 * because they have different requirement for how to implement id.
 */
@MappedSuperclass
public abstract class ExtensibleJsonPojo {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            fetch = FetchType.EAGER)
    @JoinColumn(name = "_extra_json")
    @JsonIgnore // Jackson will use the wrapped map from getExtraJsonMap() and the put() method for setting
    private ExtraJson extraJson;

    /**
     * Gets extra json.
     *
     * @return the extra json
     */
    @JsonIgnore
    public ExtraJson getExtraJson() {
        return this.extraJson;
    }


    /**
     * Sets entire extra json value.
     *
     * @param extraJson the extra json
     */
    @JsonIgnore
    public void setExtraJson(ExtraJson extraJson) {
        this.extraJson = extraJson;
    }

    /**
     * Gets extra json map for serialization
     *
     * @return the extra json map
     */
    @Transient
    @JsonAnyGetter
    public Map<String, JsonNode> getExtraJsonMap() {
        if (this.extraJson != null) {
            return this.extraJson.getValue().getView();

        } else {
            return null;
        }
    }

    /**
     * Puts extra json during deserialization
     *
     * @param key   the key
     * @param value the value
     */
    @Transient
    @JsonAnySetter
    public void putExtraJson(String key, Object value) {
        if (this.extraJson == null) {
            this.extraJson = new ExtraJson();

        } else if (this.extraJson.getId() != 0) {
            this.extraJson = new ExtraJson(this.extraJson);
        }

        JsonNode node;
        if (value instanceof JsonNode) {
            node = (JsonNode) value;

        } else {
            node = new ObjectMapper().convertValue(value, JsonNode.class);
        }
        this.extraJson.put(key, node);
    }

    /**
     * Gets extra json for a specific key
     *
     * @param key the key
     * @return the extra json
     */
    @Transient
    @JsonIgnore
    public JsonNode getExtraJson(String key) {
        if (this.extraJson == null) return null;
        return this.extraJson.get(key);
    }

    /**
     * Convienence method for subclass implementations to invoke from their
     * _prepare() method (note the difference in number of underscores), which
     * performs DB preparations on the ExtraJson
     */
    protected void __prepare() {
        this.extraJson = ExtraJson.dao.getOrCreate(this.extraJson);
    }

    /**
     * Converts the pojo into a serialized JSON ObjectNode
     *
     * @return the object node
     */
    public ObjectNode toJsonNode() {
        return new ObjectMapper().valueToTree (this);
    }

    /**
     * Serializes the pojo into a JSON string
     *
     * @return the JSON string
     * @throws JsonProcessingException if there is json processing exception
     */
    public String toJsonString() throws JsonProcessingException {
        return this.toJsonNode().toString();
    }

    /**
     * Uses the toJsonString() method to convert to a JSON string, except
     * catches any JsonProcessingException and returns an error message instead
     *
     * @return the serialized JSON string, or an error message if serialization failed
     */
    public String toString() {
        try {
            return this.toJsonString();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for '"
                    + super.toString() + "' :\n"
                    + e.getMessage();
        }
    }
}
