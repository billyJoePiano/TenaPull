package tenapull.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.lookup.*;

import java.util.*;

/**
 * A JSON wrapper for the array of PluginRefValues held by PluginRefInformation.  This does
 * not represent an entity in the DB/ORM, but is needed for the purposes of serialization /
 * deserialization, to accurately reflect the structure of the Nessus API
 */
public class RefValues extends NestedJsonArray<PluginRefInformation, PluginRefValue> {

    /**
     * The key for the array of PluginRefValues
     */
    public static final String ARRAY_KEY = "value";

    @Override
    @JsonIgnore
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @Override
    protected List<PluginRefValue> getParentList(PluginRefInformation parent) {
        return parent.getValues();
    }

    @Override
    protected void setParentList(PluginRefInformation parent, List<PluginRefValue> list) {
        parent.setValues(list);
    }

    /**
     * Gets the list of values
     *
     * @return the value
     */
    @JsonProperty("value")
    public List<PluginRefValue> getValue() {
        return this.getList();
    }

    /**
     * Sets list of values
     *
     * @param value the value
     */
    @JsonProperty("value")
    public void setValue(List<PluginRefValue> value) {
        this.setList(value);
    }

}
