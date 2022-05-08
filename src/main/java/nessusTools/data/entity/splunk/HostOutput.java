package nessusTools.data.entity.splunk;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.entity.scan.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.*;
import java.util.*;

@Entity(name = "HostOutput")
@Table(name = "host_output")
@JsonSerialize(using = HostOutputToArray.class)
public class HostOutput implements DbPojo {
    public static final Dao<HostOutput> dao = new Dao<>(HostOutput.class);

    @Id
    @NaturalId
    @JsonIgnore
    private int id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id")
    @JsonIgnore
    private ScanHostResponse scanHostResponse;

    @Column(name = "scan_timestamp")
    private Timestamp scanTimestamp;

    @Column(name = "output_timestamp")
    private Timestamp outputTimestamp;

    private String filename;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "hostOutput")
    @LazyCollection(LazyCollectionOption.FALSE)
    @Fetch(value = FetchMode.SUBSELECT)
    //@OrderColumn(name = "__order_for_host_vulnerability_output", nullable = false)
    private List<HostVulnerabilityOutput> vulnerabilities;


    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public JsonNode toJsonNode() {
        return SplunkOutputMapper.get().valueToTree(this);
    }

    @Override
    public String toJsonString() throws JsonProcessingException {
        return SplunkOutputMapper.get().writeValueAsString(this);
    }

    @Override
    public void _prepare() {
        if (this.vulnerabilities == null) return;
        for (HostVulnerabilityOutput vuln : this.vulnerabilities) {
            vuln.setHostOutput(this);
            vuln.setScanTimestamp(this.scanTimestamp);
        }
        this.vulnerabilities = HostVulnerabilityOutput.dao.getOrCreate(this.vulnerabilities);
    }

    @Transient
    @JsonIgnore
    public void useTimestampFrom(ScanResponse scanResponse) {
        if (scanResponse == null) return;
        ScanInfo info = scanResponse.getInfo();
        if (info != null) {
            this.scanTimestamp = info.getTimestamp();
            if (this.scanTimestamp == null) this.scanTimestamp = info.getScanEnd();
            if (this.scanTimestamp == null) this.scanTimestamp = info.getScannerEnd();
        }
        if (this.scanTimestamp != null) return;
        Scan scan = scanResponse.getScan();
        if (scan != null) {
            this.scanTimestamp = scan.getLastModificationDate();
        }
    }


    @Transient
    @JsonIgnore
    public void setVulnerabilityTimestamps() {
        if (this.vulnerabilities == null) return;
        for (HostVulnerabilityOutput vuln : this.vulnerabilities) {
            vuln.setScanTimestamp(this.scanTimestamp);
        }
    }

    public ScanHostResponse getScanHostResponse() {
        return scanHostResponse;
    }

    public void setScanHostResponse(ScanHostResponse scanHostResponse) {
        this.scanHostResponse = scanHostResponse;
    }

    public Timestamp getScanTimestamp() {
        return scanTimestamp;
    }

    public void setScanTimestamp(Timestamp scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    public Timestamp getOutputTimestamp() {
        return outputTimestamp;
    }

    public void setOutputTimestamp(Timestamp outputTimestamp) {
        this.outputTimestamp = outputTimestamp;
    }

    public List<HostVulnerabilityOutput> getVulnerabilities() {
        return vulnerabilities;
    }

    public void setVulnerabilities(List<HostVulnerabilityOutput> vulnerabilityOutputs) {
        this.vulnerabilities = vulnerabilityOutputs;
        if (vulnerabilityOutputs == null) return;
        for (HostVulnerabilityOutput vuln : vulnerabilityOutputs) {
            if (vuln == null) continue;
            vuln.setHostOutput(this);
        }
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Transient
    @JsonIgnore
    public int size() {
        if (this.vulnerabilities == null) return 0;
        else return this.vulnerabilities.size();
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!Objects.equals(o.getClass(), this.getClass())) return false;

        HostOutput other = (HostOutput) o;
        return  this.getId() == other.getId()
                && Objects.equals(this.scanTimestamp, other.scanTimestamp)
                && RoundTimestamp.equals(this.outputTimestamp, other.outputTimestamp)
                && ((this.vulnerabilities == null || this.vulnerabilities.size() <= 0)
                            ? (other.vulnerabilities == null || other.vulnerabilities.size() <= 0)
                            : (Objects.equals(this.vulnerabilities, ((HostOutput) o).vulnerabilities)
                        ));

    }

    @Override
    public int hashCode() {
        return HostOutput.class.hashCode() ^ this.id;
    }

    public String toString() {
        try {
            return this.toJsonString();
        } catch (JsonProcessingException e) {
            return "toString() could not convert to JSON for '"
                    + super.toString() + "' :\n"
                    + e.getMessage();
        }
    }
}
