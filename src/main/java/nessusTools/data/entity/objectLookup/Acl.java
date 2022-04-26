package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.ObjectLookupDao;

import javax.persistence.*;

@Entity(name = "Acl")
@Table(name = "acl")
public class Acl extends NullableIdPojo<Acl> {

	public static final ObjectLookupDao<Acl> dao
			= new ObjectLookupDao<Acl>(Acl.class, true);

	private Integer owner;

	private int permissions;

	private String name;

	@Column(name = "display_name")
	@JsonProperty("display_name")
	private String displayName;

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

    @Override
    public void _set(Acl o) {
        this.__set(o);
        this.owner = o.owner;
        this.permissions = o.permissions;
        this.name = o.name;
        this.displayName = o.displayName;
        this.type = o.type;
    }
}
