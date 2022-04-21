package nessusTools.data.entity;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanPolicy")
@Table(name = "scan_policy")
public class ScanPolicy extends LookupPojo<ScanPolicy> {
    public static final LookupDao<ScanPolicy> dao
            = new LookupDao<ScanPolicy>(ScanPolicy.class);

}