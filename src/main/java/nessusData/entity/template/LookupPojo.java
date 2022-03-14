package nessusData.entity.template;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nessusData.entity.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

/*
@JsonTypeInfo(use = NAME, include = PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value= ScanType.class, name = "ScanType"),
        @JsonSubTypes.Type(value= ScanOwner.class, name = "ScanOwner"),
        @JsonSubTypes.Type(value= Timezone.class, name = "Timezone"),
        @JsonSubTypes.Type(value= Scanner.class, name = "Scanner"),
        @JsonSubTypes.Type(value= ScanPolicy.class, name = "ScanPolicy"),
        @JsonSubTypes.Type(value= ScanPolicy.class, name = "ScanStatus")
})
*/

@MappedSuperclass
@JsonSubTypes({
        @JsonSubTypes.Type(value= ScanType.class),
        @JsonSubTypes.Type(value= ScanOwner.class),
        @JsonSubTypes.Type(value= Timezone.class),
        @JsonSubTypes.Type(value= Scanner.class),
        @JsonSubTypes.Type(value= ScanPolicy.class),
        @JsonSubTypes.Type(value= ScanPolicy.class)
})
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

    public JsonNode toJsonNode() {
        return new ObjectMapper().convertValue(this, JsonNode.class);
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

        return      other.getId()       == this.getId()
                &&  other.toString()    == this.toString();
    }
}