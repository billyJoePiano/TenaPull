package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.*;

public class FriendlyTimestamp {
    private static final DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("EEE LLL d H:mm:ss uuuu");

    public static class Sql extends JsonSerializer<Timestamp> {
        @Override
        public void serialize(Timestamp value,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {

            if (value == null) {
                jg.writeNull();
                return;
            }

            LocalDateTime ldt = value.toLocalDateTime();
            if (ldt != null) {
                jg.writeString(outputFormatter.format(ldt));

            } else {
                jg.writeString(value.toString());
            }

        }
    }

    private static final DateTimeFormatter numericString = DateTimeFormatter.ofPattern("uuuuMMddHHmm")
                                                            .withZone(TimeZone.getDefault().toZoneId());

    public static class NumericString extends JsonSerializer<String> {
        @Override
        public void serialize(String value,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {

            if (value == null) {
                jg.writeNull();
                return;
            }

            try {
                value = outputFormatter.format(LocalDateTime.from(numericString.parse(value)));

            } catch (DateTimeException e) { }

            jg.writeString(value);
        }
    }

    private static final DateTimeFormatter jsString = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss")
                                                        .withZone(TimeZone.getDefault().toZoneId());

    public static class JsString extends JsonSerializer<String> {
        @Override
        public void serialize(String value,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {

            if (value == null) {
                jg.writeNull();
                return;
            }

            try {
                value = outputFormatter.format(LocalDateTime.from(jsString.parse(value)));

            } catch (DateTimeException e) { }

            jg.writeString(value);
        }
    }
}
