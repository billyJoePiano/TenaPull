package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the cvss_vector table
 */
@Entity(name = "CvssVector")
@Table(name = "cvss_vector")
public class CvssVector extends SimpleStringLookupPojo<CvssVector> {
    public static final SimpleStringLookupDao<CvssVector> dao
            = new SimpleStringLookupDao<CvssVector>(CvssVector.class);

}