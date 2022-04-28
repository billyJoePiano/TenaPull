package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.sun.istack.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.json.*;
import javax.persistence.*;
import java.io.*;
import java.util.*;

// P parent , C child
@MappedSuperclass
public abstract class NestedJsonArray
                <P extends ExtensibleJsonPojo, C extends DbPojo> {

    public static final String EXTRA_JSON_KEY_IN_PARENT = "__nestedArrayContainer_extraJsonFor_";

    public NestedJsonArray() { }

    @JsonIgnore
    P parent;

    @JsonIgnore
    private List<C> list;

    @JsonIgnore
    private ObjectNode extraJson;

    @JsonIgnore
    public abstract String getArrayKey();

    @JsonIgnore
    protected abstract List<C> getParentList(@NotNull P parent);

    @JsonIgnore
    protected abstract void setParentList(@NotNull P parent, List<C> list);

    public P getParent() {
        return this.parent;
    }

    private static final TypeReference<Map<String, JsonNode>>
            typeRef = new TypeReference<Map<String, JsonNode>>() { };

    @JsonAnyGetter
    public Map<String, JsonNode> getExtraJson() {
        if (this.extraJson == null) return null;
        return new ObjectMapper().convertValue(this.extraJson, typeRef);
    }

    protected List<C> getList() {
        return this.list;
    }

    public void setList(List<C> list) {
        if (this.list == list) return; //prevent infinite recursion with parent
        this.list = list;
        if (this.parent != null) this.setParentList(this.parent, list);
    }

    @JsonAnySetter
    public void putExtraJson(String key, Object value) {
        if (this.extraJson == null) {
            this.extraJson = new ObjectNode(JsonNodeFactory.instance);
            if (this.parent != null) {
                parent.putExtraJson(EXTRA_JSON_KEY_IN_PARENT
                            + this.getClass().getSimpleName(),
                        this.extraJson);
            }
        }
        JsonNode node;
        if (value instanceof JsonNode) {
            node = (JsonNode) value;
        } else {
            node = new ObjectMapper().convertValue(value, JsonNode.class);
        }
        this.extraJson.set(key, node);
    }

    @JsonIgnore
    public void takeFieldsFromParent(P parent) throws ClassCastException {
        if (parent == null) {
            clearParent();
            return;
        }

        this.parent = parent;

        this.list = getParentList(parent);

        JsonNode node = parent.getExtraJson(EXTRA_JSON_KEY_IN_PARENT + this.getClass().getSimpleName());

        if (node instanceof ObjectNode) {
            this.extraJson = (ObjectNode)node;

        } else {
            this.extraJson = null;
        }
    }

    @JsonIgnore
    public void putFieldsIntoParent(P parent) {
        if (parent == null) {
            clearParent();
            return;
        }

        this.parent = parent;

        this.setParentList(parent, this.list);

        if (this.extraJson == null) return;
        parent.putExtraJson(EXTRA_JSON_KEY_IN_PARENT + this.getClass().getSimpleName(),
                this.extraJson);
    }

    @JsonIgnore
    public void clearParent() {
        if (this.list != null && this.parent != null) {
            this.list = new ArrayList(this.list);
        }
        this.parent = null;
    }

    @JsonIgnore
    public Map<String, JsonNode> jsonAnyGetterForParent() {
        ExtraJson extraJson = this.parent.getExtraJson();
        if (extraJson == null) return null;
        Map<String, JsonNode> map = new LinkedHashMap<>(extraJson.getMap());
        map.remove(EXTRA_JSON_KEY_IN_PARENT + this.getClass().getSimpleName());
        return map;
    }

    public void checkExtraJsonPut(String key, Object value) {
        if (this.extraJson != value
                && Objects.equals(EXTRA_JSON_KEY_IN_PARENT
                                + this.getClass().getSimpleName(), key)) {

            if (value instanceof ObjectNode) {
                this.extraJson = (ObjectNode)value;

            } else {
                this.extraJson = null;
            }
        }
    }
}
