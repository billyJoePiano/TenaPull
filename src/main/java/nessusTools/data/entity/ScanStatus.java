package nessusTools.data.entity;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanStatus")
@Table(name = "scan_status")
public class ScanStatus extends LookupPojo<ScanStatus> {
    public static final LookupDao<ScanStatus> dao
            = new LookupDao<ScanStatus>(ScanStatus.class);

}