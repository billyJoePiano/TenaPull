package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the scan_owner table
 */
@Entity(name = "ScanOwner")
@Table(name = "scan_owner")
public class ScanOwner extends SimpleStringLookupPojo<ScanOwner> {
    public static final SimpleStringLookupDao<ScanOwner> dao
            = new SimpleStringLookupDao<ScanOwner>(ScanOwner.class);
}