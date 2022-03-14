package nessusData.entity;

import nessusData.entity.template.LookupPojo;
import nessusData.persistence.LookupDao;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "Scanner")
@Table(name = "scanner")
public class Scanner extends LookupPojo {
    public static final LookupDao<Scanner> dao
            = new LookupDao<Scanner>(Scanner.class);

}