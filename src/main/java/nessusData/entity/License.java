package nessusData.entity;

import com.fasterxml.jackson.annotation.*;
import nessusData.entity.template.*;
import nessusData.persistence.*;

import javax.persistence.*;

@Entity(name = "License")
@Table(name = "license")
public class License extends GeneratedIdPojo {
	public static final ObjectLookupDao<License> dao
			= new ObjectLookupDao<License>(License.class);

	@Column(name = "`limit`")
	private String limit;

	private String trimmed;

	public String getLimit(){
		return limit;
	}

	public String getTrimmed(){
		return trimmed;
	}

}
