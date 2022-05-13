package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the operating_system lookup table,
 * that is indexed by a SHA-512 hash
 */
@Entity(name = "OperatingSystem")
@Table(name = "operating_system")
public class OperatingSystem extends StringHashLookupPojo<OperatingSystem> {
    public static final StringHashLookupDao<OperatingSystem> dao
            = new StringHashLookupDao<OperatingSystem>(OperatingSystem.class);

}