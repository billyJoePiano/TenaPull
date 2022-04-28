package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import org.jetbrains.annotations.*;

import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;


// for ScanHost
@Entity(name = "SeverityLevelCount")
@Table(name = "severity_level_count")
@JsonIgnoreProperties({"id"})
public class SeverityLevelCount extends GeneratedIdPojo
        implements ObjectLookupPojo<SeverityLevelCount>,
                    Comparable<SeverityLevelCount> {

    public static final ObjectLookupDao<SeverityLevelCount> dao = new ObjectLookupDao<>(SeverityLevelCount.class);

    @Column(name = "severity_level")
    @JsonProperty("severitylevel")
    private Integer severityLevel;

    private Integer count;

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
    @Override
    public void _prepare() { }

    @Transient
    @JsonIgnore
    @Override
    public void _set(SeverityLevelCount o) {
        this.__set(o);
        this.severityLevel = o.severityLevel;
        this.count = o.count;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(SeverityLevelCount o) {
        return Objects.equals(this.severityLevel, o.severityLevel)
                && Objects.equals(this.count, o.count)
                && Objects.equals(this.getExtraJsonMap(), o.getExtraJsonMap());
    }

    @Transient
    @JsonIgnore
    @Override
    public int compareTo(SeverityLevelCount o) {
        if (o == null) return -1;
        if (this.severityLevel != o.severityLevel) {
            return this.severityLevel < o.severityLevel ? -1 : 1;

        } else if (this.count != o.count) {
            return this.count < o.count ? -1 : 1;
        }

        ExtraJson m = this.getExtraJson();
        ExtraJson t = o.getExtraJson();

        if (m == null && t == null) return 0;
        else if (m == null) return -1;
        else if (t == null) return 1;

        Map<String, JsonNode> mine = m.getMap();
        Map<String, JsonNode> theirs = m.getMap();

        return mine.hashCode() - theirs.hashCode();
    }
}
