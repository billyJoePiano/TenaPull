package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanScheduleType")
@Table(name = "scan_schedule_type")
public class ScanScheduleType extends LookupPojo<ScanScheduleType> {
    public static final LookupDao<ScanScheduleType> dao
            = new LookupDao<>(ScanScheduleType.class);
}
