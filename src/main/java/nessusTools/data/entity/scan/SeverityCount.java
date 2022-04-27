package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;

import java.util.*;

public class SeverityCount extends NestedJsonArray<ScanHost, SeverityLevelCount> {

    public static final String ARRAY_KEY = "item";

    public SeverityCount() { }

    @Override
    @JsonIgnore
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonGetter("item")
    @JsonSerialize(using = Lists.SortSerializer.class)
    public List<SeverityLevelCount> getItem() {
        return this.getList();
    }

    @JsonSetter("item")
    public void setItem(List<SeverityLevelCount> item) {
        this.setList(item);
    }

    @Override
    @JsonIgnore
    protected List<SeverityLevelCount> getParentList(ScanHost parent) {
        return parent.getSeverityCounts();
    }

    @Override
    @JsonIgnore
    protected void setParentList(ScanHost parent, List<SeverityLevelCount> list) {
        parent.setSeverityCounts(list);
    }
}
