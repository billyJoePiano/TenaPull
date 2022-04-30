package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.apache.logging.log4j.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ExtraJson")
@Table(name = "extra_json")
public class ExtraJson implements HashLookupPojo<ExtraJson> {
    public static final HashLookupDao<ExtraJson> dao = new HashLookupDao<>(ExtraJson.class);


    private static final Logger logger = LogManager.getLogger(ExtraJson.class);

    public static String escapeString(String str) {
        return "\"" + new String(JsonStringEncoder.getInstance().quoteAsString(str)) + "\"";
    }

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

    @NaturalId
    @Access(AccessType.PROPERTY)
    @Column(name = "_hash")
    @JsonIgnore
    private byte[] _hash;

    public ExtraJson() {
        this.value = new JsonMap();
    }

    public ExtraJson(ExtraJson copyExceptId) {
        this(copyExceptId.value.map);
    }

    public ExtraJson(Map<String, JsonNode> value)
            throws NullPointerException {

        if (value == null) {
            throw new NullPointerException();
        }
        this.value = new JsonMap(value);
    }

    public ExtraJson(String jsonStr)
            throws JsonProcessingException, IllegalArgumentException {

        this.value = new JsonMap(jsonStr);
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }

        ExtraJson other = (ExtraJson)o;

        return Objects.equals(this.value, other.value);
    }

    @Transient
    public JsonNode put(String key, JsonNode value) throws IllegalStateException {
        if (this.id != 0) throw new IllegalStateException(
                "Cannot mutate ExtraJson after record has been inserted!  Create a copy and insert as a new record instead");
        return this.value.map.put(key, value);
    }

    @Transient
    public JsonNode get(String key) {
        return value.map.get(key);
    }

    public String toString() {
        return this.value.toString();
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public JsonMap getValue() {
        return this.value;
    }

    public void setValue(JsonMap map) throws NullPointerException {
        if (map == null) throw new NullPointerException();
        this.value = map;
    }

    @JsonIgnore
    @Override
    public byte[] get_hash() {
        if (this._hash == null) {
            this._hash = Hash.Sha512(this.toString());
        }
        return this._hash;
    }

    @JsonIgnore
    @Override
    public void set_hash(byte[] hash) throws IllegalStateException {
        if (this._hash != null && !Hash.equals(this._hash, hash)) {
            throw new IllegalStateException("Cannot alter the hash of a HashLookup (" +
                    this.getClass() +") after it has been set!");
        }
        this._hash = hash;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _isHashCalculated() {
        return this._hash != null;
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
        return new ObjectMapper().convertValue(this.value, ObjectNode.class);
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

    @Convert(converter = ExtraJson.Converter.class)
    public static class JsonMap {
        private final TreeMap<String, JsonNode> map;
        private Map<String, JsonNode> view;

        public JsonMap() {
            this.map = new TreeMap<>();
        }

        public JsonMap(Map<String, JsonNode> map) {
            this.map = new TreeMap<>(map);
        }

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

        public Map<String, JsonNode> getView() {
            if (this.view == null) {
                this.view = Collections.unmodifiableMap(this.map);
            }
            return this.view;
        }

        public Map<String, JsonNode> makeCopy() {
            return new TreeMap(map);
        }

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
    }

    public static final Converter converter = new Converter();

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
