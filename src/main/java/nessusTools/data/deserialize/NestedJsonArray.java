package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import com.sun.istack.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.*;

import javax.persistence.*;
import java.util.*;

/**
 * Used to "wrap" an array of objects inside an intermediate JSON object, even though the original list
 * belongs to the parent object.  This class represents the intermediate object, which does not
 * represent any entity in the ORM, but which is needed for serialization/deserialization.  Any additional
 * properties of this object (as implemented by sub classes) besides the wrapped array will typically
 * be stored inside the parent object, which IS a part of the ORM.
 *
 * @param <P> The parent object of the ORM, which owns the list of child objects
 * @param <C> The child objects of the list, which are embedded inside the intermediate JSON wrapper represented
 *           by an implementation of this class
 */
@MappedSuperclass
public abstract class NestedJsonArray
                <P extends ExtensibleJsonPojo, C extends DbPojo> {

    /**
     * The key used for the ExtraJson map in the parent object, if there is any unexpected JSON in the
     * wrapper object.  This will be appended with the class name of the wrapper implementation
     */
    public static final String EXTRA_JSON_KEY_IN_PARENT = "__nestedArrayContainer_extraJsonFor_";

    /**
     * Default constructor
     */
    public NestedJsonArray() { }

    /**
     * The parent POJO object of the ORM, which owns the Child list and stores any additional values
     * embedded within this wrapper
     */
    @JsonIgnore
    P parent;

    /**
     * The list of child POJO objects owned by the parent in the ORM
     */
    @JsonIgnore
    private List<C> list;

    /**
     * An ObjectNode containing any extra json for this wrapper
     */
    @JsonIgnore
    private ObjectNode extraJson;

    /**
     * Method that must be implemented by concrete subclasses, providing the JSON key for the array this
     * object wraps.
     *
     * @return
     */
    @JsonIgnore
    public abstract String getArrayKey();

    /**
     * Method that must be implemented by concrete subclasses, which grabs the list of
     * child entities from the parent object
     *
     * @param parent the new parent object which owns the list of children
     * @return the list of children fetched from the parent
     */
    @JsonIgnore
    protected abstract List<C> getParentList(@NotNull P parent);

    /**
     * Method that must be implemented by concrete subclasses, which sets the list
     * of child entities in the parent object
     *
     * @param parent the parent object which owns the list of children
     * @param list the new list to set in the parent object
     */
    @JsonIgnore
    protected abstract void setParentList(@NotNull P parent, List<C> list);

    /**
     * Gets the parent POJO object
     *
     * @return the parent POJO object
     */
    public P getParent() {
        return this.parent;
    }

    /**
     * Type reference for a Map&gt;String, JsonNode&lt;, used for converting
     * a single JsonNode from the parent's ExtraJson Map into an ExtraJson map
     * for the wrapper's JsonAnyGetter during serialization
     *
     */
    private static final TypeReference<Map<String, JsonNode>>
            typeRef = new TypeReference<Map<String, JsonNode>>() { };

    /**
     * Converts the ObjectNode representing the wrapper's extra json (usually obtained from
     * the parent entity) into a Map for use by the serializer
     *
     * @return a Map of keys and JsonNodes, for extra json
     */
    @JsonAnyGetter
    public Map<String, JsonNode> getExtraJson() {
        if (this.extraJson == null) return null;
        return new ObjectMapper().convertValue(this.extraJson, typeRef);
    }

    /**
     * Gets the list of child entities
     * @return the list of child entities
     */
    protected List<C> getList() {
        return this.list;
    }

    /**
     * Sets the list of child entities, and also sets it in the parent if a parent has been set.  Includes
     * an initial check to prevent infinite recursion between the parent and wrapper setting each other's
     * lists.
     *
     * @param list the list of child entities
     */
    public void setList(List<C> list) {
        if (this.list == list) return; //prevent infinite recursion with parent
        this.list = list;
        if (this.parent != null) this.setParentList(this.parent, list);
    }

    /**
     * Puts an additional key-value pair into the ObjectNode representing the ExtraJson for this wrapper
     *
     * @param key
     * @param value
     */
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

    /**
     * Obtains stored ExtraJson from the parent, when reconstructing this wrapper from the DB/ORM
     *
     * @param parent the new parent entity to obtain the extra json from
     * @throws ClassCastException if the node returned from the parent is not an ObjectNode
     */
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

    /**
     * Take the extra json deserialized into this wrapper and puts it into the parent for
     * long-term persistence in the DB/ORM.
     *
     * @param parent
     */
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

    /**
     * When the parent is unset, the shared list of child objects must be de-coupled between
     * the parent and wrapper, so a modification in one list does not cause a modification in the other.
     */
    @JsonIgnore
    public void clearParent() {
        if (this.list != null && this.parent != null) {
            this.list = new ArrayList(this.list);
        }
        this.parent = null;
    }

    /**
     * A utility for the parent entity to invoke when providing its ExtraJson map to
     * a serializer.  This method makes a copy of the parent's map and removes the wrapper's
     * extra json so it doesn't end up being serialized in the parent also.
     *
     * @return a copy of the parent's extra json map with the wrapper's extra json removed
     */
    @JsonIgnore
    public Map<String, JsonNode> jsonAnyGetterForParent() {
        ExtraJson extraJson = this.parent.getExtraJson();
        if (extraJson == null) return null;
        Map<String, JsonNode> map = extraJson.getValue().makeCopy();
        map.remove(EXTRA_JSON_KEY_IN_PARENT + this.getClass().getSimpleName());
        return map;
    }

    /**
     * Double checks that the extra json between the wrapper and parents are correctly
     * synchronized
     *
     * @param key
     * @param value
     */
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
