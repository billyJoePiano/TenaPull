package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

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