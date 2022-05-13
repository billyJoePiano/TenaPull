package tenapull.data.entity.lookup;

import tenapull.data.entity.template.SimpleStringLookupPojo;
import tenapull.data.persistence.SimpleStringLookupDao;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the scan_type table
 */
@Entity(name = "ScanType")
@Table(name = "scan_type")
public class ScanType extends SimpleStringLookupPojo<ScanType> {
    public static final SimpleStringLookupDao<ScanType> dao
            = new SimpleStringLookupDao<ScanType>(ScanType.class);

}