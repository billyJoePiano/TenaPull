package nessusData.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import nessusData.entity.Pojo;

import java.io.IOException;

public class IdRefSerializer extends JsonSerializer<Pojo> {
    // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
    @Override
    public void serialize(Pojo pojo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (pojo != null) {
            jsonGenerator.writeNumber(pojo.getId());

        } else {
            jsonGenerator.writeNull();
        }
    }
}