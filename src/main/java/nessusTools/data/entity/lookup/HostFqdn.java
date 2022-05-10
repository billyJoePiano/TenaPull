package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the host_fqdn table
 */
@Entity(name = "HostFqdn")
@Table(name = "host_fqdn")
public class HostFqdn extends SimpleStringLookupPojo<HostFqdn> {
    public static final SimpleStringLookupDao<HostFqdn> dao
            = new SimpleStringLookupDao<HostFqdn>(HostFqdn.class);

}