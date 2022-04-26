package nessusTools.data.deserialize;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.entity.objectLookup.*;

import java.util.*;

public class RefInformation extends NestedJsonArray<PluginAttributes, PluginRefInformation> {
    private static ObjectLookup.Deserializer<PluginRefInformation>
            childDeserializer = new ObjectLookup.Deserializer<>(PluginRefInformation.class);

    public static final String ARRAY_KEY = "ref";

    public static RefInformation wrapIfNeeded(PluginAttributes parent,
                                              List<PluginRefInformation> ref) {
        return wrapIfNeeded(parent, ref, RefInformation.class);
    }

    public RefInformation() { }

    public RefInformation(List<PluginRefInformation> ref) {
        super(ref);
    }

    @Override
    public String getArrayKey() {
        return ARRAY_KEY;
    }

    @JsonProperty("ref")
    public List<PluginRefInformation> getRef() {
        return this.getList();
    }

    @JsonProperty("ref")
    public void setRef(List<PluginRefInformation> ref) {
        this.setList(ref);
    }


    public static class Deserializer extends DeserializerTemplate<RefInformation, PluginRefInformation> {
        @Override
        public JsonDeserializer<PluginRefInformation> getChildDeserializer() {
            return childDeserializer;
        }

        @Override
        public RefInformation getNewWrapperInstance() {
            return new RefInformation();
        }
    }

    public static class Serializer extends SerializerTemplate<RefInformation, PluginRefInformation> { }
}
