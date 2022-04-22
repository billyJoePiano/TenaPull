package nessusTools.data.entity.objectLookup;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

@Entity(name = "Remediation")
@Table(name = "remediation")
public class Remediation extends GeneratedIdPojo
        implements ObjectLookupPojo<Remediation> {

    public static final ObjectLookupDao<Remediation> dao = new ObjectLookupDao(Remediation.class);

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

    @Override
    public void _set(Remediation o) {
        this.setId(o.getId());
        this.remediation = o.remediation;
        this.hosts = o.hosts;
        this.value = o.value;
        this.vulns = o.vulns;
        this.setExtraJson(o.getExtraJson());
    }
}
