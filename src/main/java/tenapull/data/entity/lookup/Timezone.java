package tenapull.data.entity.lookup;

import tenapull.data.entity.template.SimpleStringLookupPojo;
import tenapull.data.persistence.SimpleStringLookupDao;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the timezone table
 */
@Entity(name = "Timezone")
@Table(name = "timezone")
public class Timezone extends SimpleStringLookupPojo<Timezone> {
    public static final SimpleStringLookupDao<Timezone> dao
            = new SimpleStringLookupDao<Timezone>(Timezone.class);
}