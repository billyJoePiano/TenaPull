package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

import java.io.IOException;
import java.sql.Timestamp;

public class EpochTimestamp {
    public static final long EPOCH_LOCAL_OFFSET
            = Math.max(0, -TimeZone.getDefault().getOffset(0));

    private static final Timestamp ZERO = new Timestamp(EPOCH_LOCAL_OFFSET);
    // timezone offset in MS (as a positive value)
    // needed to prevent SQL errors in cases of epoch time 0

    public static class Deserializer extends JsonDeserializer<Timestamp> {
        private static Logger logger = LogManager.getLogger(Deserializer.class);

        @Override
        public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentValue() == JsonToken.VALUE_NULL) {
                return null;
            }
            String timestamp = jp.getText().trim();
            return deserialize(timestamp);
        }

        public static Timestamp deserialize(String timestamp) {
            try {
                long epochMs = Long.parseLong(timestamp + "000");
                if (epochMs <= EPOCH_LOCAL_OFFSET) return ZERO;
                else return new Timestamp(epochMs);

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
                if (timestamp.getTime() > EPOCH_LOCAL_OFFSET) {
                    jsonGenerator.writeNumber((int) (timestamp.getTime() / 1000));

                } else {
                    jsonGenerator.writeNumber(0);
                }

            } else {
                jsonGenerator.writeNull();
            }
        }

    }
}
