package tenapull.data.entity.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.databind.node.*;
import tenapull.data.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.*;

import tenapull.data.deserialize.Lookup;

/**
 * Abstract super class for all simple string lookup entities.  Unlike the StringHashLookupPojo
 * entities, these entities have strings which all fit into 255 or less characters, so can
 * be indexed by MySQL.  Most have a varchar(255) field, although some lookups known to
 * have consistently short text strings may be shorter than this to improve DB indexing
 * efficiency
 *
 * @param <POJO> The class implementing SimpleStringLookupPojo
 */
@MappedSuperclass
@JsonDeserialize(using = Lookup.Deserializer.class)
@JsonSerialize(using = Lookup.Serializer.class)
public abstract class SimpleStringLookupPojo<POJO extends SimpleStringLookupPojo<POJO>>
        implements StringLookupPojo<POJO> {

    /**
     * The name of the field containing the lookup string
     */
    public static final String FIELD_NAME = "value";

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private int id;

    @Column
    @NaturalId
    private String value;

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
     * @throws IllegalStateException if the value field has already been set and the
     * value being set does not match a pre-existing value.  These record are meant to
     * be immutable in the DB once created
     */
    public void setValue(String value) {
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
    @Override
    public void _prepare() { }

    /**
     * Create java hashCode using the value string, for use
     * in a HashMap or HashSet
     * @return
     */
    @Override
    public int hashCode() {
        return Hash.hashCode(this.value);
    }

    /**
     * For sorting the lookups in "ASCII-betical" order on the
     * basis of their value string
     *
     * @param other the other lookup to compare against
     * @return
     */
    public int compareTo(POJO other) {
        if (other == null) return -1;
        String theirs = other.getValue();
        if (this.value == null) {
            if (theirs == null) return 0;
            else return -theirs.compareTo(null);
        }
        return this.getValue().compareTo(theirs);
    }
}