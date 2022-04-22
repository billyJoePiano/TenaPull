package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity(name = "ScanOwnerId")
@Table(name = "scan_owner_id")
public class ScanOwnerNessusId extends NaturalIdPojo implements ObjectLookupPojo<ScanOwnerNessusId> {
    public static final ObjectLookupDao<ScanOwnerNessusId>
            dao = new ObjectLookupDao<>(ScanOwnerNessusId.class);

    @OneToOne
    @JoinColumn(name = "lookup_id")
    @JsonIgnore
    private ScanOwner scanOwner;

    public ScanOwner getScanOwner() {
        return scanOwner;
    }

    public void setScanOwner(ScanOwner scanOwner) {
        this.scanOwner = scanOwner;
    }

    @Override
    public void _set(ScanOwnerNessusId other) {
        this.setId(other.getId());
        this.scanOwner = other.scanOwner;
    }
}
