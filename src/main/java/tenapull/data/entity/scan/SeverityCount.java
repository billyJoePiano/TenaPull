package tenapull.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.objectLookup.*;

import java.util.*;

/**
 * JSON container for the array of SeverityLevelCounts in a ScanHost
 */
public class SeverityCount extends NestedJsonArray<ScanHost, SeverityLevelCount> {

    /**
     * The key for the array of SeverityLevelCounts, as needed by the
     * NestedJsonArray abstract super class
     */
    public static final String ARRAY_KEY = "item";

    @Override
    @JsonIgnore
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    /**
     * Gets item.
     *
     * @return the item
     */
    @JsonGetter("item")
    @JsonSerialize(using = Lists.SortSerializer.class)
    public List<SeverityLevelCount> getItem() {
        return this.getList();
    }

    /**
     * Sets item.
     *
     * @param item the item
     */
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
