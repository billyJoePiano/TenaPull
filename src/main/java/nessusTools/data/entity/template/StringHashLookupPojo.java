package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import java.util.*;

@MappedSuperclass
@JsonDeserialize(using = Lookup.Deserializer.class)
@JsonSerialize(using = Lookup.Serializer.class)
public abstract class StringHashLookupPojo<POJO extends StringHashLookupPojo<POJO>>
        implements HashLookupPojo<POJO>, StringLookupPojo<POJO> {

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

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        if (this.value != null && !Objects.equals(this.value, value)) {
            throw new IllegalStateException("Cannot alter the value of a StringLookup (" +
                    this.getClass() +") after it has been set!");
        }
        this.value = value;
    }

    @Transient
    @JsonIgnore
    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this.getValue(), TextNode.class);
    }

    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String toString() {
        return this.getValue();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if(!Objects.equals(o.getClass(), this.getClass())) return false;

        POJO other = (POJO) o;

        return Objects.equals(other.getValue(), this.getValue());
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
    public int hashCode() {
        return this.get_hash().hashCode();
    }


    @JsonIgnore
    @Override
    public Hash get_hash() {
        if (this._hash == null) {
            this._hash = new Hash(this.value);
        }
        return this._hash;
    }

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