package nessusTools.data.entity;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "ScanOwner")
@Table(name = "scan_owner")
public class ScanOwner extends LookupPojo {
    public static final LookupDao<ScanOwner> dao
            = new LookupDao<ScanOwner>(ScanOwner.class);

    /*
    @OneToOne
    private ScanOwnerId scanOwnerId;

    public ScanOwnerId getScanOwnerId() {
        return scanOwnerId;
    }

    public void setScanOwnerId(ScanOwnerId scanOwnerId) {
        this.scanOwnerId = scanOwnerId;
    }
     */
}