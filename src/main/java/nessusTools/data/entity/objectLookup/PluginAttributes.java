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

/**
 * Represents a reusable "object lookup", for a plugin_attributes object returned from the
 * Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "PluginAttributes")
@Table(name = "plugin_attributes")
public class PluginAttributes extends HashLookupTemplate<PluginAttributes> {

    /**
     * The dao for PluginAttributes
     */
    public static final HashLookupDao<PluginAttributes> dao
            = new HashLookupDao<PluginAttributes>(PluginAttributes.class);

    @Column(name = "threat_intensity_last_28")
    @JsonProperty("threat_intensity_last_28")
    private String threatIntensityLast28;

    /**
     * The Synopsis.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "synopsis_id")
    private PluginSynopsis synopsis;

    /**
     * The Script copyright.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "script_copyright_id")
    @JsonProperty("script_copyright")
    private PluginScriptCopyright scriptCopyright;

    /**
     * The Description.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "description_id")
    private PluginDescription description;

    /**
     * The Risk information.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "risk_information_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("risk_information")
    private PluginRiskInformation riskInformation;


    /**
     * The Ref information.
     */
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
    private List<PluginRefInformation> refInformation;

    /**
     * The Ref information.
     */
    @Transient
    private RefInformation ref_information;

    /**
     * The Threat sources last 28.
     */
    @Column(name = "threat_sources_last_28")
    @JsonProperty("threat_sources_last_28")
    private String threatSourcesLast28;

    /**
     * The Plugin name.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "plugin_name_id")
    @JsonProperty("plugin_name")
    private PluginName pluginName;

    /**
     * The Vpr score.
     */
    @Column(name = "vpr_score")
    @JsonProperty("vpr_score")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private String vprScore;

    /**
     * The Cvss score source.
     */
    @Column(name = "cvss_score_source")
    @JsonProperty("cvss_score_source")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cvssScoreSource;

    /**
     * The See also.
     */
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
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<PluginSeeAlso> seeAlso;

    /**
     * The Product coverage.
     */
    @Column(name = "product_coverage")
    @JsonProperty("product_coverage")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private String productCoverage;

    /**
     * The Threat recency.
     */
    @Column(name = "threat_recency")
    @JsonProperty("threat_recency")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private String threatRecency;

    /**
     * The Fname.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private String fname;

    /**
     * The Cvss v 3 impact score.
     */
    @Column(name = "cvss_v3_impact_score")
    @JsonProperty("cvssV3_impactScore")
    @JsonInclude(JsonInclude.Include.NON_EMPTY) // maybe ????
    private String cvssV3ImpactScore;

    /**
     * The Plugin information.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "plugin_information_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("plugin_information")
    private PluginInformation pluginInformation;

    /**
     * The Required port.
     */
    @Column(name = "required_port")
    @JsonProperty("required_port")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String requiredPort;

    /**
     * The Dependency.
     */
    private String dependency;

    /**
     * The Solution.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "solution_id")
    private PluginSolution solution;

    /**
     * The Plugin vuln information.
     */
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "vuln_information_id")
    @Access(AccessType.PROPERTY)
    @JsonProperty("vuln_information")
    private PluginVulnInformation pluginVulnInformation;

    /**
     * The Age of vuln.
     */
    @Column(name = "age_of_vuln")
    @JsonProperty("age_of_vuln")
    private String ageOfVuln;

    /**
     * The Exploit code maturity.
     */
    @Column(name = "exploit_code_maturity")
    @JsonProperty("exploit_code_maturity")
    private String exploitCodeMaturity;

    /**
     * The Cert.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String cert;

    @Column(name = "required_key")
    @JsonProperty("required_key")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String requiredKey;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String iava;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
        this.pluginVulnInformation = PluginVulnInformation.dao.getOrCreate(this.pluginVulnInformation);
        this.riskInformation = PluginRiskInformation.dao.getOrCreate(this.riskInformation);
        this.setRefInformation(PluginRefInformation.dao.getOrCreate(refInformation));
        this.pluginInformation = PluginInformation.dao.getOrCreate(this.pluginInformation);
    }


    /**
     * Gets ref information.
     *
     * @return the ref information
     */
    public List<PluginRefInformation> getRefInformation() {
        return this.refInformation;
    }

    /**
     * Sets ref information.
     *
     * @param refInformation the ref information
     */
    public void setRefInformation(List<PluginRefInformation> refInformation) {
        if (refInformation == this.refInformation) return; //break infinite recursion with child
        this.refInformation = refInformation;
        if (this.ref_information != null) this.ref_information.setRef(this.refInformation);
    }

    /**
     * Gets ref information.
     *
     * @return the ref information
     */
    public RefInformation getRef_information() {
        if (this.ref_information == null) {
            this.ref_information = new RefInformation();
            this.ref_information.takeFieldsFromParent(this);
        }
        return this.ref_information;
    }

    /**
     * Sets ref information.
     *
     * @param ref_information the ref information
     */
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
        this.seeAlso = o.seeAlso;
        this.productCoverage = o.productCoverage;
        this.threatRecency = o.threatRecency;
        this.fname = o.fname;
        this.cvssV3ImpactScore = o.cvssV3ImpactScore;
        this.pluginInformation = o.pluginInformation;
        this.requiredPort = o.requiredPort;
        this.dependency = o.dependency;
        this.solution = o.solution;
        this.pluginVulnInformation = o.pluginVulnInformation;
        this.ageOfVuln = o.ageOfVuln;
        this.exploitCodeMaturity = o.exploitCodeMaturity;
        this.cert = o.cert;
        this.requiredKey = o.requiredKey;
        this.iava = o.iava;

        if (this.ref_information != null) {
            this.ref_information.clearParent();
            this.ref_information = null;
        }
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(PluginAttributes o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.threatIntensityLast28, o.threatIntensityLast28)
                && Objects.equals(this.synopsis, o.synopsis)
                && Objects.equals(this.scriptCopyright, o.scriptCopyright)
                && Objects.equals(this.description, o.description)
                && (this.riskInformation != null
                        ? this.riskInformation._match(o.riskInformation)
                        : o.riskInformation == null)
                && this._matchRefInformation(o.refInformation)
                && Objects.equals(this.threatSourcesLast28, o.threatSourcesLast28)
                && Objects.equals(this.pluginName, o.pluginName)
                && Objects.equals(this.vprScore, o.vprScore)
                && Objects.equals(this.cvssScoreSource, o.cvssScoreSource)
                && Objects.equals(this.seeAlso, o.seeAlso)
                && Objects.equals(this.productCoverage, o.productCoverage)
                && Objects.equals(this.threatRecency, o.threatRecency)
                && Objects.equals(this.fname, o.fname)
                && Objects.equals(this.cvssV3ImpactScore, o.cvssV3ImpactScore)
                && (this.pluginInformation != null
                        ? this.pluginInformation._match(o.pluginInformation)
                        : o.pluginInformation == null)
                && Objects.equals(this.requiredPort, o.requiredPort)
                && Objects.equals(this.dependency, o.dependency)
                && Objects.equals(this.solution, o.solution)
                && (this.pluginVulnInformation != null
                        ? this.pluginVulnInformation._match(o.pluginVulnInformation)
                        : o.pluginVulnInformation == null)
                && Objects.equals(this.ageOfVuln, o.ageOfVuln)
                && Objects.equals(this.exploitCodeMaturity, o.exploitCodeMaturity)
                && Objects.equals(this.cert, o.cert)
                && Objects.equals(this.requiredKey, o.requiredKey)
                && Objects.equals(this.iava, o.iava)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) {
            return false;
        }

        PluginAttributes other = (PluginAttributes) o;
        return this.getId() == other.getId() && this._match(other);
    }

    @Transient
    @JsonIgnore
    private boolean _matchRefInformation(List<PluginRefInformation> o) {
        if (this.refInformation == null || this.refInformation.size() <= 0) {
            return o == null || o.size() <= 0;

        } else if (o == null || o.size() != this.refInformation.size()) {
            return false;
        }

        Iterator<PluginRefInformation> mine = this.refInformation.iterator();
        Iterator<PluginRefInformation> theirs = o.iterator();
        while(mine.hasNext()) {
            if (!theirs.hasNext()) return false;
            PluginRefInformation mn = mine.next();
            PluginRefInformation th = theirs.next();
            if (mn == null) {
                if (th != null) return false;
                continue;

            } else if (th == null) {
                return false;
            }

            if (!mn._match(th)) return false;
        }

        return !theirs.hasNext();
    }


    /**
     * Gets threat intensity last 28.
     *
     * @return the threat intensity last 28
     */
    public String getThreatIntensityLast28() {
        return threatIntensityLast28;
    }

    /**
     * Sets threat intensity last 28.
     *
     * @param threatIntensityLast28 the threat intensity last 28
     */
    public void setThreatIntensityLast28(String threatIntensityLast28) {
        this.threatIntensityLast28 = threatIntensityLast28;
    }

    /**
     * Gets synopsis.
     *
     * @return the synopsis
     */
    public PluginSynopsis getSynopsis() {
        return synopsis;
    }

    /**
     * Sets synopsis.
     *
     * @param synopsis the synopsis
     */
    public void setSynopsis(PluginSynopsis synopsis) {
        this.synopsis = synopsis;
    }

    /**
     * Gets script copyright.
     *
     * @return the script copyright
     */
    public PluginScriptCopyright getScriptCopyright() {
        return scriptCopyright;
    }

    /**
     * Sets script copyright.
     *
     * @param scriptCopyright the script copyright
     */
    public void setScriptCopyright(PluginScriptCopyright scriptCopyright) {
        this.scriptCopyright = scriptCopyright;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public PluginDescription getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(PluginDescription description) {
        this.description = description;
    }

    /**
     * Gets risk information.
     *
     * @return the risk information
     */
    public PluginRiskInformation getRiskInformation() {
        return riskInformation;
    }

    /**
     * Sets risk information.
     *
     * @param riskInformation the risk information
     */
    public void setRiskInformation(PluginRiskInformation riskInformation) {
        this.riskInformation = riskInformation;
    }

    /**
     * Gets threat sources last 28.
     *
     * @return the threat sources last 28
     */
    public String getThreatSourcesLast28() {
        return threatSourcesLast28;
    }

    /**
     * Sets threat sources last 28.
     *
     * @param threatSourcesLast28 the threat sources last 28
     */
    public void setThreatSourcesLast28(String threatSourcesLast28) {
        this.threatSourcesLast28 = threatSourcesLast28;
    }

    /**
     * Gets plugin name.
     *
     * @return the plugin name
     */
    public PluginName getPluginName() {
        return pluginName;
    }

    /**
     * Sets plugin name.
     *
     * @param pluginName the plugin name
     */
    public void setPluginName(PluginName pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Gets vpr score.
     *
     * @return the vpr score
     */
    public String getVprScore() {
        return vprScore;
    }

    /**
     * Sets vpr score.
     *
     * @param vprScore the vpr score
     */
    public void setVprScore(String vprScore) {
        this.vprScore = vprScore;
    }

    /**
     * Gets cvss score source.
     *
     * @return the cvss score source
     */
    public String getCvssScoreSource() {
        return cvssScoreSource;
    }

    /**
     * Sets cvss score source.
     *
     * @param cvssScoreSource the cvss score source
     */
    public void setCvssScoreSource(String cvssScoreSource) {
        this.cvssScoreSource = cvssScoreSource;
    }

    /**
     * Gets see also.
     *
     * @return the see also
     */
    public List<PluginSeeAlso> getSeeAlso() {
        return seeAlso;
    }

    /**
     * Sets see also.
     *
     * @param seeAlso the see also
     */
    public void setSeeAlso(List<PluginSeeAlso> seeAlso) {
        this.seeAlso = seeAlso;
    }

    /**
     * Gets product coverage.
     *
     * @return the product coverage
     */
    public String getProductCoverage() {
        return productCoverage;
    }

    /**
     * Sets product coverage.
     *
     * @param productCoverage the product coverage
     */
    public void setProductCoverage(String productCoverage) {
        this.productCoverage = productCoverage;
    }

    /**
     * Gets threat recency.
     *
     * @return the threat recency
     */
    public String getThreatRecency() {
        return threatRecency;
    }

    /**
     * Sets threat recency.
     *
     * @param threatRecency the threat recency
     */
    public void setThreatRecency(String threatRecency) {
        this.threatRecency = threatRecency;
    }

    /**
     * Gets fname.
     *
     * @return the fname
     */
    public String getFname() {
        return fname;
    }

    /**
     * Sets fname.
     *
     * @param fname the fname
     */
    public void setFname(String fname) {
        this.fname = fname;
    }

    /**
     * Gets cvss v 3 impact score.
     *
     * @return the cvss v 3 impact score
     */
    public String getCvssV3ImpactScore() {
        return cvssV3ImpactScore;
    }

    /**
     * Sets cvss v 3 impact score.
     *
     * @param cvssV3ImpactScore the cvss v 3 impact score
     */
    public void setCvssV3ImpactScore(String cvssV3ImpactScore) {
        this.cvssV3ImpactScore = cvssV3ImpactScore;
    }

    /**
     * Gets plugin information.
     *
     * @return the plugin information
     */
    public PluginInformation getPluginInformation() {
        return pluginInformation;
    }

    /**
     * Sets plugin information.
     *
     * @param pluginInformation the plugin information
     */
    public void setPluginInformation(PluginInformation pluginInformation) {
        this.pluginInformation = pluginInformation;
    }

    /**
     * Gets required port.
     *
     * @return the required port
     */
    public String getRequiredPort() {
        return requiredPort;
    }

    /**
     * Sets required port.
     *
     * @param requiredPort the required port
     */
    public void setRequiredPort(String requiredPort) {
        this.requiredPort = requiredPort;
    }

    /**
     * Gets dependency.
     *
     * @return the dependency
     */
    public String getDependency() {
        return dependency;
    }

    /**
     * Sets dependency.
     *
     * @param dependency the dependency
     */
    public void setDependency(String dependency) {
        this.dependency = dependency;
    }

    /**
     * Gets solution.
     *
     * @return the solution
     */
    public PluginSolution getSolution() {
        return solution;
    }

    /**
     * Sets solution.
     *
     * @param solution the solution
     */
    public void setSolution(PluginSolution solution) {
        this.solution = solution;
    }

    /**
     * Gets plugin vuln information.
     *
     * @return the plugin vuln information
     */
    public PluginVulnInformation getPluginVulnInformation() {
        return pluginVulnInformation;
    }

    /**
     * Sets plugin vuln information.
     *
     * @param pluginVulnInformation the plugin vuln information
     */
    public void setPluginVulnInformation(PluginVulnInformation pluginVulnInformation) {
        this.pluginVulnInformation = pluginVulnInformation;
    }

    /**
     * Gets age of vuln.
     *
     * @return the age of vuln
     */
    public String getAgeOfVuln() {
        return ageOfVuln;
    }

    /**
     * Sets age of vuln.
     *
     * @param ageOfVuln the age of vuln
     */
    public void setAgeOfVuln(String ageOfVuln) {
        this.ageOfVuln = ageOfVuln;
    }

    /**
     * Gets exploit code maturity.
     *
     * @return the exploit code maturity
     */
    public String getExploitCodeMaturity() {
        return exploitCodeMaturity;
    }

    /**
     * Sets exploit code maturity.
     *
     * @param exploitCodeMaturity the exploit code maturity
     */
    public void setExploitCodeMaturity(String exploitCodeMaturity) {
        this.exploitCodeMaturity = exploitCodeMaturity;
    }

    /**
     * Gets cert.
     *
     * @return the cert
     */
    public String getCert() {
        return cert;
    }

    /**
     * Sets cert.
     *
     * @param cert the cert
     */
    public void setCert(String cert) {
        this.cert = cert;
    }

    /**
     * Gets required key.
     *
     * @return the required key
     */
    public String getRequiredKey() {
        return requiredKey;
    }

    /**
     * Sets required key.
     *
     * @param requiredKey the required key
     */
    public void setRequiredKey(String requiredKey) {
        this.requiredKey = requiredKey;
    }

    /**
     * Gets iava.
     *
     * @return the iava
     */
    public String getIava() {
        return iava;
    }

    /**
     * Sets iava.
     *
     * @param iava the iava
     */
    public void setIava(String iava) {
        this.iava = iava;
    }
}
