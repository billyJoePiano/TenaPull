package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.apache.logging.log4j.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

/**
 * Represents a reusable extra json lookup, for any unexpected json returned from the
 * Nessus API
 */
@Entity(name = "ExtraJson")
@Table(name = "extra_json")
public class ExtraJson implements HashLookupPojo<ExtraJson> {
    /**
     * The dao for ExtraJson
     */
    public static final HashLookupDao<ExtraJson> dao = new HashLookupDao<>(ExtraJson.class);


    private static final Logger logger = LogManager.getLogger(ExtraJson.class);

    /**
     * Static utility method for creating a Json escape string of a given string
     *
     * @param str the string to escape
     * @return the JSON-escaped string
     */
    public static String escapeString(String str) {
        return "\"" + new String(JsonStringEncoder.getInstance().quoteAsString(str)) + "\"";
    }

    /**
     * Static utility method for creating a Json escape string of a given string,
     * but omitting the leading and trailing quotation marks.
     *
     *
     * @param str the string to escape
     * @return the JSON-escaped string without enclosing quotation marks
     */
    public static String escapeStringNoQuotes(String str) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(str));
    }

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    @JsonIgnore
    private int id;

    @Convert(converter = ExtraJson.Converter.class)
    private JsonMap value;

    /**
     * Instantiates a new Extra json.
     */
    public ExtraJson() {
        this.value = new JsonMap();
    }

    /**
     * Instantiates a new Extra json, copying the value map of the passed ExtraJson
     *
     * @param copyExceptId the ExtraJson to copy the value map from
     */
    public ExtraJson(ExtraJson copyExceptId) {
        this(copyExceptId.value.map);
    }

    /**
     * Instantiates a new Extra json using a map of string keys and JsonNodes.
     *
     * @param value the map to copy
     * @throws NullPointerException if the value is null
     */
    public ExtraJson(Map<String, JsonNode> value)
            throws NullPointerException {

        if (value == null) {
            throw new NullPointerException();
        }
        this.value = new JsonMap(value);
    }

    /**
     * Instantiates a new Extra json by deserializing a string
     *
     * @param jsonStr the json str to deserialize
     * @throws JsonProcessingException  if there is a JsonProcessingException while deserialization
     * @throws IllegalArgumentException if the deserialized JsonNode is something other than an ObjectNode
     */
    public ExtraJson(String jsonStr)
            throws JsonProcessingException, IllegalArgumentException {

        this.value = new JsonMap(jsonStr);
    }

    @NaturalId
    @Access(AccessType.PROPERTY)
    @Column(name = "_hash")
    @Convert(converter = Hash.Converter.class)
    @JsonIgnore
    public Hash get_hash() {
        return this.value.get_hash();
    }

    @JsonIgnore
    @Override
    public void set_hash(Hash _hash) {
        this.value.set_hash(_hash);
    }


    public int hashCode() {
        return this.value.hashCode();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }

        ExtraJson other = (ExtraJson)o;

        return Objects.equals(this.value.map, other.value.map);
    }

    /**
     * Put the provided json node in the value map using the provided key
     *
     * @param key   the key
     * @param value the value
     * @return the json node
     * @throws IllegalStateException the illegal state exception
     */
    @Transient
    public JsonNode put(String key, JsonNode value) throws IllegalStateException {
        if (this.id != 0) throw new IllegalStateException(
                "Cannot mutate ExtraJson after record has been inserted!  Create a copy and insert as a new record instead");
        return this.value.map.put(key, value);
    }

    /**
     * Get the json node in the value map at the provided key
     *
     * @param key the key
     * @return the json node
     */
    @Transient
    public JsonNode get(String key) {
        return value.map.get(key);
    }

    @Transient
    public String toString() {
        return this.value.toString();
    }

    /**
     * The size of the value map
     *
     * @return the int
     */
    @Transient
    public int size() {
        return this.value.map.size();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the value map
     *
     * @return the value
     */
    public JsonMap getValue() {
        return this.value;
    }

    /**
     * Sets the value map
     *
     * @param map the map
     * @throws NullPointerException the null pointer exception
     */
    public void setValue(JsonMap map) throws NullPointerException {
        if (map == null) throw new NullPointerException();
        if (this.value == map) return;
        if (this.value._hash != null) {
            if (map._hash != null) {
                if (!Objects.equals(this.value._hash, map._hash)) {
                    this.value._hash = null;
                    map._hash = null;
                }

            } else if (this.value.map.isEmpty() || Objects.equals(this.value.map, map.map)) {
                // isEmpty suggests that this is a brand new instance, and the '_hash' field
                // was set by Hibernate before the 'value' field was
                map._hash = this.value._hash;

            } else {
                this.value._hash = null;
                map._hash = null;
            }
        } else if (map._hash != null) {
            map._hash = null;
        }

        this.value = map;
    }


    @Transient
    @JsonIgnore
    @Override
    public boolean _isHashCalculated() {
        return this.value._hash != null;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(ExtraJson other) {
        if (other == null) return false;
        return Objects.equals(this.value.map, other.value.map);
    }

    @Transient
    @JsonIgnore
    public ObjectNode toJsonNode() {
        return new ObjectMapper().valueToTree(this.value);
    }

    @Transient
    @JsonIgnore
    @Override
    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this.value.map);
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.get_hash();
    }

    @Transient
    @JsonIgnore
    @Override
    public void _set(ExtraJson other) {
        this.value = other.value;
    }

    /**
     * A wrapper for the map representing the key-value pairs of the ExtraJson.  Hibernate is
     * not able to directly process a Map, but by wrapping it I am able to use a converter
     * to convert between the DB and the ORM
     */
    @Convert(converter = ExtraJson.Converter.class)
    public static class JsonMap {
        /**
         * Tree map is used so that the keys are always in "ASCII-betical" order,
         * so that the hashes of two otherwise identical objects will always match,
         * if the keys were added in a different order
         */
        private final TreeMap<String, JsonNode> map;

        /**
         * Immutable view of the map
         */
        private Map<String, JsonNode> view;

        /**
         * Instantiates a new empty JsonMap
         */
        public JsonMap() {
            this.map = new TreeMap<>();
        }

        /**
         * Instantiates a new JsonMap, copying the provided map
         *
         * @param map the map
         */
        public JsonMap(Map<String, JsonNode> map) {
            this.map = new TreeMap<>(map);
        }

        /**
         * Instantiates a new Json map by deserializing the provided string
         *
         * @param jsonStr the json string to deserialize
         * @throws JsonProcessingException  if there was an exception while deserializing
         * @throws IllegalArgumentException if the deserialized JsonNode is not an ObjectNode
         */
        public JsonMap(String jsonStr)
                throws JsonProcessingException, IllegalArgumentException {
            this();

            JsonNode top = new ObjectMapper().readValue(jsonStr, JsonNode.class);
            if (!(top instanceof ObjectNode)) {
                throw new IllegalArgumentException(
                        "Could not convert top-level node to ObjectNode:\n" + jsonStr);
            }

            Iterator<String> iterator = top.fieldNames();
            while (iterator.hasNext()) {
                String key = iterator.next();
                this.map.put(key, top.get(key));
            }
        }

        /**
         * Gets the immutable view of the map
         *
         * @return the view
         */
        public Map<String, JsonNode> getView() {
            if (this.view == null) {
                this.view = Collections.unmodifiableMap(this.map);
            }
            return this.view;
        }

        /**
         * Returns a mutable copy of the map
         *
         * @return the map
         */
        public Map<String, JsonNode> makeCopy() {
            return new TreeMap(map);
        }

        /**
         * Serializes the map into a string
         * @return
         */
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this.map);

            } catch (JsonProcessingException e) {
                logger.error(e);

                return "{\n \"error\":"
                        + escapeString(this.getClass().toString() + " could not convert map to json")
                        + ",\n \"map.toString()\":"
                        + escapeString(this.toString())
                        + ",\n \"exception.getMessage()\":"
                        + escapeString(e.getMessage())
                        + ",\n \"exception.toString()\":"
                        + escapeString(e.toString())
                        + ",\n}";
            }
        }

        public boolean equals(Object o) {
            if (o == null) return false;
            if (o == this) return true;
            if (!Objects.equals(this.getClass(), o.getClass())) {
                return false;
            }

            JsonMap other = (JsonMap) o;
            return Objects.equals(this.map, other.map);
        }

        private Hash _hash;

        /**
         * Calculates the hash of the JsonMap if it is not already calculated, or returns
         * the pre-calculated hash if it was calculated.
         *
         * @return the SHA-512 hash of the serialized map
         */
        public Hash get_hash() {
            if (this._hash == null) {
                this._hash = new Hash(this.toString());
            }
            return this._hash;
        }

        /**
         * Sets the hash after performing an immutability check
         *
         * @param hash the hash to set
         * @throws IllegalStateException if the hash is being changed to a value different
         * than previously set.  All values including the hash are intended to be immutable
         * once a lookup record is created
         */
        public void set_hash(Hash hash) throws IllegalStateException {
            if (this._hash != null && !Objects.equals(this._hash, hash)) {
                throw new IllegalStateException("Cannot alter the hash of a HashLookup (" +
                        this.getClass() +") after it has been set!");
            }
            this._hash = hash;
        }

        public int hashCode() {
            return this.get_hash().hashCode();
        }
    }

    /**
     * A singleton instance of the DB converter for static utility usage
     */
    public static final Converter converter = new Converter();

    /**
     * Hibernate converter.  Converts a JsonMap into a string for the database,
     * and a database string back into a JsonMap
     */
    @javax.persistence.Converter
    public static class Converter implements AttributeConverter<JsonMap, String> {
        private static final Logger logger = LogManager.getLogger(Converter.class);

        @Override
        public String convertToDatabaseColumn(JsonMap json) {
            if (json != null) {
                return json.toString();

            } else {
                return null;
            }

        }

        @Override
        public JsonMap convertToEntityAttribute(String json) {
            if (json == null) {
                return null;
            }

            try {
                return new JsonMap(json);

            } catch (Exception e) {
                logger.error(e);
                return null;
            }
        }
    }
}
