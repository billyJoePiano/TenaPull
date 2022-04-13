package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;



// for ScanHost
@Entity(name = "SeverityCount")
@Table(name = "scan_host_severity_count")
@JsonIgnoreProperties({"id"})
public class SeverityCount extends GeneratedIdPojo {
    public static final Dao<SeverityCount> dao = new Dao(SeverityCount.class);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name="scan_host_id")
    @JsonIgnore
    ScanHost scanHost;

    @Column(name = "severity_level")
    private int severityLevel;

    @Column
    private int count;

    public ScanHost getScanInfoHost() {
        return scanHost;
    }

    public void setScanInfoHost(ScanHost scanHost) {
        this.scanHost = scanHost;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
