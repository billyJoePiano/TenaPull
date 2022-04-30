package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "HostIp")
@Table(name = "host_ip")
public class HostIp extends SimpleStringLookupPojo<HostIp> {
    public static final SimpleStringLookupDao<HostIp> dao
            = new SimpleStringLookupDao<HostIp>(HostIp.class);

}