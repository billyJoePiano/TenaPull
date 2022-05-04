package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanTargets")
@Table(name = "scan_targets")
public class ScanTargets extends StringHashLookupPojo<ScanTargets> {
    public static final StringHashLookupDao<ScanTargets> dao
            = new StringHashLookupDao<ScanTargets>(ScanTargets.class);

}