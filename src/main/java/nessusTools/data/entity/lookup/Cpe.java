package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the cpe lookup table, that is indexed by a SHA-512 hash
 */
@Entity(name = "Cpe")
@Table(name = "cpe")
public class Cpe extends StringHashLookupPojo<Cpe> {
    public static final StringHashLookupDao<Cpe> dao
            = new StringHashLookupDao<Cpe>(Cpe.class);

}
