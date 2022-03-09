package nessusData.serialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;

import java.sql.Timestamp;

import org.apache.logging.log4j.*;

public class EpochTimestampDeserializer extends JsonDeserializer<Timestamp> {
    Logger logger = LogManager.getLogger(EpochTimestampDeserializer.class);

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
