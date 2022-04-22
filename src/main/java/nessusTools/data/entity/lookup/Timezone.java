package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.LookupPojo;
import nessusTools.data.persistence.LookupDao;

import javax.persistence.*;

@Entity(name = "Timezone")
@Table(name = "timezone")
public class Timezone extends LookupPojo<Timezone> {
    public static final LookupDao<Timezone> dao
            = new LookupDao<Timezone>(Timezone.class);
}