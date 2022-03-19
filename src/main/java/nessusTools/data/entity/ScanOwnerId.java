package nessusTools.data.entity;

import nessusTools.data.entity.template.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "ScanOwnerId")
@Table(name = "scan_owner_id")
public class ScanOwnerId extends NaturalIdPojo {
    @OneToOne
    @JoinColumn(name = "lookup_id")
    private ScanOwner scanOwner;

    public ScanOwner getScanOwner() {
        return scanOwner;
    }

    public void setScanOwner(ScanOwner scanOwner) {
        this.scanOwner = scanOwner;
    }
}
