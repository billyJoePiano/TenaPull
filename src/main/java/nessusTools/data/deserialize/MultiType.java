package nessusTools.data.deserialize;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;

import javax.json.*;
import java.io.*;
import java.math.*;

import static nessusTools.data.persistence.MultiTypeWrapper.wrap;
import static nessusTools.data.persistence.MultiTypeWrapper.getPrimaryType;

/**
 * Includes two inner classes for serializing and deserializing the MultiTypeWrapper, which is
 * capable of representing more than one primitive type (e.g. string, integer) in both JSON
 * and through the Hibernate ORM
 */
public class MultiType {
    /**
     * Deserializes a JSON value of an unknown primitive type into a MultiTypeWrapper
     */
    public static class Deserializer extends JsonDeserializer<MultiTypeWrapper> {
        /**
         * Deserializes a JSON value of an unknown primitive type into a MultiTypeWrapper
         *
         * @param jp
         * @param ctxt
         * @return
         * @throws IOException
         */
        @Override
        public MultiTypeWrapper deserialize(JsonParser jp,
                                            DeserializationContext ctxt)
                throws IOException {
            JsonToken token = jp.currentToken();

            switch(token) {
                case VALUE_NULL:
                    return null;

                case VALUE_STRING:
                    return wrap(jp.getText());

                case VALUE_NUMBER_INT: case VALUE_NUMBER_FLOAT:
                    switch(jp.getNumberType()) {
                        case INT:
                            return wrap(jp.readValueAs(Integer.class));

                        case DOUBLE:
                            return wrap(jp.readValueAs(Double.class));

                        case FLOAT:
                            return wrap(jp.readValueAs(Float.class));

                        case LONG:
                            return wrap(jp.readValueAs(Long.class));

                        case BIG_DECIMAL:
                            return wrap(jp.getDecimalValue());

                        case BIG_INTEGER:
                            return wrap(jp.getBigIntegerValue());
                    }

                case VALUE_TRUE:
                    return wrap(Boolean.TRUE);

                case VALUE_FALSE:
                    return wrap(Boolean.FALSE);

                default:
                    throw new JsonException(this.getClass()
                            + " cannot deserialize JSON value of type "
                            + jp.currentToken() + "\n"
                            + jp.getText());
            }

        }
    }

    /**
     * Serializes a MultiTypeWrapper into the appropriate JSON primitive
     */
    public static class Serializer extends JsonSerializer<MultiTypeWrapper> {
        Logger logger = LogManager.getLogger(Serializer.class);

        /**
         * Serializes a MultiTypeWrapper into the appropriate JSON primitive, delegating to
         * the static writeValueNotNull method for all non-null and non-string values.
         * If writeValueNotNull returns false, meaning no value was written, then this
         * method will default to writing the value as a string
         *
         * @param wrapper
         * @param jsonGenerator
         * @param serializerProvider
         * @throws IOException
         */
        @Override
        public void serialize(MultiTypeWrapper wrapper,
                              JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider)
                throws IOException {


            if (wrapper == null) {
                jsonGenerator.writeNull();
                return;
            }

            Class type = wrapper.getPrimaryType();
            if (type == null
                    || type.equals(String.class)
                    || type.equals(MultiTypeWrapper.class)) {

                jsonGenerator.writeString(wrapper.toString());

            } else if (!writeValueNotNull(wrapper.getObject(), type, jsonGenerator)) {
                logger.error("Unexpected type while serializing MultiTypeObjectWrapper : "
                        + type.toString() + "\n"
                        + wrapper.toDb());

                jsonGenerator.writeString(wrapper.toString());
            }

        }

        /**
         * Serializes an Object representing any primitive JSON type, delegating to writeValueNotNull
         * for all non-null and non-string objects.
         *
         * @param object
         * @param jsonGenerator
         * @return true when the jsonGenerator wrote a value, false if no matching type could be found
         * and therefore no value was written.
         * @throws IOException
         */
        public static boolean writeValue(Object object, JsonGenerator jsonGenerator)
                throws IOException {

            if (object == null) {
                jsonGenerator.writeNull();
                return true;
            }

            Class primaryType = getPrimaryType(object);
            if (primaryType == null) {
                return false;
            }

            if (primaryType.equals(String.class)) {
                jsonGenerator.writeString(object.toString());
                return true;

            } else {
                return writeValueNotNull(object, primaryType, jsonGenerator);
            }

        }

        /**
         * Writes any non-null and non-string value as the appropriate JSON primitive type, based
         * upon MultiTypeWrapper's "primary type" provided (e.g. AtomicInteger falls under the Integer
         * primary type)
         *
         * @param object
         * @param primaryType The primaryType as specified in MultiTypeWrapper
         * @param jsonGenerator
         * @return true when the jsonGenerator wrote a value, false if no matching type could be found
         * and therefore no value was written.
         * @throws IOException
         */
        private static boolean writeValueNotNull(Object object, Class primaryType, JsonGenerator jsonGenerator)
                throws IOException {

            if (primaryType.equals(Integer.class)) {
                jsonGenerator.writeNumber(((Number) object).intValue());

            } else if (primaryType.equals(Boolean.class)) {
                jsonGenerator.writeBoolean((Boolean) object);

            } else if (primaryType.equals(Double.class)) {
                jsonGenerator.writeNumber(((Number) object).doubleValue());

            } else if (primaryType.equals(Long.class)) {
                jsonGenerator.writeNumber(((Number) object).longValue());

            } else if (primaryType.equals(Byte.class)) {
                jsonGenerator.writeNumber(((Number) object).byteValue());

            } else if (primaryType.equals(Short.class)) {
                jsonGenerator.writeNumber(((Number) object).shortValue());

            } else if (primaryType.equals(Float.class)) {
                jsonGenerator.writeNumber(((Number) object).floatValue());

            } else if (primaryType.equals(BigInteger.class)) {
                jsonGenerator.writeNumber((BigInteger) object);

            } else if (primaryType.equals(BigDecimal.class)) {
                jsonGenerator.writeNumber((BigDecimal) object);

            } else {
                return false;
            }
            return true;
        }
    }
}
