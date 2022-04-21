package nessusTools.data.entity;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanUuid")
@Table(name = "scan_uuid")
public class ScanUuid extends LookupPojo<ScanUuid> {
    public static final LookupDao<ScanUuid> dao
            = new LookupDao<>(ScanUuid.class);
}
