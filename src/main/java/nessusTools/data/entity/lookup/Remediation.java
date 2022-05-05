package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "Remediation")
@Table(name = "remediation")
public class Remediation extends StringHashLookupPojo<Remediation> {
    public static final StringHashLookupDao<Remediation> dao
            = new StringHashLookupDao<Remediation>(Remediation.class);

}