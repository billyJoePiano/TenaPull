package nessusData.entity;

import com.fasterxml.jackson.annotation.*;
import nessusData.entity.template.*;
import nessusData.persistence.ObjectLookupDao;

import javax.persistence.*;

@Entity(name = "SeverityBase")
@Table(name = "severity_base")
public class SeverityBase extends GeneratedIdPojo {
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

	public String getValue(){
		return value;
	}
}
