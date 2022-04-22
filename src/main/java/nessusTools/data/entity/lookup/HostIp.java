package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "HostIp")
@Table(name = "host_ip")
public class HostIp extends LookupPojo<HostIp> {
    public static final LookupDao<HostIp> dao
            = new LookupDao<HostIp>(HostIp.class);

}