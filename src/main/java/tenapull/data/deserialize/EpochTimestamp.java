package tenapull.data.deserialize;

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

/**
 * Includes two inner classes for deserializing and serializing epoch timestamps between
 * a JSON integer (representing seconds) and java.sql.Timestamp
 */
public class EpochTimestamp {
    /**
     * The timezone offset, needed for the ZERO constant
     */
    public static final long EPOCH_LOCAL_OFFSET
            = Math.max(0, -TimeZone.getDefault().getOffset(0));

    /**
     * Epoch zero timestamp adjusted for timezone, to prevent SQL errors from being thrown
     * when the timestamp is zero
     */
    private static final Timestamp ZERO = new Timestamp(EPOCH_LOCAL_OFFSET);
    // timezone offset in MS (as a positive value)
    // needed to prevent SQL errors in cases of epoch time 0

    /**
     * Deserializes a JSON integer representing seconds into a java.sql.Timestamp
     */
    public static class Deserializer extends JsonDeserializer<Timestamp> {
        private static Logger logger = LogManager.getLogger(Deserializer.class);

        /**
         * Obtains the timestamp as text and passes to the static deserialize method, which does the work of
         * converting to java.sql.Timestamp
         *
         * @param jp
         * @param ctxt
         * @return The converted timestamp
         * @throws IOException
         * @throws JsonProcessingException
         */
        @Override
        public Timestamp deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            if (jp.getCurrentValue() == JsonToken.VALUE_NULL) {
                return null;
            }
            String timestamp = jp.getText().trim();
            return deserialize(timestamp);
        }

        /**
         * Static utility which is invoked by the instance deserialize method, and can also be
         * invoked by other classes.  Takes an epoch timestamp string in seconds, adds three
         * zeros to convert to milliseconds, parses as an int, and then converts to a java.sql.Timestamp
         *
         *
         * @param timestamp string in seconds
         * @return the converted timestamp
         */
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

    /**
     * Serializes a java.sql.Timestamp into a JSON integer based on epoch seconds
     */
    public static class Serializer extends JsonSerializer<Timestamp> {
        Logger logger = LogManager.getLogger(Serializer.class);

        /**
         * Serializes a java.sql.Timestamp into a JSON integer based on epoch seconds
         *
         * @param timestamp
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
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
