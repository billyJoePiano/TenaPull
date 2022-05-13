package tenapull.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.deserialize.*;

import java.util.*;

/**
 * A JSON wrapper for the array of PluginRefInformation held by PluginAttributes.  This does
 * not represent an entity in the DB/ORM, but is needed for the purposes of serialization /
 * deserialization, to accurately reflect the structure of the Nessus API
 */
public class RefInformation extends NestedJsonArray<PluginAttributes, PluginRefInformation> {

    /**
     * The key for the array of PluginRefInformations
     */
    public static final String ARRAY_KEY = "ref";

    @Override
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    /**
     * Gets list of pluginRefInformations
     *
     * @return the ref
     */
    @JsonGetter("ref")
    public List<PluginRefInformation> getRef() {
        return this.getList();
    }

    /**
     * Sets list of pluginRefInformations
     *
     * @param ref the ref
     */
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
