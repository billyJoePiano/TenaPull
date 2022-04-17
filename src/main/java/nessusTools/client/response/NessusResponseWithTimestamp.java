package nessusTools.client.response;

import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;

import javax.persistence.*;
import java.sql.*;

@MappedSuperclass
public abstract class NessusResponseWithTimestamp
        extends GeneratedIdPojo
        implements NessusResponse { // (NessusResponse extends DbPojo)

    @JsonDeserialize(using = EpochTimestamp.Deserializer.class)
    @JsonSerialize(using = EpochTimestamp.Serializer.class)
    private Timestamp timestamp;

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}