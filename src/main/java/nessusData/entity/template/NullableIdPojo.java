package nessusData.entity.template;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nessusData.serialize.NullableId;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NullableIdPojo extends NaturalIdPojo {
    @JsonProperty
    @JsonSerialize(using = NullableId.Serializer.class)
    @JsonDeserialize(using = NullableId.Deserializer.class)
    public int getId() {
        return super.getId();
    }
}
