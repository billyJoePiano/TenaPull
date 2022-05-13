package tenapull.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import tenapull.data.deserialize.*;
import tenapull.data.entity.template.*;

import javax.persistence.*;
import java.sql.*;

/**
 * Abstract implementation of the NessusResponse interface, which uses the timestamp
 * provided by the Nessus API
 *
 * @param <RES> the nessus response type implementing NessusResponseWithTimestamp
 */
@MappedSuperclass
public abstract class NessusResponseWithTimestamp<RES extends NessusResponseWithTimestamp<RES>>
        extends GeneratedIdPojo
        implements NessusResponse<RES> { // (NessusResponse extends DbPojo)

    @JsonIgnore
    public abstract String getUrlPath();

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
