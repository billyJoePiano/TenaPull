package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "PluginRiskInformation")
@Table(name = "plugin_risk_information")
public class PluginRiskInformation extends HashLookupTemplate<PluginRiskInformation> {

    public static final HashLookupDao<PluginRiskInformation> dao
            = new HashLookupDao<PluginRiskInformation>(PluginRiskInformation.class);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="risk_factor_id")
    @JsonProperty("risk_factor")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    PluginRiskFactor riskFactor;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_base_score_id")
    @JsonProperty("cvss_base_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ???
    VulnerabilityScore cvssBaseScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_vector_id")
    @JsonProperty("cvss_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    CvssVector cvssVector;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_temporal_score_id")
    @JsonProperty("cvss_temporal_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    VulnerabilityScore cvssTemporalScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_temporal_vector_id")
    @JsonProperty("cvss_temporal_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    CvssTemporalVector cvssTemporalVector;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_base_score_id")
    @JsonProperty("cvss3_base_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    VulnerabilityScore cvss3BaseScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_vector_id")
    @JsonProperty("cvss3_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    CvssVector cvss3Vector;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_temporal_score_id")
    @JsonProperty("cvss3_temporal_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    VulnerabilityScore cvss3TemporalScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_temporal_vector_id")
    @JsonProperty("cvss3_temporal_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    CvssTemporalVector cvss3TemporalVector;



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

    public PluginRiskFactor getRiskFactor() {
        return riskFactor;
    }

    public void setRiskFactor(PluginRiskFactor riskFactor) {
        this.riskFactor = riskFactor;
    }

    public VulnerabilityScore getCvssBaseScore() {
        return cvssBaseScore;
    }

    public void setCvssBaseScore(VulnerabilityScore cvssBaseScore) {
        this.cvssBaseScore = cvssBaseScore;
    }

    public CvssVector getCvssVector() {
        return cvssVector;
    }

    public void setCvssVector(CvssVector cvssVector) {
        this.cvssVector = cvssVector;
    }

    public VulnerabilityScore getCvssTemporalScore() {
        return cvssTemporalScore;
    }

    public void setCvssTemporalScore(VulnerabilityScore cvssTemporalScore) {
        this.cvssTemporalScore = cvssTemporalScore;
    }

    public CvssTemporalVector getCvssTemporalVector() {
        return cvssTemporalVector;
    }

    public void setCvssTemporalVector(CvssTemporalVector cvssTemporalVector) {
        this.cvssTemporalVector = cvssTemporalVector;
    }

    public VulnerabilityScore getCvss3BaseScore() {
        return cvss3BaseScore;
    }

    public void setCvss3BaseScore(VulnerabilityScore cvss3BaseScore) {
        this.cvss3BaseScore = cvss3BaseScore;
    }

    public CvssVector getCvss3Vector() {
        return cvss3Vector;
    }

    public void setCvss3Vector(CvssVector cvss3Vector) {
        this.cvss3Vector = cvss3Vector;
    }

    public VulnerabilityScore getCvss3TemporalScore() {
        return cvss3TemporalScore;
    }

    public void setCvss3TemporalScore(VulnerabilityScore cvss3TemporalScore) {
        this.cvss3TemporalScore = cvss3TemporalScore;
    }

    public CvssTemporalVector getCvss3TemporalVector() {
        return cvss3TemporalVector;
    }

    public void setCvss3TemporalVector(CvssTemporalVector cvss3TemporalVector) {
        this.cvss3TemporalVector = cvss3TemporalVector;
    }
}
