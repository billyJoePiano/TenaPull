package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the url table
 */
@Entity(name = "Url")
@Table(name = "url")
public class Url extends SimpleStringLookupPojo<Url> {
    public static final SimpleStringLookupDao<Url> dao
            = new SimpleStringLookupDao<Url>(Url.class);

}