package tenapull.data.entity.lookup;

import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;

/**
 * Represents a simple string/varchar lookup from the cvss_temporal_vector table
 */
@Entity(name = "CvssTemporalVector")
@Table(name = "cvss_temporal_vector")
public class CvssTemporalVector extends SimpleStringLookupPojo<CvssTemporalVector> {
    public static final SimpleStringLookupDao<CvssTemporalVector> dao
            = new SimpleStringLookupDao<CvssTemporalVector>(CvssTemporalVector.class);

}