package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "OperatingSystem")
@Table(name = "operating_system")
public class OperatingSystem extends SimpleStringLookupPojo<OperatingSystem> {
    public static final SimpleStringLookupDao<OperatingSystem> dao
            = new SimpleStringLookupDao<OperatingSystem>(OperatingSystem.class);

}