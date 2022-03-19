package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Timestamp;

public class EpochTimestamp {
    public static class Deserializer extends JsonDeserializer<Timestamp> {
        Logger logger = LogManager.getLogger(Deserializer.class);

        @Override
        public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String timestamp = jp.getText().trim();

            try {
                return new Timestamp(Long.valueOf(timestamp + "000"));

            } catch (NumberFormatException e) {
                logger.error("Unable to deserialize timestamp: " + timestamp, e);
                return null;
            }
        }

    }

    public static class Serializer extends JsonSerializer<Timestamp> {
        Logger logger = LogManager.getLogger(Serializer.class);

        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
        @Override
        public void serialize(Timestamp timestamp, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (timestamp != null) {
                jsonGenerator.writeNumber((int)(timestamp.getTime() / 1000));

            } else {
                jsonGenerator.writeNull();
            }
        }

    }
}
