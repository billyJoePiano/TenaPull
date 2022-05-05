package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanRemediationsSummary")
@Table(name = "scan_remediations_summary")
public class ScanRemediationsSummary extends ScanResponse.SingleChild<ScanRemediationsSummary> {
    public static final Dao<ScanRemediationsSummary> dao = new Dao(ScanRemediationsSummary.class);

    @Transient // shared with / obtained from ScanResponse parent
    @JsonSerialize(using = Lists.EmptyToNullSerializer.class)
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

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void setResponse(ScanResponse response) {
        ScanResponse old = this.getResponse();
        super.setResponse(response);
        if (response != old && old != null) {
            old.setRemediations(null); // ScanResponse handles list detachment
        }
    }

    public List<ScanRemediation> getRemediations() {
        return remediations;
    }

    public void setRemediations(List<ScanRemediation> remediations) {
        if (remediations == this.remediations) return;
        this.remediations = remediations;

        ScanResponse response = this.getResponse();
        if (response == null || response.getRemediations() != this) return;

        response.setRemediationsList(remediations);
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
