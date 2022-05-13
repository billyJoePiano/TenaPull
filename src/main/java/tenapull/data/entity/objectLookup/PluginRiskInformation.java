package tenapull.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import tenapull.data.entity.lookup.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a reusable "object lookup", for the "risk_information" object included
 * in the plugin attributes returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "PluginRiskInformation")
@Table(name = "plugin_risk_information")
public class PluginRiskInformation extends HashLookupTemplate<PluginRiskInformation> {

    /**
     * The dao for PluginRiskInformation
     */
    public static final HashLookupDao<PluginRiskInformation> dao
            = new HashLookupDao<PluginRiskInformation>(PluginRiskInformation.class);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="risk_factor_id")
    @JsonProperty("risk_factor")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private PluginRiskFactor riskFactor;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_base_score_id")
    @JsonProperty("cvss_base_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ???
    private VulnerabilityScore cvssBaseScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_vector_id")
    @JsonProperty("cvss_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CvssVector cvssVector;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_temporal_score_id")
    @JsonProperty("cvss_temporal_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private VulnerabilityScore cvssTemporalScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss_temporal_vector_id")
    @JsonProperty("cvss_temporal_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private CvssTemporalVector cvssTemporalVector;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_base_score_id")
    @JsonProperty("cvss3_base_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private VulnerabilityScore cvss3BaseScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_vector_id")
    @JsonProperty("cvss3_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private CvssVector cvss3Vector;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_temporal_score_id")
    @JsonProperty("cvss3_temporal_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private VulnerabilityScore cvss3TemporalScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="cvss3_temporal_vector_id")
    @JsonProperty("cvss3_temporal_vector")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CvssTemporalVector cvss3TemporalVector;



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

    /**
     * Gets risk factor.
     *
     * @return the risk factor
     */
    public PluginRiskFactor getRiskFactor() {
        return riskFactor;
    }

    /**
     * Sets risk factor.
     *
     * @param riskFactor the risk factor
     */
    public void setRiskFactor(PluginRiskFactor riskFactor) {
        this.riskFactor = riskFactor;
    }

    /**
     * Gets cvss base score.
     *
     * @return the cvss base score
     */
    public VulnerabilityScore getCvssBaseScore() {
        return cvssBaseScore;
    }

    /**
     * Sets cvss base score.
     *
     * @param cvssBaseScore the cvss base score
     */
    public void setCvssBaseScore(VulnerabilityScore cvssBaseScore) {
        this.cvssBaseScore = cvssBaseScore;
    }

    /**
     * Gets cvss vector.
     *
     * @return the cvss vector
     */
    public CvssVector getCvssVector() {
        return cvssVector;
    }

    /**
     * Sets cvss vector.
     *
     * @param cvssVector the cvss vector
     */
    public void setCvssVector(CvssVector cvssVector) {
        this.cvssVector = cvssVector;
    }

    /**
     * Gets cvss temporal score.
     *
     * @return the cvss temporal score
     */
    public VulnerabilityScore getCvssTemporalScore() {
        return cvssTemporalScore;
    }

    /**
     * Sets cvss temporal score.
     *
     * @param cvssTemporalScore the cvss temporal score
     */
    public void setCvssTemporalScore(VulnerabilityScore cvssTemporalScore) {
        this.cvssTemporalScore = cvssTemporalScore;
    }

    /**
     * Gets cvss temporal vector.
     *
     * @return the cvss temporal vector
     */
    public CvssTemporalVector getCvssTemporalVector() {
        return cvssTemporalVector;
    }

    /**
     * Sets cvss temporal vector.
     *
     * @param cvssTemporalVector the cvss temporal vector
     */
    public void setCvssTemporalVector(CvssTemporalVector cvssTemporalVector) {
        this.cvssTemporalVector = cvssTemporalVector;
    }

    /**
     * Gets cvss 3 base score.
     *
     * @return the cvss 3 base score
     */
    public VulnerabilityScore getCvss3BaseScore() {
        return cvss3BaseScore;
    }

    /**
     * Sets cvss 3 base score.
     *
     * @param cvss3BaseScore the cvss 3 base score
     */
    public void setCvss3BaseScore(VulnerabilityScore cvss3BaseScore) {
        this.cvss3BaseScore = cvss3BaseScore;
    }

    /**
     * Gets cvss 3 vector.
     *
     * @return the cvss 3 vector
     */
    public CvssVector getCvss3Vector() {
        return cvss3Vector;
    }

    /**
     * Sets cvss 3 vector.
     *
     * @param cvss3Vector the cvss 3 vector
     */
    public void setCvss3Vector(CvssVector cvss3Vector) {
        this.cvss3Vector = cvss3Vector;
    }

    /**
     * Gets cvss 3 temporal score.
     *
     * @return the cvss 3 temporal score
     */
    public VulnerabilityScore getCvss3TemporalScore() {
        return cvss3TemporalScore;
    }

    /**
     * Sets cvss 3 temporal score.
     *
     * @param cvss3TemporalScore the cvss 3 temporal score
     */
    public void setCvss3TemporalScore(VulnerabilityScore cvss3TemporalScore) {
        this.cvss3TemporalScore = cvss3TemporalScore;
    }

    /**
     * Gets cvss 3 temporal vector.
     *
     * @return the cvss 3 temporal vector
     */
    public CvssTemporalVector getCvss3TemporalVector() {
        return cvss3TemporalVector;
    }

    /**
     * Sets cvss 3 temporal vector.
     *
     * @param cvss3TemporalVector the cvss 3 temporal vector
     */
    public void setCvss3TemporalVector(CvssTemporalVector cvss3TemporalVector) {
        this.cvss3TemporalVector = cvss3TemporalVector;
    }
}
