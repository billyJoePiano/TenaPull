package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.sql.*;
import java.time.*;

/**
 * Abstract implementation of the NessusResponse interface, which generates a timestamp at the time
 * of instantiation, because the Nessus API does not provide a timestamp.
 *
 * @param <RES> the nessus response type implementing NessusResponseGenerateTimestamp
 */
@MappedSuperclass
@JsonIgnoreProperties(allowSetters = true, value = {"id"})
public abstract class NessusResponseGenerateTimestamp<RES extends NessusResponseGenerateTimestamp<RES>>
        extends NaturalIdPojo
        implements NessusResponse<RES> { // (NessusResponse extends DbPojo)

    @JsonIgnore
    public abstract String getUrlPath();

    @JsonIgnore
    private Timestamp timestamp = Timestamp.from(Instant.now());

    @Override
    public Timestamp getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
