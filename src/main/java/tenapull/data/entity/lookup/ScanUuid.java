package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the scan_uuid table
 */
@Entity(name = "ScanUuid")
@Table(name = "scan_uuid")
public class ScanUuid extends SimpleStringLookupPojo<ScanUuid> {
    public static final SimpleStringLookupDao<ScanUuid> dao
            = new SimpleStringLookupDao<>(ScanUuid.class);
}
