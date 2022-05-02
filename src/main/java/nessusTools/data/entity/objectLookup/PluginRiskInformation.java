package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "PluginRiskInformation")
@Table(name = "plugin_risk_information")
public class PluginRiskInformation extends HashLookupTemplate<PluginRiskInformation> {

    public static final HashLookupDao<PluginRiskInformation> dao
            = new HashLookupDao<PluginRiskInformation>(PluginRiskInformation.class);

    @Column(name = "cvss_temporal_vector")
    @JsonProperty("cvss_temporal_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    String cvssTemporalVector;

    @Column(name = "risk_factor")
    @JsonProperty("risk_factor")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    String riskFactor;

    @Column(name = "cvss_base_score")
    @JsonProperty("cvss_base_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ???
    String cvssBaseScore;

    @Column(name = "cvss_vector")
    @JsonProperty("cvss_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String cvssVector;

    @Column(name = "cvss_temporal_score")
    @JsonProperty("cvss_temporal_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String cvssTemporalScore;

    @Column(name = "cvss3_base_score")
    @JsonProperty("cvss3_base_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String cvss3BaseScore;

    @Column(name = "cvss3_temporal_vector")
    @JsonProperty("cvss3_temporal_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String cvss3TemporalVector;

    @Column(name = "cvss3_temporal_score")
    @JsonProperty("cvss3_temporal_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String cvss3TemporalScore;

    @Column(name = "cvss3_vector")
    @JsonProperty("cvss3_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    String cvss3Vector;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(PluginRiskInformation o) {
        this.__set(o);
        this.cvssTemporalVector = o.cvssTemporalVector;
        this.riskFactor = o.riskFactor;
        this.cvssBaseScore = o.cvssBaseScore;
        this.cvssVector = o.cvssVector;
        this.cvssTemporalScore = o.cvssTemporalScore;
        this.cvss3TemporalVector = o.cvss3TemporalVector;
        this.cvss3BaseScore = o.cvss3BaseScore;
        this.cvss3Vector = o.cvss3Vector;
        this.cvss3TemporalScore = o.cvss3TemporalScore;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginRiskInformation o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.cvssTemporalVector, o.cvssTemporalVector)
                && Objects.equals(this.riskFactor, o.riskFactor)
                && Objects.equals(this.cvssBaseScore, o.cvssBaseScore)
                && Objects.equals(this.cvssVector, o.cvssVector)
                && Objects.equals(this.cvssTemporalScore, o.cvssTemporalScore)
                && Objects.equals(this.cvss3TemporalVector, o.cvss3TemporalVector)
                && Objects.equals(this.cvss3BaseScore, o.cvss3BaseScore)
                && Objects.equals(this.cvss3Vector, o.cvss3Vector)
                && Objects.equals(this.cvss3TemporalScore, o.cvss3TemporalScore)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }


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

    public String getCvssBaseScore() {
        return cvssBaseScore;
    }

    public void setCvssBaseScore(String cvssBaseScore) {
        this.cvssBaseScore = cvssBaseScore;
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
}
