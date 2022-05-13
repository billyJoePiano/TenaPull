package tenapull.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import tenapull.data.entity.objectLookup.*;
import tenapull.data.entity.response.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.util.*;
import org.hibernate.annotations.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

/**
 * Represents an object from the remediations array returned by the Nessus API in /scans/&lt;scan-id&gt;
 */
@Entity(name = "ScanRemediation")
@Table(name = "scan_remediation")
public class ScanRemediation implements MapLookupPojo<ScanRemediation>,
        ScanResponse.ScanResponseChild<ScanRemediation> {

    /**
     * The dao for ScanRemediation
     */
    public static final MapLookupDao<ScanRemediation>
            dao = new MapLookupDao<ScanRemediation>(ScanRemediation.class);

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO, generator="native")
    @GenericGenerator(name = "native",strategy = "native")
    @JsonIgnore
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scan_id")
    @JsonIgnore
    private ScanResponse response;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "remediation_id")
    @JsonUnwrapped
    private RemediationDetails details;

    private Integer hosts;

    private Integer vulns;

    @Transient
    @JsonIgnore
    @Override
    public ObjectNode toJsonNode() {
        //use the cached node from plugin
        if (this.details == null) return new ObjectMapper().valueToTree (this);
        ObjectNode node = this.details.toJsonNode();
        if (this.hosts != null) {
            node.put("hosts", this.hosts);
        }
        if (this.vulns != null) {
            node.put("vulns", this.vulns);
        }

        return node;
    }

    @Transient
    @JsonIgnore
    @Override
    public String toJsonString() {
        return this.toJsonNode().toString();
    }

    @Transient
    @JsonIgnore
    @Override
    public String toString() {
        return this.toJsonString();
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.details = RemediationDetails.dao.getOrCreate(this.details);
    }

    @Override
    public boolean _match(ScanRemediation o) {
        if (o == null) return false;
        if (o == this) return true;
        ScanResponse myRes = this.getResponse();
        ScanResponse theirRes = o.getResponse();
        if (myRes == null || theirRes == null) {
            return false;
        }

        if (myRes != theirRes) {
            int myId = myRes.getId();
            int theirId = theirRes.getId();

            if (myId == 0 || myId != theirId) {
                return false;
            }
        }

        RemediationDetails myDetails = this.details;
        RemediationDetails theirDetails = o.details;
        if (myDetails == null) {
            return theirDetails == null;

        } else if (theirDetails == null) {
            return false;
        }

        if (myDetails == theirDetails) return true;

        int myDetailsId = myDetails.getId();
        int theirDetailsId = theirDetails.getId();

        return myDetailsId != 0 && myDetailsId == theirDetailsId;
    }

    @Override
    @Transient
    @JsonIgnore
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[]
                { "response", this.getResponse(), "details", this.details });
    }



    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(this.getClass(), o.getClass())) return false;

        ScanRemediation other = (ScanRemediation)o;
        ScanResponse theirs = other.getResponse();
        ScanResponse mine = this.getResponse();
        if (mine == null) {
            if (theirs != null) return false;

        } else if (theirs == null) {
            return false;

        } else if (mine.getId() != 0  && theirs.getId() != 0
                && mine.getId() != theirs.getId()) {
            return false;
        }

        return  (this.getId() == 0 || other.getId() == 0 || this.getId() == other.getId())
                && Objects.equals(other.vulns, this.vulns)
                && Objects.equals(other.details, this.details);
    }

    @Transient
    @JsonIgnore
    @Override
    public void _set(ScanRemediation o) {
        this.response = o.response;
        this.details = o.details;
        this.hosts = o.hosts;
        this.vulns = o.vulns;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public ScanResponse getResponse() {
        return this.response;
    }

    @Override
    public void setResponse(ScanResponse response) {
        this.response = response;
    }

    /**
     * Gets details.
     *
     * @return the details
     */
    public RemediationDetails getDetails() {
        return details;
    }

    /**
     * Sets details.
     *
     * @param details the details
     */
    public void setDetails(RemediationDetails details) {
        this.details = details;
    }

    /**
     * Gets hosts.
     *
     * @return the hosts
     */
    public Integer getHosts() {
        return hosts;
    }

    /**
     * Sets hosts.
     *
     * @param hosts the hosts
     */
    public void setHosts(Integer hosts) {
        this.hosts = hosts;
    }

    /**
     * Gets vulns.
     *
     * @return the vulns
     */
    public Integer getVulns() {
        return vulns;
    }

    /**
     * Sets vulns.
     *
     * @param vulns the vulns
     */
    public void setVulns(Integer vulns) {
        this.vulns = vulns;
    }

    /**
     * Because this entity cannot have any extraJson (it will all be contained in the RemediationDetails
     * object lookup entity) it must implement hashCode and equals on its own, so its works properly in
     * a HashMap or HashSet.  The strategy used here is to bitwise XOR the id with the hashcode of ScanRemediation.class
     *
     * @return a hashcode to uniquely identify each ScanRemediation record
     */
    @Override
    public int hashCode() {
        return ScanRemediation.class.hashCode() ^ this.id;
    }
}