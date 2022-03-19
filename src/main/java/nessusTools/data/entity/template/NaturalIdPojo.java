package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import nessusTools.data.persistence.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.*;

@MappedSuperclass
public abstract class NaturalIdPojo implements Pojo {

    @Id
    @NaturalId
    @JsonProperty
    private int id;

    /*
    @Convert(
            attributeName = "_extra_json",
            converter = JsonConverter.class
        )
     */
    @Transient
    @JsonIgnore
    private Map<String, JsonNode> _extraJson;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @JsonAnyGetter
    public Map<String, JsonNode> _getExtraJson() {
        return this._extraJson;
    }

    @JsonIgnore
    public void _setExtraJson(Map<String, JsonNode> _extraJson) {
        this._extraJson = _extraJson;
    }

    @Transient
    @JsonAnySetter
    public void _putExtraJson(String key, Object value) {
        if (this._extraJson == null) {
            this._extraJson = new HashMap();
        }
        this._extraJson.put(key, new ObjectMapper().convertValue(value, JsonNode.class));
    }


    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }

    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String toString() {
        try {
            return this.toJsonString();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for '"
                    + super.toString() + "' :\n"
                    + e.getMessage();
        }
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !o.getClass().equals(this.getClass())) return false;
        Pojo other = (Pojo) o;
        return this.toJsonNode().equals(other.toJsonNode());

    }
}
