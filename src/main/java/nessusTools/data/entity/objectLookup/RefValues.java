package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;

import java.util.*;

public class RefValues extends NestedJsonArray<PluginRefInformation, PluginRefValue> {

    public static final String ARRAY_KEY = "value";

    public RefValues() { }

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

    @JsonProperty("value")
    public List<PluginRefValue> getValue() {
        return this.getList();
    }

    @JsonProperty("value")
    public void setValue(List<PluginRefValue> ref) {
        this.setList(ref);
    }

}
