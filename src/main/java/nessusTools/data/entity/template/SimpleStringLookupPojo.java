package nessusTools.data.entity.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.*;

import nessusTools.data.deserialize.Lookup;

@MappedSuperclass
@JsonDeserialize(using = Lookup.Deserializer.class)
@JsonSerialize(using = Lookup.Serializer.class)
public abstract class SimpleStringLookupPojo<POJO extends SimpleStringLookupPojo<POJO>>
        implements DbPojo, StringLookupPojo<POJO> {

    public static final String FIELD_NAME = "value";

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    private int id;

    @Column
    @NaturalId
    private String value;

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
    @Override
    public void _prepare() { }

    @Override
    public int hashCode() {
        return Hash.hashCode(this.value);
    }

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