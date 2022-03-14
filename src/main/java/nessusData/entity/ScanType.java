package nessusData.entity;

import nessusData.entity.template.LookupPojo;
import nessusData.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "ScanType")
@Table(name = "scan_type")
public class ScanType extends LookupPojo {
    public static final LookupDao<ScanType> dao
            = new LookupDao<ScanType>(ScanType.class);

}