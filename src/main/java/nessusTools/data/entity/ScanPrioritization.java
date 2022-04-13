package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.client.response.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "ScanPrioritization")
@Table(name = "scan_prioritization")
@JsonIgnoreProperties({"id"})
public class ScanPrioritization extends ScanResponse.ChildTemplate {
    public static final Dao<ScanPrioritization> dao = new Dao(ScanPrioritization.class);

    @OneToMany(mappedBy = "scanResponse", targetEntity = ScanPlugin.class)
    private List<ScanPlugin> plugins;

    @Column(name = "threat_level")
    @JsonProperty("threat_level")
    private Integer threatLevel;

    public List<ScanPlugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<ScanPlugin> plugins) {
        this.plugins = plugins;
    }

    public Integer getThreatLevel() {
        return threatLevel;
    }

    public void setThreatLevel(Integer threatLevel) {
        this.threatLevel = threatLevel;
    }
}
