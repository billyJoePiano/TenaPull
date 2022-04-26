package nessusTools.data.entity.objectLookup;

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




    public String getCvssTemporalVector() {
        return cvssTemporalVector;
    }

    public void setCvssTemporalVector(String cvssTemporalVector) {
        this.cvssTemporalVector = cvssTemporalVector;
    }

    public String getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(String riskFactor) {
        this.riskFactor = riskFactor;
    }

    public String getCvssVector() {
        return cvssVector;
    }

    public void setCvssVector(String cvssVector) {
        this.cvssVector = cvssVector;
    }

    public String getCvssTemporalScore() {
        return cvssTemporalScore;
    }

    public void setCvssTemporalScore(String cvssTemporalScore) {
        this.cvssTemporalScore = cvssTemporalScore;
    }

    public String getCvss3BaseScore() {
        return cvss3BaseScore;
    }

    public void setCvss3BaseScore(String cvss3BaseScore) {
        this.cvss3BaseScore = cvss3BaseScore;
    }

    public String getCvss3TemporalVector() {
        return cvss3TemporalVector;
    }

    public void setCvss3TemporalVector(String cvss3TemporalVector) {
        this.cvss3TemporalVector = cvss3TemporalVector;
    }

    public String getCvss3TemporalScore() {
        return cvss3TemporalScore;
    }

    public void setCvss3TemporalScore(String cvss3TemporalScore) {
        this.cvss3TemporalScore = cvss3TemporalScore;
    }

    public String getCvss3Vector() {
        return cvss3Vector;
    }

    public void setCvss3Vector(String cvss3Vector) {
        this.cvss3Vector = cvss3Vector;
    }

    @Override
    public void _set(PluginRiskInformation o) {
        this.__set(o);
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
