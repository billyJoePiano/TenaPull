package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.objectLookup.*;

import javax.persistence.*;
import java.util.*;

// Abstract base class that implements extraJson behavior for all non-lookup pojos
// Note that 'implements DbPojo' interface is delayed until NaturalIdPojo and GeneratedIdPojo
// This was done so that NessusResponse can inherit the extraJson behaviors without needing to
// implement the DbPojo interface

@MappedSuperclass
public abstract class ExtensibleJsonPojo {

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH},
            fetch = FetchType.EAGER)
    @JoinColumn(name = "_extra_json")
    @JsonIgnore // Jackson will use the wrapped map from getExtraJsonMap() and the put() method for setting
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
            return this.extraJson.getValue().getView();

        } else {
            return null;
        }
    }

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

    @Transient
    @JsonIgnore
    public JsonNode getExtraJson(String key) {
        if (this.extraJson == null) return null;
        return this.extraJson.get(key);
    }

    protected void __prepare() {
        this.extraJson = ExtraJson.dao.getOrCreate(this.extraJson);
    }

    public ObjectNode toJsonNode() {
        return new ObjectMapper().valueToTree (this);
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
