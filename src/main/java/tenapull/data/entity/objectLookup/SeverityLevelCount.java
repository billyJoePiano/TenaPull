package tenapull.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import tenapull.data.entity.template.*;
import tenapull.data.persistence.*;
import tenapull.util.*;

import javax.persistence.*;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;


/**
 * Represents a reusable "object lookup", for the list of objects in the "severitycount" arrays
 * within the host objects returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "SeverityLevelCount")
@Table(name = "severity_level_count")
@JsonIgnoreProperties({"id"})
public class SeverityLevelCount extends GeneratedIdPojo
        implements MapLookupPojo<SeverityLevelCount>,
                    Comparable<SeverityLevelCount> {

    /**
     * The dao for SeverityLevelCount
     */
    public static final MapLookupDao<SeverityLevelCount> dao = new MapLookupDao<>(SeverityLevelCount.class);

    @Column(name = "severity_level")
    @JsonProperty("severitylevel")
    private Integer severityLevel;

    private Integer count;

    /**
     * Gets severity level.
     *
     * @return the severity level
     */
    public int getSeverityLevel() {
        return severityLevel;
    }

    /**
     * Sets severity level.
     *
     * @param severityLevel the severity level
     */
    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    /**
     * Gets count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets count.
     *
     * @param count the count
     */
    public void setCount(int count) {
        this.count = count;
    }

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

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
        if (o == this) return true;
        return o != null
                && Objects.equals(this.severityLevel, o.severityLevel)
                && Objects.equals(this.count, o.count)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "severityLevel", this.severityLevel,
                "count", this.count,
                "extraJson", this.getExtraJson()
        });
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

        Map<String, JsonNode> mine = m.getValue().getView();
        Map<String, JsonNode> theirs = m.getValue().getView();

        return mine.hashCode() - theirs.hashCode();
    }
}
