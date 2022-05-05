package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import nessusTools.data.deserialize.*;
import nessusTools.data.entity.lookup.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;
import nessusTools.util.*;

import javax.persistence.*;
import java.util.*;

@Entity(name = "RemediationDetails")
@Table(name = "remediation_details")
public class RemediationDetails extends GeneratedIdPojo
        implements MapLookupPojo<RemediationDetails>, IdCachingSerializer.NodeCacher<RemediationDetails> {

    public static final MapLookupDao<RemediationDetails> dao = new MapLookupDao<>(RemediationDetails.class);

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "remediation_id")
    private Remediation remediation;
    private String value;

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(RemediationDetails o) {
        this.__set(o);
        this.remediation = o.remediation;
        this.value = o.value;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(RemediationDetails o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.remediation, o.remediation)
                && Objects.equals(this.value, o.value)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
                
    }

    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "remediation", this.remediation,
                "value", this.value,
                "extraJson", this.getExtraJson()
            });
    }

    @Transient
    @JsonIgnore
    private IdCachingSerializer.MainCachedNode<RemediationDetails> cachedNode;

    public IdCachingSerializer.MainCachedNode<RemediationDetails> getCachedNode() {
        return this.cachedNode;
    }

    public void setCachedNode(IdCachingSerializer.MainCachedNode<RemediationDetails> cachedNode) {
        if (cachedNode != null) {
            assert cachedNode.getId() == this.getId() && cachedNode.represents(this);
        }
        this.cachedNode = cachedNode;
    }

    public static JsonSerializer<RemediationDetails>
            getCachingSerializer(JsonSerializer<RemediationDetails> defaultSerializer, ObjectMapper mapper) {

        return IdCachingSerializer.getIdCachingSerializer(defaultSerializer, mapper);
    }

    public static JsonSerializer<RemediationDetails>
            getCacheResetSerializer(JsonSerializer<RemediationDetails> defaultSerializer, ObjectMapper mapper) {

        return IdCachingSerializer.getCacheResetSerializer(defaultSerializer, mapper);
    }

    @Override
    public ObjectNode toJsonNode() {
        if (this.cachedNode == null){
            if (this.getId() == 0) {
                return super.toJsonNode();
            }
            this.cachedNode = IdCachingSerializer.getOrCreateNodeCache(this);
        }
        return this.cachedNode.getNode();
    }

    @Override
    public String toJsonString() throws JsonProcessingException {
        if (this.cachedNode == null){
            if (this.getId() == 0) {
                return super.toJsonString();
            }
            this.cachedNode = IdCachingSerializer.getOrCreateNodeCache(this);
        }
        return this.cachedNode.getString();
    }

    public Remediation getRemediation() {
        return remediation;
    }

    public void setRemediation(Remediation remediation) {
        this.remediation = remediation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
