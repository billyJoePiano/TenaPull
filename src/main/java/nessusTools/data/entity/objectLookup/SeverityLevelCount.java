package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.Table;



// for ScanHost
@Entity(name = "SeverityLevelCount")
@Table(name = "severity_level_count")
@JsonIgnoreProperties({"id"})
public class SeverityLevelCount extends GeneratedIdPojo
        implements ObjectLookupPojo<SeverityLevelCount> {

    public static final ObjectLookupDao<SeverityLevelCount> dao = new ObjectLookupDao<>(SeverityLevelCount.class);

    @Column(name = "severity_level")
    @JsonProperty("severitylevel")
    private int severityLevel;

    private int count;

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

    @Transient
    @JsonIgnore
    public void _set(SeverityLevelCount o) {
        this.setId(o.getId());
        this.severityLevel = o.severityLevel;
        this.count = o.count;
        this.setExtraJson(o.getExtraJson());
    }
}
