package nessusTools.data.entity;

import com.fasterxml.jackson.annotation.*;
import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.ObjectLookupDao;

import javax.persistence.*;

@Entity(name = "SeverityBase")
@Table(name = "severity_base")
public class SeverityBase extends GeneratedIdPojo
        implements ObjectLookupPojo<SeverityBase> {
	public static final ObjectLookupDao<SeverityBase> dao = new ObjectLookupDao<SeverityBase>(SeverityBase.class);

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

    @Override
    public void _set(SeverityBase other) {
        //TODO
    }
}
