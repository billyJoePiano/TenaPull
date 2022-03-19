package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.naming.*;
import javax.persistence.*;
import java.util.*;

import nessusTools.data.deserialize.Lookup;

@MappedSuperclass
@JsonDeserialize(using = Lookup.Deserializer.class)
@JsonSerialize(using = Lookup.Serializer.class)
public abstract class LookupPojo implements Pojo {
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

    @Transient
    @JsonIgnore
    @Override
    public Map<String, JsonNode> _getExtraJson() {
        throw new UnsupportedOperationException("_getExtraJson() is not supported by LookupPojo");
    }

    @Transient
    @JsonIgnore
    @Override
    public void _setExtraJson(Map<String, JsonNode> _extraJson) {
        throw new UnsupportedOperationException("_setExtraJson(map) is not supported by LookupPojo");
    }

    @Transient
    @JsonIgnore
    @Override
    public void _putExtraJson(String key, Object value) {
        throw new UnsupportedOperationException("_putExtraJson(key, value) is not supported by LookupPojo");
    }

    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this.getValue(), JsonNode.class);
    }

    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String toString() {
        return this.getValue();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;

        } else if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        LookupPojo other = (LookupPojo) o;

        return Objects.equals(other.toString(), this.toString());
    }
}