package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.objectLookup.*;

import java.util.*;

public class RefValues extends NestedJsonArray<PluginRefInformation, PluginRefValue> {

    private static ObjectLookup.Deserializer<PluginRefValue>
            childDeserializer = new ObjectLookup.Deserializer<>(PluginRefValue.class);

    public static final String ARRAY_KEY = "value";

    public static RefValues wrapIfNeeded(PluginRefInformation parent,
                                         List<PluginRefValue> ref) {
        return wrapIfNeeded(parent, ref, RefValues.class);
    }

    public RefValues() { }

    public RefValues(List<PluginRefValue> ref) {
        super(ref);
    }

    @Override
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonProperty("value")
    public List<PluginRefValue> getValue() {
        return this.getList();
    }

    @JsonProperty("value")
    public void setValue(List<PluginRefValue> ref) {
        this.setList(ref);
    }


    public static class Deserializer extends DeserializerTemplate<RefValues, PluginRefValue> {
        @Override
        public JsonDeserializer<PluginRefValue> getChildDeserializer() {
            return childDeserializer;
        }

        @Override
        public RefValues getNewWrapperInstance() {
            return new RefValues();
        }
    }

    public static class Serializer extends SerializerTemplate<RefValues, PluginRefValue> { }
}
