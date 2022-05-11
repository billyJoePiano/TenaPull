package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.MapLookupDao;
import nessusTools.util.*;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a reusable "object lookup", for the "severity_base" objects included
 * in the scan info returned from the Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "SeverityBase")
@Table(name = "severity_base")
public class SeverityBase extends GeneratedIdPojo
        implements MapLookupPojo<SeverityBase> {

	public static final MapLookupDao<SeverityBase> dao = new MapLookupDao<SeverityBase>(SeverityBase.class);

	@Column
	@JsonProperty
	private String display;

	@Column
	@JsonProperty
	private String value;

	public String getDisplay(){
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getValue(){
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
    }

    @Override
    public void _set(SeverityBase o) {
        this.__set(o);
        this.display = o.display;
        this.value = o.value;
    }

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(SeverityBase o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.display, o.display)
                && Objects.equals(this.value, o.value)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }

    @Transient
    @JsonIgnore
    @Override
    public Map<String, Object> _getSearchMap() {
        return MakeMap.of(new Object[] {
                "display", this.display,
                "value", this.value,
                "extraJson", this.getExtraJson()
        });
    }


}
