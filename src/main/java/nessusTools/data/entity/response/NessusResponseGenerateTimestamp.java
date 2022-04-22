package nessusTools.data.entity.response;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import java.sql.*;

@MappedSuperclass
public abstract class NessusResponseGenerateTimestamp
        extends NaturalIdPojo
        implements NessusResponse { // (NessusResponse extends DbPojo)

    @UpdateTimestamp
    @JsonIgnore
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
