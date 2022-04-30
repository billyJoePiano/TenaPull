package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.*;

@Entity(name = "ScanType")
@Table(name = "scan_type")
public class ScanType extends SimpleStringLookupPojo<ScanType> {
    public static final SimpleStringLookupDao<ScanType> dao
            = new SimpleStringLookupDao<ScanType>(ScanType.class);

}