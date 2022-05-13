package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the host_netbios_name table
 */
@Entity(name = "HostNetbiosName")
@Table(name = "host_netbios_name")
public class HostNetbiosName extends SimpleStringLookupPojo<HostNetbiosName> {
    public static final SimpleStringLookupDao<HostNetbiosName> dao
            = new SimpleStringLookupDao<HostNetbiosName>(HostNetbiosName.class);

}