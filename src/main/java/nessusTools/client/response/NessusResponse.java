package nessusTools.client.response;

import java.lang.reflect.*;
import java.sql.Timestamp;
import java.util.*;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.sync.*;
import org.apache.logging.log4j.*;
import org.hibernate.annotations.*;

import javax.persistence.*;

@MappedSuperclass
public interface NessusResponse extends DbPojo {
    public String getUrlPath();

    public Timestamp getTimestamp();

    public void setTimestamp(Timestamp timestamp);
}
