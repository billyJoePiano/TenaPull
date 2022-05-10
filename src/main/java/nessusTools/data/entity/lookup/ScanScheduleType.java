package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the scan_schedule_type table
 */
@Entity(name = "ScanScheduleType")
@Table(name = "scan_schedule_type")
public class ScanScheduleType extends SimpleStringLookupPojo<ScanScheduleType> {
    public static final SimpleStringLookupDao<ScanScheduleType> dao
            = new SimpleStringLookupDao<>(ScanScheduleType.class);
}
