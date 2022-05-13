package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the hostname table
 */
@Entity(name = "Hostname")
@Table(name = "hostname")
public class Hostname extends SimpleStringLookupPojo<Hostname> {
    public static final SimpleStringLookupDao<Hostname> dao
            = new SimpleStringLookupDao<Hostname>(Hostname.class);

}