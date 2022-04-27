package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import com.fasterxml.jackson.databind.node.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.*;

import nessusTools.data.deserialize.Lookup;

@MappedSuperclass
@JsonDeserialize(using = Lookup.Deserializer.class)
@JsonSerialize(using = Lookup.Serializer.class)
public abstract class LookupPojo<POJO extends LookupPojo<POJO>>
        implements LookupSearchMapProvider<POJO> {

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

        return Objects.equals(other.toString(), this.toString());
    }

    @Override
    public void _set(POJO other) {
        this.setId(other.getId());
        this.setValue(other.getValue());
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _lookupMatch(POJO other) {
        return Objects.equals(this.getValue(), other.getValue());
    }

    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return Map.of("value", this.value);
    }

}