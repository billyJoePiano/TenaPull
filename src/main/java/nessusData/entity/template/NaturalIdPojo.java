package nessusData.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import nessusData.entity.*;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS;
import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import org.hibernate.annotations.NaturalId;

import java.util.*;

/*@JsonSubTypes({
        @Type(value = Folder.class, name = "Folder"),
        @Type(value = Scan.class, name = "Scan"),
        @Type(value = ScanInfo.class, name = "ScanInfo"),
        @Type(value = ScanGroup.class, name = "ScanGroup"),
        @Type(value = Acl.class, name = "Acl")
})*/


//@JsonTypeInfo(use = CLASS, include = PROPERTY, property = "type")

@MappedSuperclass
@JsonSubTypes({
        @Type(value = Folder.class),
        @Type(value = Scan.class),
        @Type(value = ScanInfo.class),
        @Type(value = ScanGroup.class),
        @Type(value = Acl.class)
})
public abstract class NaturalIdPojo implements Pojo {
    /*
    static {
        ObjectMapper mapper = new ObjectMapper();
        /*
        mapper.registerSubtypes(new NamedType(Folder.class, "Folder"));
        mapper.registerSubtypes(new NamedType(Scan.class, "Scan"));
        mapper.registerSubtypes(new NamedType(ScanInfo.class, "ScanInfo"));
        mapper.registerSubtypes(new NamedType(ScanGroup.class, "ScanGroup"));
        mapper.registerSubtypes(new NamedType(Acl.class, "Acl"));
         *//*

        mapper.registerSubtypes(Folder.class);
        mapper.registerSubtypes(Scan.class);
        mapper.registerSubtypes(ScanInfo.class);
        mapper.registerSubtypes(ScanGroup.class);
        mapper.registerSubtypes(Acl.class);
    }
    */

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
