package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

import java.io.*;
import java.util.*;

public class ScanPrioritization extends NestedJsonArray<ScanResponse, ScanPlugin> {
    public static final String ARRAY_KEY = "plugins";

    @Override
    @JsonIgnore
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    public ScanPrioritization() { }

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

    @JsonGetter("plugins")
    public List<ScanPlugin> getPlugins() {
        return this.getList();
    }

    @JsonSetter("plugins")
    @JsonDeserialize(contentUsing = ResponseChildDeserializer.class)
    public void setPlugins(List<ScanPlugin> item) {
        this.setList(item);
    }

    public Integer getThreatLevel() {
        return this.threatLevel;
    }

    public void setThreatLevel(Integer threatLevel) {
        if (this.threatLevel == threatLevel) return;
        this.threatLevel = threatLevel;
        ScanResponse parent = this.getParent();
        if (parent != null) parent.setThreatLevel(threatLevel);
    }

    @Override
    @JsonIgnore
    public void takeFieldsFromParent(ScanResponse parent) {
        super.takeFieldsFromParent(parent);
        if (parent != null) this.threatLevel = parent.getThreatLevel();
    }

    @Override
    @JsonIgnore
    public void putFieldsIntoParent(ScanResponse parent) {
        super.putFieldsIntoParent(parent);
        if (parent != null) parent.setThreatLevel(this.threatLevel);
    }
}
