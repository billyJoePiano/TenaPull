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

/**
 * Represents the scan-specific fields for the remediations object returned by the
 * Nessus API in /scans/&lt;scan-id&gt;.  The array of remediations belongs to the ScanResponse
 * parent in the DB/ORM, but are deserialized/serialized into this entity and shared with the parent
 */
@Entity(name = "ScanRemediationsSummary")
@Table(name = "scan_remediations_summary")
public class ScanRemediationsSummary extends ScanResponse.SingleChild<ScanRemediationsSummary> {
    /**
     * The dao for ScanRemediationsSummary
     */
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

    /**
     * Sets the ScanResponse parent, and sets the parent's remediations list if
     * it is a new parent.
     *
     * @param response
     */
    @Override
    public void setResponse(ScanResponse response) {
        ScanResponse old = this.getResponse();
        super.setResponse(response);
        if (response != old && old != null) {
            old.setRemediations(null); // ScanResponse handles list detachment
        }
    }

    /**
     * Gets the list of remediations.
     *
     * @return the remediations
     */
    public List<ScanRemediation> getRemediations() {
        return remediations;
    }

    /**
     * Sets the list of remediations in both this entity and the parent (if a parent
     * is set).  Includes a check to prevent infinite recursion between this and the parent
     * setting each other's remediations list.
     *
     * @param remediations the remediations list
     */
    public void setRemediations(List<ScanRemediation> remediations) {
        if (remediations == this.remediations) return;
        this.remediations = remediations;

        ScanResponse response = this.getResponse();
        if (response == null || response.getRemediations() != this) return;

        response.setRemediationsList(remediations);
    }

    /**
     * Gets num hosts.
     *
     * @return the num hosts
     */
    public Integer getNumHosts() {
        return numHosts;
    }

    /**
     * Sets num hosts.
     *
     * @param numHosts the num hosts
     */
    public void setNumHosts(Integer numHosts) {
        this.numHosts = numHosts;
    }

    /**
     * Gets num cves.
     *
     * @return the num cves
     */
    public Integer getNumCves() {
        return numCves;
    }

    /**
     * Sets num cves.
     *
     * @param numCves the num cves
     */
    public void setNumCves(Integer numCves) {
        this.numCves = numCves;
    }

    /**
     * Gets num impacted hosts.
     *
     * @return the num impacted hosts
     */
    public Integer getNumImpactedHosts() {
        return numImpactedHosts;
    }

    /**
     * Sets num impacted hosts.
     *
     * @param numImpactedHosts the num impacted hosts
     */
    public void setNumImpactedHosts(Integer numImpactedHosts) {
        this.numImpactedHosts = numImpactedHosts;
    }

    /**
     * Gets num remediated cves.
     *
     * @return the num remediated cves
     */
    public Integer getNumRemediatedCves() {
        return numRemediatedCves;
    }

    /**
     * Sets num remediated cves.
     *
     * @param numRemediatedCves the num remediated cves
     */
    public void setNumRemediatedCves(Integer numRemediatedCves) {
        this.numRemediatedCves = numRemediatedCves;
    }
}
