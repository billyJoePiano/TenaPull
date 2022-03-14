package nessusData.entity;

import nessusData.entity.template.LookupPojo;
import nessusData.persistence.LookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanPolicy")
@Table(name = "scan_policy")
public class ScanPolicy extends LookupPojo {
    public static final LookupDao<ScanPolicy> dao
            = new LookupDao<ScanPolicy>(ScanPolicy.class);

}