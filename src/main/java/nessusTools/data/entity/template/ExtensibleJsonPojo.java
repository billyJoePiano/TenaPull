package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

// Abstract base class that implements extraJson behavior for all non-lookup pojos
// Note that 'implements DbPojo' interface is delayed until NaturalIdPojo and GeneratedIdPojo
// This was done so that NessusResponse can inherit the extraJson behaviors without needing to
// implement the DbPojo interface

@MappedSuperclass
public abstract class ExtensibleJsonPojo {

    @Column(name = "_extra_json")
    @Convert(converter = ExtraJson.Converter.class)
    @JsonIgnore // Jackson will use the wrapped map from _getExtraJsonMap() and the put() method for setting
    private ExtraJson extraJson;

    @JsonIgnore
    public ExtraJson getExtraJson() {
        return this.extraJson;
    }


    @JsonIgnore
    public void setExtraJson(ExtraJson extraJson) {
        this.extraJson = extraJson;
    }

    @Transient
    @JsonAnyGetter
    public Map<String, JsonNode> getExtraJsonMap() {
        if (this.extraJson != null) {
            return this.extraJson.getMap();

        } else {
            return null;
        }
    }

    @Transient
    @JsonAnySetter
    public void putExtraJson(String key, Object value) {
        if (this.extraJson == null) {
            this.extraJson = new ExtraJson();
        }
        JsonNode node;
        if (value instanceof JsonNode) {
            node = (JsonNode) value;
        } else {
            node = new ObjectMapper().convertValue(value, JsonNode.class);
        }
        this.extraJson.put(key, node);
    }


    public ObjectNode toJsonNode() {
        return new ObjectMapper().convertValue(this, ObjectNode.class);
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
}
