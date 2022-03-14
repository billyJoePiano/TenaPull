package nessusData.entity;

import com.fasterxml.jackson.annotation.*;
import nessusData.entity.template.*;
import nessusData.persistence.ObjectLookupDao;

import javax.persistence.*;

@Entity(name = "Acl")
@Table(name = "acl")
public class Acl extends NaturalIdPojo {
	public static final ObjectLookupDao<Acl> dao
			= new ObjectLookupDao<Acl>(Acl.class);

	@JsonProperty("owner")
	private Integer owner;

	@Column
	@JsonProperty("permissions")
	private int permissions;

	@Column
	@JsonProperty("name")
	private String name;

	@Column(name = "display_name")
	@JsonProperty("display_name")
	private String displayName;

	@Column
	@JsonProperty("type")
	private String type;

	public Integer getOwner() {
		return owner;
	}

	public void setOwner(Integer owner) {
		this.owner = owner;
	}

	public int getPermissions() {
		return permissions;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
