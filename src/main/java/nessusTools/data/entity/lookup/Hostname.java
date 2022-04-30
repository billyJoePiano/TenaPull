package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "Hostname")
@Table(name = "hostname")
public class Hostname extends SimpleStringLookupPojo<Hostname> {
    public static final SimpleStringLookupDao<Hostname> dao
            = new SimpleStringLookupDao<Hostname>(Hostname.class);

}