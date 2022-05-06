package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "CvssTemporalVector")
@Table(name = "cvss_temporal_vector")
public class CvssTemporalVector extends SimpleStringLookupPojo<CvssTemporalVector> {
    public static final SimpleStringLookupDao<CvssTemporalVector> dao
            = new SimpleStringLookupDao<CvssTemporalVector>(CvssTemporalVector.class);

}