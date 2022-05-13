package tenapull.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.data.deserialize.*;
import tenapull.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import java.util.*;

/**
 * Abstract super class of all StringHashLookups.  This is used for lookup tables
 * that may have a string longer than 255 characters.  Because longtext fields cannot
 * be indexed by MySQL, we instead use a SHA-512 hash to index it with a 'unique'
 * constraint.
 *
 * @param <POJO> the type implementing StringHashLookupPojo
 */
@MappedSuperclass
@JsonDeserialize(using = Lookup.Deserializer.class)
@JsonSerialize(using = Lookup.Serializer.class)
public abstract class StringHashLookupPojo<POJO extends StringHashLookupPojo<POJO>>
        implements HashLookupPojo<POJO>, StringLookupPojo<POJO> {

    /**
     * The name of the field containing the lookup string
     */
    public static final String FIELD_NAME = "value";

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private int id;

    @Column
    private String value;

    @NaturalId
    @Access(AccessType.PROPERTY)
    @Column(name = "_hash")
    @Convert(converter = Hash.Converter.class)
    @JsonIgnore
    private Hash _hash;

    /**
     * Get the surrogate primary key id for the lookup
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Set the surrogate primary key id for the lookup
     * @param id the id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the string lookup value this record represents
     * @return the string lookup value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * Sets the string lookup value this record represents, after doing an immutability check
     *
     * @param value the string lookup value to set
     * @throws IllegalStateException if the _hash field has already been set and the
     * value being set does not match a pre-existing value.  These record are meant to
     * be immutable in the DB once created, and the hash should always accurately reflect
     * the hash of the string value
     */
    public void setValue(String value) throws IllegalStateException {
        if (this.value != null && !Objects.equals(this.value, value)) {
            throw new IllegalStateException("Cannot alter the value of a StringLookup (" +
                    this.getClass() +") after it has been set!");
        }
        this.value = value;
    }

    /**
     * Converts this lookup entity into a JSON TextNode
     *
     * @return a JSON TextNode representing the string value of this entity
     */
    @Transient
    @JsonIgnore
    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this.getValue(), TextNode.class);
    }

    /**
     * Converts this lookup entity's value into a JSON-escaped String
     *
     * @return the lookup string value as a JSON-escaped String
     * @throws JsonProcessingException
     */
    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    /**
     * @return the lookup value of this entity
     */
    public String toString() {
        return this.getValue();
    }

    /**
     * Returns true if and only if the compared object is of the same class and
     * they share the same lookup value
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if(!Objects.equals(o.getClass(), this.getClass())) return false;

        POJO other = (POJO) o;

        return Objects.equals(other.getValue(), this.getValue());
    }

    /**
     * Prepares this entity for saving/inserting into the DB
     */
    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.get_hash();
    }

    /**
     * Creates a java hashCode for use in a HashMap or HashSet
     * @return
     */
    @Transient
    @JsonIgnore
    @Override
    public int hashCode() {
        return this.get_hash().hashCode();
    }


    /**
     * Generates the SHA-512 hash of the lookup value if it does not exist, or returns
     * the already-existing hash if it does.
     * @return
     */
    @JsonIgnore
    @Override
    public Hash get_hash() {
        if (this._hash == null) {
            this._hash = new Hash(this.value);
        }
        return this._hash;
    }

    /**
     * Sets the hash of the lookup after doing an immutability check.
     *
     * @param hash
     * @throws IllegalStateException if the _hash field has already been set and the
     * hash being set does not match the pre-existing hash.  These record are meant to
     * be immutable in the DB once created, and the hash should always accurately reflect
     * the hash of the string value
     */
    @JsonIgnore
    @Override
    public void set_hash(Hash hash) throws IllegalStateException {
        if (this._hash != null && !Objects.equals(this._hash, hash)) {
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

    @Override
    public void _set(POJO other) {
        this.set_hash(other.get_hash());
        this.setValue(other.getValue());
    }

    public boolean _match(POJO o) {
        if (this._hash != null && o._isHashCalculated()) {
            return Objects.equals(this._hash, o.get_hash());

        } else {
            return Objects.equals(this.value, o.getValue());
        }
    }
}