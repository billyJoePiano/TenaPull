package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

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