package tenapull.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import java.io.*;
import java.sql.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

/**
 * Includes three inner classes for serializing timestamps from different formats into a single
 * "human friendly" timestamp format.
 */
public class FriendlyTimestamp {
    /**
     * "Human friendly" output format, e.g. Wed Apr 10 14:51:25 2022, which emulates the human-friendly
     * timestamp strings provided by the Nessus API
     */
    public static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("EEE LLL d H:mm:ss uuuu");

    /**
     * Serializes a java.sql.Timestamp into a "human friendly" output format,
     * e.g. Wed Apr 10 14:51:25 2022
     */
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
                jg.writeString(OUTPUT_FORMATTER.format(ldt));

            } else {
                jg.writeString(value.toString());
            }

        }
    }

    /**
     * The expected input format for a "numeric string" timestamp accurate to the minute,
     * e.g. "202204101451" represents April 10, 2022 at 14:51 (2:51pm)
     */
    private static final DateTimeFormatter NUMERIC_STRING = DateTimeFormatter.ofPattern("uuuuMMddHHmm")
                                                            .withZone(TimeZone.getDefault().toZoneId());

    /**
     * Serializes a "numeric string" date/time format into a "human friendly" output format,
     * e.g. e.g. "202204101451" converts to Wed Apr 10 14:51:00 2022
     */
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
                value = OUTPUT_FORMATTER.format(LocalDateTime.from(NUMERIC_STRING.parse(value)));

            } catch (DateTimeException e) { }

            jg.writeString(value);
        }
    }

    /**
     * The expected input format for a standard JavaScript timestamp
     */
    private static final DateTimeFormatter JS_STRING = DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss")
                                                        .withZone(TimeZone.getDefault().toZoneId());

    /**
     * Serializes a standard JavaScript timestamp string into a "human friendly" output format
     */
    public static class JsString extends JsonSerializer<String> {
        /**
         * Serializes a standard JavaScript timestamp string into a "human friendly" output format
         */
        @Override
        public void serialize(String value,
                              JsonGenerator jg,
                              SerializerProvider sp) throws IOException {

            if (value == null) {
                jg.writeNull();
                return;
            }

            try {
                value = OUTPUT_FORMATTER.format(LocalDateTime.from(JS_STRING.parse(value)));

            } catch (DateTimeException e) { }

            jg.writeString(value);
        }
    }
}
