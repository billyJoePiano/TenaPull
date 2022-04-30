package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "HostFqdn")
@Table(name = "host_fqdn")
public class HostFqdn extends SimpleStringLookupPojo<HostFqdn> {
    public static final SimpleStringLookupDao<HostFqdn> dao
            = new SimpleStringLookupDao<HostFqdn>(HostFqdn.class);

}