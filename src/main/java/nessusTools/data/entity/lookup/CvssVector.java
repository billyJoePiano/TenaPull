package nessusTools.data.entity.lookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "CvssVector")
@Table(name = "cvss_vector")
public class CvssVector extends SimpleStringLookupPojo<CvssVector> {
    public static final SimpleStringLookupDao<CvssVector> dao
            = new SimpleStringLookupDao<CvssVector>(CvssVector.class);

}