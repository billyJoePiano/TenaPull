package nessusData.entity;

import nessusData.entity.template.LookupPojo;
import nessusData.persistence.LookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanStatus")
@Table(name = "scan_status")
public class ScanStatus extends LookupPojo {
    public static final LookupDao<ScanStatus> dao
            = new LookupDao<ScanStatus>(ScanStatus.class);

}