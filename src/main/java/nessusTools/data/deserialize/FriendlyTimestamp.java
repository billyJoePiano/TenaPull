package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.sql.*;
import java.time.*;

public class FriendlyTimestamp extends JsonSerializer<Timestamp> {
    @Override
    public void serialize(Timestamp value,
                          JsonGenerator jg,
                          SerializerProvider sp) throws IOException {

        if (value != null) {
            LocalDateTime ldt = value.toLocalDateTime();
            if (ldt != null) {
                jg.writeString(ldt.toString());

            } else {
                jg.writeString(value.toString());
            }

        } else {
            jg.writeNull();
        }
    }
}
