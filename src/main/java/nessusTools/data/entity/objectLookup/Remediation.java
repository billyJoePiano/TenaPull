package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "Remediation")
@Table(name = "remediation")
public class Remediation extends GeneratedIdPojo
        implements MapLookupPojo<Remediation>, IdCachingSerializer.NodeCacher<Remediation> {

    public static final MapLookupDao<Remediation> dao = new MapLookupDao<>(Remediation.class);

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

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(Remediation o) {
        this.__set(o);
        this.remediation = o.remediation;
        this.hosts = o.hosts;
        this.value = o.value;
        this.vulns = o.vulns;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(Remediation o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.remediation, o.remediation)
                && Objects.equals(this.hosts, o.hosts)
                && Objects.equals(this.value, o.value)
                && Objects.equals(this.vulns, o.vulns)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
                
    }

    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "remediation", this.remediation,
                "hosts", this.hosts,
                "value", this.value,
                "vulns", this.vulns,
                "extraJson", this.getExtraJson()
        });
    }

    @Transient
    @JsonIgnore
    private IdCachingSerializer.MainCachedNode<Remediation> cachedNode;

    public IdCachingSerializer.MainCachedNode<Remediation> getCachedNode() {
        return this.cachedNode;
    }

    public void setCachedNode(IdCachingSerializer.MainCachedNode<Remediation> cachedNode) {
        if (cachedNode != null) {
            assert cachedNode.getId() == this.getId() && cachedNode.represents(this);
        }
        this.cachedNode = cachedNode;
    }

    public static JsonSerializer<Remediation>
            getCachingSerializer(JsonSerializer<Remediation> defaultSerializer, ObjectMapper mapper) {

        return IdCachingSerializer.getIdCachingSerializer(defaultSerializer, mapper);
    }

    public static JsonSerializer<Remediation>
            getCacheResetSerializer(JsonSerializer<Remediation> defaultSerializer, ObjectMapper mapper) {

        return IdCachingSerializer.getCacheResetSerializer(defaultSerializer, mapper);
    }
}
