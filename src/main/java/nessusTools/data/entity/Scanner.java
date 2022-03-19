package nessusTools.data.entity;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "Scanner")
@Table(name = "scanner")
public class Scanner extends LookupPojo {
    public static final LookupDao<Scanner> dao
            = new LookupDao<Scanner>(Scanner.class);

}