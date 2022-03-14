package nessusData.entity.template;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nessusData.entity.*;
import org.hibernate.annotations.*;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

/*
@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value= SeverityBase.class, name = "SeverityBase"),
        @JsonSubTypes.Type(value= License.class, name = "License")
})
 */


@MappedSuperclass
@JsonSubTypes({
        @JsonSubTypes.Type(value= SeverityBase.class),
        @JsonSubTypes.Type(value= License.class)
})
public abstract class GeneratedIdPojo implements Pojo {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    @JsonIgnore
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