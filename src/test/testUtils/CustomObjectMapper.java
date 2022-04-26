package testUtils;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.*;
import com.fasterxml.jackson.databind.node.*;
import com.fasterxml.jackson.databind.ser.*;
import com.fasterxml.jackson.databind.ser.impl.*;

import nessusTools.data.entity.response.*;

/*
Adds special Jackson behavior for certain unit tests, to serialize/deserialize the id
number of NessusResponse implementations when the value is non-null.  This is
needed as a substitute for the NessusClient's handling of the id setting
during normal application run-time.  The id can be included in the JSON of the
test parameters, at the top of the root object.
 */
public class CustomObjectMapper extends ObjectMapper {
    PropertyFilter skipNullId = new SkipNullId();
    SimpleFilterProvider filterProvider = new SimpleFilterProvider();

    public CustomObjectMapper() {
        super();
        this.addMixIn(NessusResponse.class, SkipNullIdMixIn.class);

        filterProvider.addFilter("SkipNullId", this.skipNullId);
        this.setFilterProvider(this.filterProvider);
    }


    //Jackson MixIn -- Overrides @JsonIgnore for NessusResponse class, and adds the JsonFilter
    @JsonFilter("SkipNullId")
    public abstract class SkipNullIdMixIn {
        @JsonIgnore(false)
        private Integer id;
    }

    public class SkipNullId implements PropertyFilter {
        private SkipNullId() { }

        @Override
        public void serializeAsField(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, PropertyWriter propertyWriter) throws Exception {
            NessusResponse n = (NessusResponse) o;
            //TODO had to comment out below...
            //if (Objects.equals(propertyWriter.getName(), "id") && n.getId() == null) return;
            propertyWriter.serializeAsField(o, jsonGenerator, serializerProvider);
        }

        @Override
        public void serializeAsElement(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, PropertyWriter propertyWriter) throws Exception {
            NessusResponse n = (NessusResponse) o;
            //TODO had to comment out below...
            //if (Objects.equals(propertyWriter.getName(), "id") && n.getId() == null) return;
            propertyWriter.serializeAsField(o, jsonGenerator, serializerProvider);
        }

        @Override
        public void depositSchemaProperty(PropertyWriter propertyWriter, ObjectNode objectNode, SerializerProvider serializerProvider) throws JsonMappingException {
            // Deprecated version
        }

        @Override
        public void depositSchemaProperty(PropertyWriter propertyWriter, JsonObjectFormatVisitor jsonObjectFormatVisitor, SerializerProvider serializerProvider) throws JsonMappingException {

        }
    }
}