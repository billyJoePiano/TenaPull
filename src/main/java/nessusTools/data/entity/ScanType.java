package nessusTools.data.entity;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "ScanType")
@Table(name = "scan_type")
public class ScanType extends LookupPojo {
    public static final LookupDao<ScanType> dao
            = new LookupDao<ScanType>(ScanType.class);

}