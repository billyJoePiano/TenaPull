package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
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
    @Access(AccessType.PROPERTY)
    @JsonProperty("risk_information")
    PluginRiskInformation riskInformation;


    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    @Access(AccessType.PROPERTY)
    @JoinTable(
            name = "plugin_attributes_ref_information",
            joinColumns = { @JoinColumn(name = "attributes_id") },
            inverseJoinColumns = { @JoinColumn(name = "ref_id") }
    )
    @OrderColumn(name = "__order_for_plugin_attributes_ref_information", nullable = false)
    @JsonIgnore
    List<PluginRefInformation> refInformation;

    @Transient
    RefInformation ref_information;

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
            joinColumns = { @JoinColumn(name = "attributes_id") },
            inverseJoinColumns = { @JoinColumn(name = "see_also_id") }
    )
    @OrderColumn(name = "__order_for_plugin_attributes_see_also", nullable = false)
    @JsonProperty("see_also")
    @JsonSerialize(using = Lists.EmptyToNullSerializer.class)
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
    @JoinColumn(name = "plugin_information_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("plugin_information")
    PluginInformation pluginInformation;

    @Column(name = "required_port")
    @JsonProperty("required_port")
    String requiredPort;

    String dependency;

    String solution;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "vuln_information_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("vuln_information")
    PluginVulnInformation pluginVulnInformation;

    @Column(name = "age_of_vuln")
    @JsonProperty("age_of_vuln")
    String ageOfVuln;

    @Column(name = "exploit_code_maturity")
    @JsonProperty("exploit_code_maturity")
    String exploitCodeMaturity;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.riskInformation = PluginRiskInformation.dao.getOrCreate(this.riskInformation);
        this.setRefInformation(PluginRefInformation.dao.getOrCreate(refInformation));
        this.pluginInformation = PluginInformation.dao.getOrCreate(this.pluginInformation);
    }


    public List<PluginRefInformation> getRefInformation() {
        return this.refInformation;
    }

    public void setRefInformation(List<PluginRefInformation> refInformation) {
        if (refInformation == this.refInformation) return; //break infinite recursion with child
        this.refInformation = refInformation;
        if (this.ref_information != null) this.ref_information.setRef(this.refInformation);
    }

    public RefInformation getRef_information() {
        if (this.ref_information == null) {
            this.ref_information = new RefInformation();
            this.ref_information.takeFieldsFromParent(this);
        }
        return this.ref_information;
    }

    public void setRef_information(RefInformation ref_information) {
        if (this.ref_information != null && this.ref_information != ref_information) {
            this.ref_information.clearParent();
        }
        this.ref_information = ref_information;
        if (ref_information != null) ref_information.putFieldsIntoParent(this);
    }

    @JsonAnyGetter
    @Transient
    @Override
    public Map<String, JsonNode> getExtraJsonMap() {
        return this.getRef_information().jsonAnyGetterForParent();
    }

    @JsonAnySetter
    @Transient
    @Override
    public void putExtraJson(String key, Object value) {
        super.putExtraJson(key, value);
        if (this.ref_information != null) this.ref_information.checkExtraJsonPut(key, value);
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

        if (this.ref_information != null) {
            this.ref_information.clearParent();
            this.ref_information = null;
        }
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginAttributes o) {
        return this.equals(o);
    }



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
        this.riskInformation = PluginRiskInformation.dao.getOrCreate(riskInformation);
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
        this.pluginInformation = PluginInformation.dao.getOrCreate(pluginInformation);
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
        this.pluginVulnInformation = PluginVulnInformation.dao.getOrCreate(pluginVulnInformation);
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
}
