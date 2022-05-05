package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.hibernate.annotations.*;

import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.*;
import java.util.*;

@Entity(name = "ScanRemediation")
@Table(name = "scan_remediation")
public class ScanRemediation implements MapLookupPojo<ScanRemediation>,
        ScanResponse.ScanResponseChild<ScanRemediation> {

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
    RemediationDetails details;

    Integer hosts;

    Integer vulns;

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

    public RemediationDetails getDetails() {
        return details;
    }

    public void setDetails(RemediationDetails details) {
        this.details = details;
    }

    public Integer getHosts() {
        return hosts;
    }

    public void setHosts(Integer hosts) {
        this.hosts = hosts;
    }

    public Integer getVulns() {
        return vulns;
    }

    public void setVulns(Integer vulns) {
        this.vulns = vulns;
    }
}