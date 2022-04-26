package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "PluginAttributes")
@Table(name = "plugin_attributes")
public class PluginAttributes extends GeneratedIdPojo
        implements ObjectLookupPojo<PluginAttributes> {

    public static final ObjectLookupDao<PluginAttributes> dao
            = new ObjectLookupDao<PluginAttributes>(PluginAttributes.class);

    @Column(name = "threat_intensity_last_28")
    @JsonProperty("threat_intensity_last_28")
    String threatIntensityLast28;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "synopsis_id")
    PluginSynopsis synopsis;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "script_copyright_id")
    @JsonProperty("script_copyright")
    PluginScriptCopyright scriptCopyright;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "description_id")
    PluginDescription description;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "risk_information_id")
    @JsonProperty("risk_information")
    @JsonDeserialize(using = ObjectLookup.Deserializer.class)
    PluginRiskInformation riskInformation;


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "plugin_attributes_ref_information",
            joinColumns = { @JoinColumn(name = "attributes_id") },
            inverseJoinColumns = { @JoinColumn(name = "ref_id") }
    )
    @OrderColumn(name = "__order_for_plugin_attributes_ref_information", nullable = false)
    @Access(AccessType.PROPERTY)
    @JsonProperty("ref_information")
    @JsonDeserialize(using = RefInformation.Deserializer.class)
    @JsonSerialize(using = RefInformation.Serializer.class)
    List<PluginRefInformation> refInformation;

    @Column(name = "threat_sources_last_28")
    @JsonProperty("threat_sources_last_28")
    String threatSourcesLast28;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "plugin_name_id")
    @JsonProperty("plugin_name")
    PluginName pluginName;

    @Column(name = "vpr_score")
    @JsonProperty("vpr_score")
    String vprScore;

    @Column(name = "cvss_score_source")
    @JsonProperty("cvss_score_source")
    String cvssScoreSource;

    @Column(name = "see_also_id")
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "plugin_attributes_see_also",
            joinColumns = { @JoinColumn(name = "see_also_id") },
            inverseJoinColumns = { @JoinColumn(name = "attributes_id") }
    )
    @OrderColumn(name = "__order_for_plugin_attributes_see_also", nullable = false)
    @JsonProperty("see_also")
    List<PluginSeeAlso> seeAlso;

    @Column(name = "product_coverage")
    @JsonProperty("product_coverage")
    String productCoverage;

    @Column(name = "threat_recency")
    @JsonProperty("threat_recency")
    String threatRecency;

    String fname;

    @Column(name = "cvss_v3_impact_score")
    @JsonProperty("cvssV3_impactScore")
    String cvssV3ImpactScore;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonProperty("plugin_information")
    @JoinColumn(name = "plugin_information_id")
    PluginInformation pluginInformation;

    @Column(name = "required_port")
    @JsonProperty("required_port")
    String requiredPort;

    String dependency;

    String solution;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "vuln_information_id")
    @JsonProperty("vuln_information")
    @JsonDeserialize(using = ObjectLookup.Deserializer.class)
    PluginVulnInformation pluginVulnInformation;

    @Column(name = "age_of_vuln")
    @JsonProperty("age_of_vuln")
    String ageOfVuln;

    @Column(name = "exploit_code_maturity")
    @JsonProperty("exploit_code_maturity")
    String exploitCodeMaturity;



    public String getThreatIntensityLast28() {
        return threatIntensityLast28;
    }

    public void setThreatIntensityLast28(String threatIntensityLast28) {
        this.threatIntensityLast28 = threatIntensityLast28;
    }

    public PluginSynopsis getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(PluginSynopsis synopsis) {
        this.synopsis = synopsis;
    }

    public PluginScriptCopyright getScriptCopyright() {
        return scriptCopyright;
    }

    public void setScriptCopyright(PluginScriptCopyright scriptCopyright) {
        this.scriptCopyright = scriptCopyright;
    }

    public PluginDescription getDescription() {
        return description;
    }

    public void setDescription(PluginDescription description) {
        this.description = description;
    }

    public PluginRiskInformation getRiskInformation() {
        return riskInformation;
    }

    public void setRiskInformation(PluginRiskInformation riskInformation) {
        this.riskInformation = riskInformation;
    }

    public List<PluginRefInformation> getRefInformation() {
        return refInformation;
    }

    public void setRefInformation(List<PluginRefInformation> refInformation) {
        this.refInformation = RefInformation.wrapIfNeeded(this, refInformation);
    }

    public String getThreatSourcesLast28() {
        return threatSourcesLast28;
    }

    public void setThreatSourcesLast28(String threatSourcesLast28) {
        this.threatSourcesLast28 = threatSourcesLast28;
    }

    public PluginName getPluginName() {
        return pluginName;
    }

    public void setPluginName(PluginName pluginName) {
        this.pluginName = pluginName;
    }

    public String getVprScore() {
        return vprScore;
    }

    public void setVprScore(String vprScore) {
        this.vprScore = vprScore;
    }

    public String getCvssScoreSource() {
        return cvssScoreSource;
    }

    public void setCvssScoreSource(String cvssScoreSource) {
        this.cvssScoreSource = cvssScoreSource;
    }

    public List<PluginSeeAlso> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(List<PluginSeeAlso> seeAlso) {
        this.seeAlso = seeAlso;
    }

    public String getProductCoverage() {
        return productCoverage;
    }

    public void setProductCoverage(String productCoverage) {
        this.productCoverage = productCoverage;
    }

    public String getThreatRecency() {
        return threatRecency;
    }

    public void setThreatRecency(String threatRecency) {
        this.threatRecency = threatRecency;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getCvssV3ImpactScore() {
        return cvssV3ImpactScore;
    }

    public void setCvssV3ImpactScore(String cvssV3ImpactScore) {
        this.cvssV3ImpactScore = cvssV3ImpactScore;
    }

    public PluginInformation getPluginInformation() {
        return pluginInformation;
    }

    public void setPluginInformation(PluginInformation pluginInformation) {
        this.pluginInformation = pluginInformation;
    }

    public String getRequiredPort() {
        return requiredPort;
    }

    public void setRequiredPort(String requiredPort) {
        this.requiredPort = requiredPort;
    }

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }

    public PluginVulnInformation getPluginVulnInformation() {
        return pluginVulnInformation;
    }

    public void setPluginVulnInformation(PluginVulnInformation pluginVulnInformation) {
        this.pluginVulnInformation = pluginVulnInformation;
    }

    public String getAgeOfVuln() {
        return ageOfVuln;
    }

    public void setAgeOfVuln(String ageOfVuln) {
        this.ageOfVuln = ageOfVuln;
    }

    public String getExploitCodeMaturity() {
        return exploitCodeMaturity;
    }

    public void setExploitCodeMaturity(String exploitCodeMaturity) {
        this.exploitCodeMaturity = exploitCodeMaturity;
    }

    @Override
    public void _set(PluginAttributes o) {
        this.__set(o);
        this.threatIntensityLast28 = o.threatIntensityLast28;
        this.synopsis = o.synopsis;
        this.scriptCopyright = o.scriptCopyright;
        this.description = o.description;
        this.riskInformation = o.riskInformation;
        this.refInformation = o.refInformation;
        this.threatSourcesLast28 = o.threatSourcesLast28;
        this.pluginName = o.pluginName;
        this.vprScore = o.vprScore;
        this.cvssScoreSource = o.cvssScoreSource;
        this.productCoverage = o.productCoverage;
        this.threatRecency = o.threatRecency;
        this.fname = o.fname;
        this.cvssV3ImpactScore = o.cvssV3ImpactScore;
        this.requiredPort = o.requiredPort;
        this.dependency = o.dependency;
        this.solution = o.solution;
        this.ageOfVuln = o.ageOfVuln;
        this.exploitCodeMaturity = o.exploitCodeMaturity;
    }
}
