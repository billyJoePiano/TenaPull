package nessusData.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nessusData.entity.LookupPojo;

import java.io.IOException;

public class LookupSerializer extends JsonSerializer<LookupPojo> {
    // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
    @Override
    public void serialize(LookupPojo pojo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(pojo != null ? pojo.toString() : null);
    }
}