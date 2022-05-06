package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "HostNetbiosName")
@Table(name = "host_netbios_name")
public class HostNetbiosName extends SimpleStringLookupPojo<HostNetbiosName> {
    public static final SimpleStringLookupDao<HostNetbiosName> dao
            = new SimpleStringLookupDao<HostNetbiosName>(HostNetbiosName.class);

}