package nessusData.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nessusData.entity.*;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import org.hibernate.annotations.NaturalId;

@MappedSuperclass
@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value= Folder.class, name = "Folder"),
        @JsonSubTypes.Type(value= Scan.class, name = "Scan"),
        @JsonSubTypes.Type(value= ScanInfo.class, name = "ScanInfo"),
        @JsonSubTypes.Type(value= ScanGroup.class, name = "ScanGroup"),
        @JsonSubTypes.Type(value= Acl.class, name = "Acl")
})
public abstract class NaturalIdPojo implements Pojo {
    // @NaturalId
    @Id
    @JsonProperty
    private int id;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
    }

    public String toJsonString() throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(this);
    }

    public String toString() {
        try {
            return this.toJsonString();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for '"
                    + super.toString() + "' :\n"
                    + e.getMessage();
        }
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null || !o.getClass().equals(this.getClass())) return false;
        Pojo other = (Pojo) o;
        return this.toJsonNode().equals(other.toJsonNode());

    }
}
