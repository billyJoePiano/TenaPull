package nessusData.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class IdNullable {
    private IdNullable() { }

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
            return jp.getValueAsInt(); //converts null to zero
        }
    }
}
