package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "ScanOwner")
@Table(name = "scan_owner")
public class ScanOwner extends LookupPojo {
    public static final LookupDao<ScanOwner> dao
            = new LookupDao<ScanOwner>(ScanOwner.class);

    @OneToOne
    @JoinColumn(name = "lookup_id")
    @JsonIgnore
    private ScanOwnerNessusId scanOwnerId;

    public ScanOwnerNessusId getScanOwnerId() {
        return scanOwnerId;
    }

    public void setScanOwnerId(ScanOwnerNessusId scanOwnerId) {
        this.scanOwnerId = scanOwnerId;
    }
}