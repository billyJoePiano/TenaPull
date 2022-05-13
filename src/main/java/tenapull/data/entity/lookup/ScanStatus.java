package tenapull.data.entity.lookup;

import tenapull.data.entity.template.SimpleStringLookupPojo;
import tenapull.data.persistence.SimpleStringLookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents a simple string/varchar lookup from the scan_status table
 */
@Entity(name = "ScanStatus")
@Table(name = "scan_status")
public class ScanStatus extends SimpleStringLookupPojo<ScanStatus> {
    public static final SimpleStringLookupDao<ScanStatus> dao
            = new SimpleStringLookupDao<ScanStatus>(ScanStatus.class);

}