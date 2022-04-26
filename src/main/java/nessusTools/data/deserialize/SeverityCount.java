package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.scan.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class SeverityCount extends NestedJsonArray<ScanHost, SeverityLevelCount> {
    private static ObjectLookup.Deserializer<SeverityLevelCount>
            childDeserializer = new ObjectLookup.Deserializer<SeverityLevelCount>(SeverityLevelCount.class);

    public static final String ARRAY_KEY = "item";

    public static SeverityCount wrapIfNeeded(ScanHost parent, List<SeverityLevelCount> item) {
        return NestedJsonArray.wrapIfNeeded(parent, item, SeverityCount.class);
    }

    public SeverityCount() { }

    public SeverityCount(List<SeverityLevelCount> item) {
        super(item);
    }

    @Override
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonProperty("item")
    public List<SeverityLevelCount> getItem() {
        return this.getList();
    }

    @JsonProperty("item")
    public void setItem(List<SeverityLevelCount> item) {
        this.setList(item);
    }


    public static class Deserializer extends DeserializerTemplate<SeverityCount, SeverityLevelCount> {
        @Override
        public JsonDeserializer<SeverityLevelCount> getChildDeserializer() {
            return childDeserializer;
        }

        @Override
        public SeverityCount getNewWrapperInstance() {
            return new SeverityCount();
        }
    }

    public static class Serializer extends SerializerTemplate<SeverityCount, SeverityLevelCount> { }

    @Override
    public void setList(List<SeverityLevelCount> list) {
        Collections.sort(list);
        super.setList(list);
    }

    @Override
    @JsonIgnore
    public boolean add(SeverityLevelCount child) {
        boolean result = super.add(child);
        Collections.sort(this.getList());
        return result;
    }

    @Override
    @JsonIgnore
    public boolean addAll(@NotNull Collection<? extends SeverityLevelCount> c) {
        boolean result = super.addAll(c);
        Collections.sort(this.getList());
        return result;
    }

    @Override
    @JsonIgnore
    public boolean addAll(int index, @NotNull Collection<? extends SeverityLevelCount> c) {
        boolean result = super.addAll(index, c);
        Collections.sort(this.getList());
        return result;
    }

    @Override
    @JsonIgnore
    public SeverityLevelCount set(int index, SeverityLevelCount element) {
        SeverityLevelCount result = super.set(index, element);
        Collections.sort(this.getList());
        return result;
    }

    @Override
    @JsonIgnore
    public void add(int index, SeverityLevelCount element) {
        super.add(index, element);
        Collections.sort(this.getList());
    }
}
