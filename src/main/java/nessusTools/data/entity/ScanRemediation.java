package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "ScanRemediation")
@Table(name = "scan_remediation")
public class ScanRemediation extends ScanResponse.ChildListTemplate {
    public static final Dao<ScanRemediation> dao = new Dao(ScanRemediation.class);

    private String remediation;
    private Integer hosts;
    private String value;
    private Integer vulns;

    public String getRemediation() {
        return remediation;
    }

    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }

    public Integer getHosts() {
        return hosts;
    }

    public void setHosts(Integer hosts) {
        this.hosts = hosts;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getVulns() {
        return vulns;
    }

    public void setVulns(Integer vulns) {
        this.vulns = vulns;
    }
}
