package nessusTools.data.persistence;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.deserialize.*;
import org.apache.logging.log4j.*;

import javax.json.*;
import javax.persistence.*;
import java.io.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class MultiTypeWrapper {
    private static final Logger logger = LogManager.getLogger(MultiTypeWrapper.class);

    public static char UNKNOWN = 'U';

    private static final List<Class> STRING_TYPES = List.of(
            String.class //,
            // Character[].class // TODO ???
        );

    private static final List<Class> INT_TYPES = List.of(
            Integer.class,
            AtomicInteger.class
        );

    private static final List<Class> BOOLEAN_TYPES = List.of(
            Boolean.class
        );

    private static final List<Class> DOUBLE_TYPES = List.of(
            Double.class,
            DoubleAccumulator.class,
            DoubleAdder.class
        );

    private static final List<Class> LONG_TYPES = List.of(
            Long.class,
            AtomicLong.class,
            LongAccumulator.class,
            LongAdder.class
        );

    private static final List<Class> BYTE_TYPES = List.of(
            Byte.class
        );

    private static final List<Class> SHORT_TYPES = List.of(
            Short.class
        );

    private static final List<Class> FLOAT_TYPES = List.of(
            Float.class
        );

    private static final List<Class> BIGINT_TYPES = List.of(
            BigInteger.class
        );

    private static final List<Class> BIGDECIMAL_TYPES = List.of(
            BigDecimal.class
        );


    // NOTE: the first class in each list is the "Primary type", that will be used when re-marshalling entities
    // from DB varchar values.
    // Character == first character of DB varchar value, to indicate the type of the subsequent string
    // All primary types MUST have a single-argument String constructor
    private static final Map<Character, List<Class>> TYPE_MAP = Collections.unmodifiableMap(
            new LinkedHashMap() { { //preserves order
                put('S',STRING_TYPES);
                put('i',INT_TYPES);
                put('B',BOOLEAN_TYPES);
                put('d',DOUBLE_TYPES);
                put('l',LONG_TYPES);
                put('b',BYTE_TYPES);
                put('s',SHORT_TYPES);
                put('f',FLOAT_TYPES);
                put('I',BIGINT_TYPES);
                put('D',BIGDECIMAL_TYPES);
            } });



    private static final Map<MultiTypeWrapper, String> dbStringMap = new WeakHashMap();

    public static MultiTypeWrapper wrap(Object object) {
        if (object == null) {
            return null;

        } else if (object instanceof MultiTypeWrapper) {
            return (MultiTypeWrapper) object;
        }

        for (Map.Entry<Character, List<Class>> entry : TYPE_MAP.entrySet()) {
            List<Class> list = entry.getValue();
            for (Class type : list) {
                if (type.isInstance(object)) {
                    return fetchOrConstruct(object, entry.getKey() + object.toString(), type);
                }
            }
        }

        return constructUnknownType(object);
    }

    public static MultiTypeWrapper buildFrom(String dbString) {
        if (dbString == null) {
            return null;

        } else if (dbString.length() < 1) {
            return fetchOrConstruct(dbString, dbString, MultiTypeWrapper.class); // dbString is ""
        }

        MultiTypeWrapper wrapper = get(dbString);
        if (wrapper != null) {
            return wrapper;
        }

        char typeIdentifier = dbString.charAt(0);

        if (typeIdentifier == UNKNOWN) {
            return parseUnknownTypeDbString(dbString);
        }

        List<Class> typeList = TYPE_MAP.get(typeIdentifier);
        if (typeList == null) {
            logger.error("Invalid multi-type value passed from DB, does not have correct type init: '"
                    + dbString + "'");
            return fetchOrConstruct(dbString, dbString, MultiTypeWrapper.class);
        }

        Class type = typeList.get(0);

        String constructorString;
        if (dbString.length() > 1) {
            constructorString = dbString.substring(1);

        } else {
            constructorString = "";
        }

        Object instance = null;

        try {
            instance = type.getDeclaredConstructor(String.class).newInstance(constructorString);

        } catch (Throwable e) {
            logger.error(e);
            instance = constructorString;
        }

        return fetchOrConstruct(instance, dbString, type);
    }

    private static MultiTypeWrapper fetchOrConstruct(Object object, String dbString, Class primaryType) {
        MultiTypeWrapper wrapper = get(dbString);

        if (wrapper != null) {
            return wrapper;

        } else {
            return put(new MultiTypeWrapper(object, dbString, primaryType));
        }
    }

    private static MultiTypeWrapper put(MultiTypeWrapper wrapper) {
        MultiTypeWrapper other;
        synchronized (dbStringMap) {
            other = get(wrapper.toDb()); //in case an identical instance was constructed under race conditions
            if (other == null) {
                dbStringMap.put(wrapper, wrapper.toDb());

            } else {
                wrapper = other;
            }
        }
        return wrapper;
    }

    private static MultiTypeWrapper get(String dbString) {
        synchronized (dbStringMap) {
            MultiTypeWrapper wrapper;
            if (!dbStringMap.containsValue(dbString)) return null;
            for (Map.Entry<MultiTypeWrapper, String> entry : dbStringMap.entrySet()) {
                if (entry.getValue().equals(dbString)) {
                    wrapper = entry.getKey();
                    if (wrapper != null) { //in case it was garbage collected while iterating?!?!?
                        return wrapper;

                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }


    //error messages.  These will end up in the DB if there are errors or nulls with unknown types
    private static final String PRE = "<" + MultiTypeWrapper.class.toString() + " for Unknown class : ";

    public static final String NULL_VALUE = PRE + "object.toString() returned null>";
    public static final String NULL_TYPE = PRE + "object.getClass() returned null>";
    public static final String NULL_TYPE_STRING = PRE + "object.getClass().toString() returned null>";

    private static MultiTypeWrapper constructUnknownType(Object object) {
        if (object == null) {
            return null;
        }

        String valueString = object.toString();
        if (valueString == null) {
            valueString = NULL_VALUE;
        }


        Class type = object.getClass();
        String typeString;
        if (type != null) {
            typeString = type.toString();
            if (typeString == null) {
                typeString = NULL_TYPE_STRING;
            }
        } else {
            typeString = NULL_TYPE;
        }

        return put(new MultiTypeWrapper(new String[] {typeString, valueString}));
    }

    // Error messages when there are errors with the init string of an unknown type:
    public static final String MISSING_TYPE = PRE + "init did not include type string>";
    public static final String MISSING_VALUE = PRE + "init did not include value string>";

    private static MultiTypeWrapper parseUnknownTypeDbString(String dbString) {
        if (dbString.length() > 1) {
            dbString = dbString.substring(1);

        } else {
            dbString = "";
        }

        String[] list = dbString.split("\\n", 2);

        if (list.length < 1 || list.length > 2) {
            throw new IllegalStateException(
                    "Error splitting dbString string for MultiTypeObjectWrapper -- UnknownType:\n"
                            + dbString + "\nsplit on new line:" + list.toString());
        }

        String valueString;

        if (list.length == 2 && list[1] != null) {
            valueString = list[1];

        } else {
            valueString = MISSING_VALUE;
        }

        String typeString;
        if (list[0] != null) {
            typeString = list[0];

        } else {
            typeString = MISSING_TYPE;
        }

        return put(new MultiTypeWrapper(dbString, new String[] { typeString, valueString }));
    }



    private final Object object;
    private final String dbString;
    private final String toString;
    private final String typeString;
    private final Class primaryType;

    private MultiTypeWrapper(Object object,
                             String dbString,
                             Class primaryType) {

        this.object = object;
        this.dbString = dbString;
        this.primaryType = primaryType;

        Class type = object.getClass();

        if (type != null) {
            String typeString = type.toString();
            if (typeString != null) {
                this.typeString = typeString;
            } else {
                this.typeString = NULL_TYPE_STRING;
            }
        } else {
            this.typeString = NULL_TYPE;
        }

        String toString = object.toString();
        if (toString != null) {
            this.toString = toString;

        } else {
            this.toString = NULL_VALUE;
        }
    }

    // For objects with a class not included in TYPE_MAP aka 'u' or unknown
    private MultiTypeWrapper(String dbString,
                             String[] typeAndValue) {

        this.dbString = dbString;
        this.object = this;
        this.typeString = typeAndValue[0];
        this.toString = typeAndValue[1];
        this.primaryType = null;
    }

    private MultiTypeWrapper(String[] typeAndValue) {

        this(UNKNOWN + typeAndValue[0]
                            + "\n" + typeAndValue[1],
                    typeAndValue);
    }

    @Override
    public String toString() {
        return toString;
    }

    public String getTypeString() {
        return this.typeString;
    }

    public Class getPrimaryType() {
        return this.primaryType;
    }

    public String toDb() {
        return this.dbString;
    }

    public Object getObject() {
        return this.object;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;

        if (o instanceof MultiTypeWrapper) {
            MultiTypeWrapper other = (MultiTypeWrapper) o;
            return Objects.equals(other.object, this.object);

        } else if (this.object instanceof MultiTypeWrapper) {
            return false;

        } else {
            return Objects.equals(this.object, o);
        }

    }

    @javax.persistence.Converter
    public static class Converter implements AttributeConverter<MultiTypeWrapper, String>  {
        @Override
        public String convertToDatabaseColumn(MultiTypeWrapper wrapper) {
            if (wrapper != null) {
                return wrapper.toDb();

            } else {
                return null;
            }
        }

        @Override
        public MultiTypeWrapper convertToEntityAttribute(String dbString) {
            if (dbString != null) {
                return MultiTypeWrapper.buildFrom(dbString);

            } else {
                return null;
            }
        }
    }

    public static class Deserializer extends JsonDeserializer<MultiTypeWrapper> {
        @Override
        public MultiTypeWrapper deserialize(JsonParser jp,
                                            DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            JsonToken token = jp.getCurrentToken();

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
                            + jp.getCurrentToken() + "\n"
                            + jp.getText());
            }

        }
    }

    public static class Serializer extends JsonSerializer<MultiTypeWrapper> {
        Logger logger = LogManager.getLogger(EpochTimestamp.Serializer.class);

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

            } else if (type.equals(Integer.class)) {
                jsonGenerator.writeNumber(((Number) wrapper.getObject()).intValue());

            } else if (type.equals(Boolean.class)) {
                jsonGenerator.writeBoolean((Boolean) wrapper.getObject());

            } else if (type.equals(Double.class)) {
                jsonGenerator.writeNumber(((Number) wrapper.getObject()).doubleValue());

            } else if (type.equals(Long.class)) {
                jsonGenerator.writeNumber(((Number) wrapper.getObject()).longValue());

            } else if (type.equals(Byte.class)) {
                jsonGenerator.writeNumber(((Number) wrapper.getObject()).byteValue());

            } else if (type.equals(Short.class)) {
                jsonGenerator.writeNumber(((Number) wrapper.getObject()).shortValue());

            } else if (type.equals(Float.class)) {
                jsonGenerator.writeNumber(((Number) wrapper.getObject()).floatValue());

            } else if (type.equals(BigInteger.class)) {
                jsonGenerator.writeNumber((BigInteger) wrapper.getObject());

            } else if (type.equals(BigDecimal.class)) {
                jsonGenerator.writeNumber((BigDecimal) wrapper.getObject());

            } else {
                logger.error("Unexpected type while serializing MultiTypeObjectWrapper : "
                        + type.toString() + "\n"
                        + wrapper.toDb());

                jsonGenerator.writeString(wrapper.toString());
            }

        }
    }
}
