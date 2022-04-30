package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanPolicy")
@Table(name = "scan_policy")
public class ScanPolicy extends SimpleStringLookupPojo<ScanPolicy> {
    public static final SimpleStringLookupDao<ScanPolicy> dao
            = new SimpleStringLookupDao<ScanPolicy>(ScanPolicy.class);

}