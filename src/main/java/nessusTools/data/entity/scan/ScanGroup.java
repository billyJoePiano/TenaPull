package nessusTools.data.entity.scan;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.Dao;

import javax.persistence.*;

@Entity(name = "ScanGroup")
@Table(name = "scan_group")
public class ScanGroup extends NaturalIdPojo {
    public static final Dao<ScanGroup> dao
            = new Dao<ScanGroup>(ScanGroup.class);

}