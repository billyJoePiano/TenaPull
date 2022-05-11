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

/**
 * Represents a reusable "object lookup", for an object in the list of remediations,
 * returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "RemediationDetails")
@Table(name = "remediation_details")
public class RemediationDetails extends GeneratedIdPojo implements MapLookupPojo<RemediationDetails> {

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
