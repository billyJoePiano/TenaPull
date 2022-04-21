package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
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

    String synposis;

    @Column(name = "script_copyright")
    @JsonProperty("script_copyright")
    String scriptCopyright;

    String description;

    @Column(name = "risk_information_id")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonProperty("risk_information")
    PluginRiskInformation riskInformation;


    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "plugin_attributes_ref_information",
            joinColumns = { @JoinColumn(name = "attributes_id") },
            inverseJoinColumns = { @JoinColumn(name = "ref_id") }
    )
    @OrderColumn(name = "__order_for_plugin_attributes_ref_information", nullable = false)
    @JsonProperty("ref_information") // TODO flatten
    List<PluginRefInformation> refInformation;

    @Column(name = "threat_sources_last_28")
    @JsonProperty("threat_sources_last_28")
    String threatSourcesLast28;

    @Column(name = "plugin_name_id")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonProperty("plugin_name")
    PluginName pluginName;

    @Column(name = "vpr_score")
    @JsonProperty("vpr_score")
    String vprScore;

    @Column(name = "cvss_score_source")
    @JsonProperty("cvss_score_source")
    String cvssScoreSource;

    @Column(name = "see_also_id")
    @ManyToMany(cascade = CascadeType.ALL)
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

    @Column(name = "plugin_information_id")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JsonProperty("plugin_information")
    PluginInformation pluginInformation;

    @Column(name = "required_port")
    @JsonProperty("required_port")
    String requiredPort;

    String dependency;

    String solution;

    @Column(name = "vuln_information_id")
    @JsonProperty("vuln_information")
    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    PluginVulnInformation pluginVulnInformation;

    @Column(name = "age_of_vuln")
    @JsonProperty("age_of_vuln")
    String ageOfVuln;

    @Column(name = "exploit_code_maturity")
    @JsonProperty("exploit_code_maturity")
    String exploitCodeMaturity;

    @Override
    public void _set(PluginAttributes o) {
        this.setId(o.getId());
        this.threatIntensityLast28 = o.threatIntensityLast28;
        this.synposis = o.synposis;
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
