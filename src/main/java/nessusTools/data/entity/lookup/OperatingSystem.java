package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "OperatingSystem")
@Table(name = "operating_system")
public class OperatingSystem extends LookupPojo<OperatingSystem> {
    public static final LookupDao<OperatingSystem> dao
            = new LookupDao<OperatingSystem>(OperatingSystem.class);

}