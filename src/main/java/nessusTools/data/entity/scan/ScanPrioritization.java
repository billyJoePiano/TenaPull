package nessusTools.data.entity.scan;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.entity.objectLookup.*;
import nessusTools.data.entity.response.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

@Entity(name = "ScanPrioritization")
@Table(name = "scan_prioritization")
@JsonIgnoreProperties({"id"})
public class ScanPrioritization extends ScanResponse.SingleChild<ScanPrioritization> {
    public static final Dao<ScanPrioritization> dao = new Dao(ScanPrioritization.class);

    @OneToMany(mappedBy = "scanPrioritization")
    @OrderColumn(name = "__order_for_scan_plugin")
    @JsonDeserialize(contentAs = ScanPlugin.class)
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
