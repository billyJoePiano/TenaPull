package tenapull.data.entity.lookup;

import tenapull.data.entity.template.SimpleStringLookupPojo;
import tenapull.data.persistence.SimpleStringLookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents a simple string/varchar lookup from the scan_policy table
 */
@Entity(name = "ScanPolicy")
@Table(name = "scan_policy")
public class ScanPolicy extends SimpleStringLookupPojo<ScanPolicy> {
    public static final SimpleStringLookupDao<ScanPolicy> dao
            = new SimpleStringLookupDao<ScanPolicy>(ScanPolicy.class);

}