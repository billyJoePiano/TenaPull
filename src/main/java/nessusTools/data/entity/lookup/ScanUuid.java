package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanUuid")
@Table(name = "scan_uuid")
public class ScanUuid extends SimpleStringLookupPojo<ScanUuid> {
    public static final SimpleStringLookupDao<ScanUuid> dao
            = new SimpleStringLookupDao<>(ScanUuid.class);
}
