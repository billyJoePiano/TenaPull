package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "ScanType")
@Table(name = "scan_type")
public class ScanType extends LookupPojo<ScanType> {
    public static final LookupDao<ScanType> dao
            = new LookupDao<ScanType>(ScanType.class);

}