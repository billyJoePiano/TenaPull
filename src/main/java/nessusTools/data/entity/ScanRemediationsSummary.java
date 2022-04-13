package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "ScanRemediationsSummary")
@Table(name = "scan_remediations_summary")
public class ScanRemediationsSummary extends ScanResponse.ChildTemplate {
    public static final Dao<ScanRemediationsSummary> dao = new Dao(ScanRemediationsSummary.class);

    @OneToMany(mappedBy="scanResponse") //, cascade = { CascadeType.ALL }, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderColumn(name = "__order_for_scan_info_response")
    private List<ScanRemediation> remediations;

    @Column(name = "num_hosts")
    @JsonProperty("num_hosts")
    private Integer numHosts;

    @Column(name = "num_cves")
    @JsonProperty("num_cves")
    private Integer numCves;

    @Column(name = "num_impacted_hosts")
    @JsonProperty("num_impacted_hosts")
    private Integer numImpactedHosts;

    @Column(name = "num_remediated_cves")
    @JsonProperty("num_remediated_cves")
    private Integer numRemediatedCves;

    public List<ScanRemediation> getRemediations() {
        return remediations;
    }

    public void setRemediations(List<ScanRemediation> remediations) {
        this.remediations = remediations;
    }

    public Integer getNumHosts() {
        return numHosts;
    }

    public void setNumHosts(Integer numHosts) {
        this.numHosts = numHosts;
    }

    public Integer getNumCves() {
        return numCves;
    }

    public void setNumCves(Integer numCves) {
        this.numCves = numCves;
    }

    public Integer getNumImpactedHosts() {
        return numImpactedHosts;
    }

    public void setNumImpactedHosts(Integer numImpactedHosts) {
        this.numImpactedHosts = numImpactedHosts;
    }

    public Integer getNumRemediatedCves() {
        return numRemediatedCves;
    }

    public void setNumRemediatedCves(Integer numRemediatedCves) {
        this.numRemediatedCves = numRemediatedCves;
    }
}
