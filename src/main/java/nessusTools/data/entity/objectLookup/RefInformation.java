package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;

import java.util.*;

public class RefInformation extends NestedJsonArray<PluginAttributes, PluginRefInformation> {

    public static final String ARRAY_KEY = "ref";

    public RefInformation() { }

    @Override
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonGetter("ref")
    public List<PluginRefInformation> getRef() {
        return this.getList();
    }

    @JsonSetter("ref")
    public void setRef(List<PluginRefInformation> ref) {
        this.setList(ref);
    }

    @Override
    @JsonIgnore
    protected List<PluginRefInformation> getParentList(PluginAttributes parent) {
        return parent.getRefInformation();
    }

    @Override
    @JsonIgnore
    protected void setParentList(PluginAttributes parent, List<PluginRefInformation> list) {
        parent.setRefInformation(list);
    }
}
