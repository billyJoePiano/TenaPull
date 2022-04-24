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

public class MultiType {
    public static class Deserializer extends JsonDeserializer<MultiTypeWrapper> {
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

    public static class Serializer extends JsonSerializer<MultiTypeWrapper> {
        Logger logger = LogManager.getLogger(Serializer.class);

        // https://stackoverflow.com/questions/33519354/how-to-get-property-or-field-name-in-a-custom-json-serializer
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

        // returns true if jsonGenerator wrote a value
        // returns false if a matching type could not be found for object, and therefore no value was written
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
