package nessusTools.data.persistence;

import nessusTools.sync.*;
import org.apache.logging.log4j.*;

import javax.persistence.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class MultiTypeWrapper {
    private static final Logger logger = LogManager.getLogger(MultiTypeWrapper.class);

    public static char UNKNOWN = 'U';

    public static final List<Class> STRING_TYPES = List.of(
            String.class //,
            // Character[].class // TODO ???
        );

    public static final List<Class> INT_TYPES = List.of(
            Integer.class,
            AtomicInteger.class
        );

    public static final List<Class> BOOLEAN_TYPES = List.of(
            Boolean.class
        );

    public static final List<Class> DOUBLE_TYPES = List.of(
            Double.class,
            DoubleAccumulator.class,
            DoubleAdder.class
        );

    public static final List<Class> LONG_TYPES = List.of(
            Long.class,
            AtomicLong.class,
            LongAccumulator.class,
            LongAdder.class
        );

    public static final List<Class> BYTE_TYPES = List.of(
            Byte.class
        );

    public static final List<Class> SHORT_TYPES = List.of(
            Short.class
        );

    public static final List<Class> FLOAT_TYPES = List.of(
            Float.class
        );

    public static final List<Class> BIGINT_TYPES = List.of(
            BigInteger.class
        );

    public static final List<Class> BIGDECIMAL_TYPES = List.of(
            BigDecimal.class
        );


    // NOTE: the first class in each list is the "Primary type", that will be used when re-marshalling entities
    // from DB varchar values.
    // Character == first character of DB varchar value, to indicate the type of the subsequent string
    // All primary types MUST have a single-argument String constructor
    public static final Map<Character, List<Class>> TYPE_MAP = Collections.unmodifiableMap(
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


    public static MultiTypeWrapper wrap(Object object) {
        if (object == null) {
            return null;

        } else if (object instanceof MultiTypeWrapper) {
            return (MultiTypeWrapper) object;
        }

        String dbString = makeDbStringForNotNull(object);
        return byDbString.getOrConstructWith(dbString, dbs ->
            byInstanceWrapped.getOrConstructWith(object, o -> {
                MultiTypeWrapper wrapper = new MultiTypeWrapper(dbString);
                Object obj = wrapper.getObject();

                if (!(obj instanceof MultiTypeWrapper || Objects.equals(obj, object))) {
                    MultiTypeWrapper alt = byInstanceWrapped.put(obj, wrapper);

                    if (alt != null && alt != wrapper) {
                        if (Objects.equals(wrapper, alt)) {
                            return alt;

                        } else {
                            logger.warn("!wrapper.equals(alt) (1)");
                        }
                    }
                }

                return wrapper;
            })
        );
    }


    public static MultiTypeWrapper buildFrom(String dbString) {
        if (dbString == null) {
            return null;

        }

        return byDbString.getOrConstruct(dbString);
    }


    // Maps all instances passed to "wrap" to their respective dbStrings
    // these dbStrings can then be used to access a MultiTypeWrapper in the wrapperTracker
    private static WeakInstancesTracker<Object, MultiTypeWrapper>
            byInstanceWrapped = byInstanceWrapped();



    private static InstancesTracker<String, MultiTypeWrapper>
            byDbString = byDbString();

    private static WeakInstancesTracker<Object, MultiTypeWrapper> byInstanceWrapped() {
        return new WeakInstancesTracker(Object.class, MultiTypeWrapper.class, null);
    }

    private static InstancesTracker<String, MultiTypeWrapper> byDbString() {
        return new InstancesTracker<String, MultiTypeWrapper>(
            String.class,
            MultiTypeWrapper.class,
            dbString -> {
                MultiTypeWrapper wrapper = new MultiTypeWrapper(dbString);
                Object obj = wrapper.getObject() != null ? wrapper.getObject() : wrapper;
                // We need a unique key that allows us to place the instance into the
                // wrappedInstancesTracker as well, in case an Unknown object comes
                // along that matches the dbString for this (assuming this construction
                // was based soley on dbString)

                if (!Objects.equals(dbString, wrapper.toDb())) {
                    MultiTypeWrapper alt = byDbString.get(wrapper.toDb());
                    if (alt != null && alt != wrapper) {
                        if (Objects.equals(wrapper, alt)) {
                            return alt;

                        } else {
                            logger.warn("!wrapper.equals(alt) (2)");
                        }
                    }

                }

                MultiTypeWrapper alt = byInstanceWrapped.getOrConstructWith(obj, o -> wrapper);
                if (alt != null && alt != wrapper) {
                    if (Objects.equals(wrapper, alt)) {
                        return alt;

                    } else {
                        logger.warn("!wrapper.equals(alt) (3)");
                    }
                }

                return wrapper;
            });
    }


    public static int size() {
        return byDbString.size();
    }

    //Counts the number of
    private static long counter = 0;
    public static long getCounter() {
        return counter;
    }
    public static void resetCounter() {
        counter = 0;
    }
    public static void clearInstances() {
        byDbString = byDbString();
        byInstanceWrapped = byInstanceWrapped();
    }

    private final Object object;
    private final String dbString;
    private final String toString;
    private final String typeString; //used for unknown types only
    private final Class primaryType;

    private MultiTypeWrapper(String dbString) {
        counter++;

        char typeIdentifier = dbString.charAt(0);

        if (typeIdentifier == UNKNOWN) {
            String dbs = dbString;
            if (dbs.length() > 1) {
                dbs = dbs.substring(1);

            } else {
                dbs = "";
            }

            String[] typeAndValue = parseUnknownTypeDbString(dbs);
            this.dbString = UNKNOWN + typeAndValue[0] + "\n" + typeAndValue[1];
            this.typeString = typeAndValue[0];
            this.toString = typeAndValue[1];
            this.primaryType = null;
            this.object = this;
            return;

        } else {

            List<Class> typeList = TYPE_MAP.get(typeIdentifier);
            if (typeList == null) {
                logger.error("Invalid multi-type value passed from DB, does not have correct type init:\n'"
                        + dbString + "'");
                this.dbString = dbString;
                this.typeString = null;
                this.toString = dbString;
                this.primaryType = null;
                this.object = this;
                return;

            }

            this.primaryType = typeList.get(0);
        }

        String constructorString;
        if (dbString.length() > 1) {
            constructorString = dbString.substring(1);

        } else {
            constructorString = "";
        }


        Object instance = null;

        try {
            instance = primaryType.getDeclaredConstructor(String.class).newInstance(constructorString);

        } catch (Exception e) {
            logger.error(e);

            this.toString = constructorString;
            this.dbString = dbString;
            this.object = this;
            this.typeString = null;
            return;
        }

        this.toString = instance.toString();
        this.dbString = typeIdentifier + toString;
        this.object = instance;
        this.typeString = null;
    }





    public static Class getPrimaryType(Object object) {
        if (object == null) {
            return null;
        }
        Map.Entry<Character, List<Class>> entry = getPrimaryTypeNotNull(object);
        if (entry != null) {
            return entry.getValue().get(0);

        } else {
            return null;
        }
    }

    public static Map.Entry<Character, Class> getPrimaryTypeWithChar(Object object) {
        if (object == null) {
            return null;
        }

        Map.Entry<Character, List<Class>> entry = getPrimaryTypeNotNull(object);
        if (entry != null) {
            return new AbstractMap.SimpleEntry(entry.getKey(), entry.getValue().get(0));

        } else {
            return null;
        }
    }

    private static Map.Entry<Character, List<Class>> getPrimaryTypeNotNull(Object object) {
        for (Map.Entry<Character, List<Class>> entry : TYPE_MAP.entrySet()) {
            List<Class> list = entry.getValue();
            for (Class type : list) {
                if (type.isInstance(object)) {
                    return entry;
                }
            }
        }
        return null;
    }

    public static String makeDbStringFor(Object object) {
        if (object == null) {
            return null;

        } else if (object instanceof MultiTypeWrapper) {
            return ((MultiTypeWrapper)object).toDb();

        } else {
            return makeDbStringForNotNull(object);
        }
    }

    //error messages.  These will end up in the DB if there are errors or nulls with unknown types
    private static final String PRE = "<" + MultiTypeWrapper.class.toString() + " for Unknown class : ";

    public static final String NULL_VALUE = PRE + "object.toString() returned null>";
    public static final String NULL_TYPE = PRE + "object.getClass() returned null>";
    public static final String NULL_TYPE_STRING = PRE + "object.getClass().toString() returned null>";

    private static String makeDbStringForNotNull(Object object) {
        Map.Entry<Character, List<Class>> entry = getPrimaryTypeNotNull(object);
        if (entry != null) {
            return entry.getKey() + object.toString();
        }

        //Unknown type

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

        return UNKNOWN + typeString + "\n" + valueString;
    }

    // Error messages when there are errors with the init string of an unknown type:
    public static final String MISSING_TYPE = PRE + "init did not include type string>";
    public static final String MISSING_VALUE = PRE + "init did not include value string>";

    private static String[] parseUnknownTypeDbString(String dbString) { // after initial type identifier has been removed

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

        return new String[] { typeString, valueString };
    }


    @Override
    public String toString() {
        return this.toString;
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

        if (this.object instanceof MultiTypeWrapper) {
            if (o instanceof MultiTypeWrapper) {
                MultiTypeWrapper other = (MultiTypeWrapper) o;
                if (other.object instanceof MultiTypeWrapper) {
                    return Objects.equals(this.dbString, other.dbString);

                } else {
                    return false;
                }
            } else {
                return Objects.equals(this.dbString, makeDbStringForNotNull(o));
            }

        } else if (o instanceof MultiTypeWrapper) {
            MultiTypeWrapper other = (MultiTypeWrapper) o;

            if (other.object instanceof MultiTypeWrapper) {
                return false;

            } else if (this.object == null && other.object == null) {
                return false;

            } else {
                return Objects.equals(other.object, this.object);
            }

        } else {
            return Objects.equals(this.dbString, makeDbStringForNotNull(o));
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
}
