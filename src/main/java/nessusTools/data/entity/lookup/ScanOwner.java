package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.*;

@Entity(name = "ScanOwner")
@Table(name = "scan_owner")
public class ScanOwner extends SimpleStringLookupPojo<ScanOwner> {
    public static final SimpleStringLookupDao<ScanOwner> dao
            = new SimpleStringLookupDao<ScanOwner>(ScanOwner.class);

    /*
    @OneToOne(mappedBy = "scanOwner")
    @JsonIgnore
    private ScanOwnerNessusId scanOwnerId;

    public ScanOwnerNessusId getScanOwnerId() {
        return scanOwnerId;
    }

    public void setScanOwnerId(ScanOwnerNessusId scanOwnerId) {
        this.scanOwnerId = scanOwnerId;
    }
     */
}