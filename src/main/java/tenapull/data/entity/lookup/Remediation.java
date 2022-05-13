package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a MySQL longtext field from the remediation lookup table,
 * that is indexed by a SHA-512 hash
 */
@Entity(name = "Remediation")
@Table(name = "remediation")
public class Remediation extends StringHashLookupPojo<Remediation> {
    public static final StringHashLookupDao<Remediation> dao
            = new StringHashLookupDao<Remediation>(Remediation.class);

}