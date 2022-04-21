package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "PluginRiskInformation")
@Table(name = "plugin_risk_information")
public class PluginRiskInformation extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginRiskInformation> {

    public static final ObjectLookupDao<PluginRiskInformation> dao
            = new ObjectLookupDao<PluginRiskInformation>(PluginRiskInformation.class);

    @Column(name = "cvss_temporal_vector")
    @JsonProperty("cvss_temporal_vector")
    String cvssTemporalVector;

    @Column(name = "risk_factor")
    @JsonProperty("risk_factor")
    String riskFactor;

    @Column(name = "cvss_vector")
    @JsonProperty("cvss_vector")
    String cvssVector;

    @Column(name = "cvss_temporal_score")
    @JsonProperty("cvss_temporal_score")
    String cvssTemporalScore;

    @Column(name = "cvss3_base_score")
    @JsonProperty("cvss3_base_score")
    String cvss3BaseScore;

    @Column(name = "cvss3_temporal_vector")
    @JsonProperty("cvss3_temporal_vector")
    String cvss3TemporalVector;

    @Column(name = "cvss3_temporal_score")
    @JsonProperty("cvss3_temporal_score")
    String cvss3TemporalScore;

    @Column(name = "cvss3_vector")
    @JsonProperty("cvss3_vector")
    String cvss3Vector;

    @Override
    public void _set(PluginRiskInformation o) {
        this.setId(o.getId());
        this.cvssTemporalVector = o.cvssTemporalVector;
        this.riskFactor = o.riskFactor;
        this.cvssVector = o.cvssVector;
        this.cvssTemporalScore = o.cvssTemporalScore;
        this.cvss3TemporalVector = o.cvss3TemporalVector;
        this.cvss3BaseScore = o.cvss3BaseScore;
        this.cvss3Vector = o.cvss3Vector;
        this.cvss3TemporalScore = o.cvss3TemporalScore;
    }
}
