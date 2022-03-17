package nessusData.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;


public class NullableId {
    private NullableId() { }

    public static class Serializer extends JsonSerializer<Integer> {
        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
        @Override
        public void serialize(Integer id,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException {

            int primitive;
            if (id == null || (primitive = id.intValue()) == 0) {
                jsonGenerator.writeNull();

            } else {
                jsonGenerator.writeNumber(primitive);
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<Integer> {

        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            return jp.getValueAsInt();
        }

        @Override
        public Integer getNullValue(DeserializationContext ctxt) {
            return 0;
        }

    }
}


/*
public class IdNullable {
    private IdNullable() { }

    // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer

    public static class Serializer<POJO extends NaturalIdPojo>
            extends JsonSerializer<POJO> {
        @Override
        public void serialize(POJO pojo,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException {

            if (pojo == null) {
                jsonGenerator.writeNull();
                return;

            } else if (pojo.getId() != 0) {
                jsonGenerator.writeObject(pojo);
                return;

            }

            jsonGenerator.writeEm


        }
    }

    public static class Deserializer<POJO extends NaturalIdPojo>
            extends AbstractContextualDeserializer<POJO, Dao<POJO>> {

        private static Logger logger = LogManager.getLogger(Deserializer.class);
        @Override
        protected Logger getLogger() {
            return logger;
        }

        @Override
        public Integer deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {
            return jp.getValueAsInt(); //converts null to zero
        }
    }
}
*/