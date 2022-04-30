package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.SimpleStringLookupPojo;
import nessusTools.data.persistence.SimpleStringLookupDao;

import javax.persistence.*;

@Entity(name = "Timezone")
@Table(name = "timezone")
public class Timezone extends SimpleStringLookupPojo<Timezone> {
    public static final SimpleStringLookupDao<Timezone> dao
            = new SimpleStringLookupDao<Timezone>(Timezone.class);
}