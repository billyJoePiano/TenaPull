package nessusTools.data.entity.template;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import nessusTools.data.deserialize.*;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class NullableIdPojo<ME extends NullableIdPojo> extends NaturalIdPojo
        implements ObjectLookupPojo<ME> {

    @JsonProperty
    @JsonSerialize(using = NullableId.Serializer.class)
    @JsonDeserialize(using = NullableId.Deserializer.class)
    public int getId() {
        return super.getId();
    }
}
