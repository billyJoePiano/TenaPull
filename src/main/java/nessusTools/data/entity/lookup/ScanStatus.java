package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanStatus")
@Table(name = "scan_status")
public class ScanStatus extends SimpleStringLookupPojo<ScanStatus> {
    public static final SimpleStringLookupDao<ScanStatus> dao
            = new SimpleStringLookupDao<ScanStatus>(ScanStatus.class);

}