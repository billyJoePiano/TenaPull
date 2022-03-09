package nessusData.serialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import org.apache.logging.log4j.*;

import java.io.IOException;
import java.sql.Timestamp;

public class EpochTimestampSerializer extends JsonSerializer<Timestamp> {
    Logger logger = LogManager.getLogger(EpochTimestampSerializer.class);

    // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
    @Override
    public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (timestamp != null) {
            jsonGenerator.writeNumber(timestamp.getTime() / 1000);

        } else {
            jsonGenerator.writeNull();
        }
    }

}
