package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanGroup")
@Table(name = "scan_group")
public class ScanGroup extends GeneratedIdPojo
        /*implements ObjectLookupPojo<ScanGroup>*/ {

    //public static final ObjectLookupDao<ScanGroup> dao
    //        = new ObjectLookupDao<ScanGroup>(ScanGroup.class);

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() { }

    /*
    @Transient
    @JsonIgnore
    @Override
    public void _set(ScanGroup o) {
        this.__set(o);
    }
     */
}