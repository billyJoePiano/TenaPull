package nessusTools.data.entity.lookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "ScanOwner")
@Table(name = "scan_owner")
public class ScanOwner extends LookupPojo<ScanOwner> {
    public static final LookupDao<ScanOwner> dao
            = new LookupDao<ScanOwner>(ScanOwner.class);

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