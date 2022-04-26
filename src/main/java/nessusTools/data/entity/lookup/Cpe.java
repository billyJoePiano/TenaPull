package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "Cpe")
@Table(name = "cpe")
public class Cpe extends LookupPojo<Cpe> {
    public static final LookupDao<Cpe> dao
            = new LookupDao<>(Cpe.class);
}
