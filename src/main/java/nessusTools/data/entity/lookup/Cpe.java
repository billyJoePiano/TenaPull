package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "Cpe")
@Table(name = "cpe")
public class Cpe extends SimpleStringLookupPojo<Cpe> {
    public static final SimpleStringLookupDao<Cpe> dao
            = new SimpleStringLookupDao<>(Cpe.class);
}
