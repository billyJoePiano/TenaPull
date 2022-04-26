package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanRemediationsSummary")
@Table(name = "scan_remediations_summary")
public class ScanRemediationsSummary extends ScanResponse.SingleChild<ScanRemediationsSummary> {
    public static final Dao<ScanRemediationsSummary> dao = new Dao(ScanRemediationsSummary.class);

    @ManyToMany(cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "scan_remediation",
            joinColumns = { @JoinColumn(name = "scan_id") },
            inverseJoinColumns = { @JoinColumn(name = "remediation_id") }
    )
    @OrderColumn(name = "__order_for_scan_remediation", nullable = false)
    @JsonDeserialize(contentUsing = ObjectLookup.Deserializer.class)
    private List<Remediation> remediations;

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

    public List<Remediation> getRemediations() {
        return remediations;
    }

    public void setRemediations(List<Remediation> remediations) {
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
