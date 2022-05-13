package tenapull.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import tenapull.data.deserialize.*;
import tenapull.data.entity.response.*;

import java.util.*;

/**
 * Represents a prioritization object returned by the Nessus API in /scans/&lt;scan-id&gt;,
 * which contains the list of plugins.  This is just a wrapper for the purposes of serialization /
 * deserialization, and is not an actual DB/ORM entity.  It is linked to a parent ScanResponse
 * which persists the fields contained in this wrapper
 */
public class ScanPrioritization extends NestedJsonArray<ScanResponse, ScanPlugin> {
    /**
     * The key for the array of plugins, as needed by the NestJsonArray abstract super class
     */
    public static final String ARRAY_KEY = "plugins";

    @Override
    @JsonIgnore
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonProperty("threat_level")
    private Integer threatLevel;

    @Override
    @JsonIgnore
    protected List<ScanPlugin> getParentList(ScanResponse parent) {
        return parent.getPlugins();
    }

    @Override
    @JsonIgnore
    protected void setParentList(ScanResponse parent, List<ScanPlugin> list) {
        parent.setPlugins(list);
    }

    /**
     * Gets plugins.
     *
     * @return the plugins
     */
    @JsonGetter("plugins")
    public List<ScanPlugin> getPlugins() {
        return this.getList();
    }

    /**
     * Sets plugins.
     *
     * @param item the item
     */
    @JsonSetter("plugins")
    public void setPlugins(List<ScanPlugin> item) {
        this.setList(item);
    }

    /**
     * Gets threat level.
     *
     * @return the threat level
     */
    public Integer getThreatLevel() {
        return this.threatLevel;
    }

    /**
     * Sets threat level.
     *
     * @param threatLevel the threat level
     */
    public void setThreatLevel(Integer threatLevel) {
        if (this.threatLevel == threatLevel) return;
        this.threatLevel = threatLevel;
        ScanResponse parent = this.getParent();
        if (parent != null) parent.setThreatLevel(threatLevel);
    }

    /**
     * First invokes the super class's takeFieldsFromParent, then
     * also takes the persisted threat level from the parent ScanResponse
     *
     * @param parent the new parent entity to obtain the extra json from
     */
    @Override
    @JsonIgnore
    public void takeFieldsFromParent(ScanResponse parent) {
        super.takeFieldsFromParent(parent);
        if (parent != null) this.threatLevel = parent.getThreatLevel();
    }

    /**
     * First invokes the super class's putFieldsIntoParent, then
     * also puts the deserialized threatLevel field into the parent
     *
     * @param parent
     */
    @Override
    @JsonIgnore
    public void putFieldsIntoParent(ScanResponse parent) {
        super.putFieldsIntoParent(parent);
        if (parent != null) parent.setThreatLevel(this.threatLevel);
    }
}
