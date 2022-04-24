package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "ScanOwnerId")
@Table(name = "scan_owner_id")
public class ScanOwnerNessusId implements ObjectLookupPojo<ScanOwnerNessusId> {

    public static final ObjectLookupDao<ScanOwnerNessusId>
            dao = new ObjectLookupDao<>(ScanOwnerNessusId.class);

    @Id
    @NaturalId
    @JsonProperty
    private int id;

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
        this.id = other.id;
        this.scanOwner = other.scanOwner;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public JsonNode toJsonNode() {
        return IntNode.valueOf(this.id);
    }

    @Override
    public String toJsonString() throws JsonProcessingException {
        return Integer.toString(this.id);
    }
}
