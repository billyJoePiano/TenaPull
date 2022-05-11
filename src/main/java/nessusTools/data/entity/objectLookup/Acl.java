package nessusTools.data.entity.objectLookup;

import com.fasterxml.jackson.annotation.*;

import nessusTools.data.entity.template.*;
import nessusTools.data.persistence.*;

import javax.persistence.*;
import java.util.*;

/**
 * Represents a reusable "object lookup", for an acl object returned from the
 * Nessus API at /scans/&lt;scan-id&gt;
 */
@Entity(name = "Acl")
@Table(name = "acl")
public class Acl extends HashLookupTemplate<Acl> {

    /**
     * The dao for Acl
     */
    public static final HashLookupDao<Acl> dao
			= new HashLookupDao<Acl>(Acl.class);


    @Column(name = "nessus_id")
    @JsonProperty("id")
    private Integer nessusId;

	private Integer owner;

	private int permissions;

	private String name;

	@Column(name = "display_name")
	@JsonProperty("display_name")
	private String displayName;

	private String type;

    /**
     * Gets nessus id.
     *
     * @return the nessus id
     */
    public Integer getNessusId() {
        return nessusId;
    }

    /**
     * Sets nessus id.
     *
     * @param nessusId the nessus id
     */
    public void setNessusId(Integer nessusId) {
        this.nessusId = nessusId;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public Integer getOwner() {
		return owner;
	}

    /**
     * Sets owner.
     *
     * @param owner the owner
     */
    public void setOwner(Integer owner) {
		this.owner = owner;
	}

    /**
     * Gets permissions.
     *
     * @return the permissions
     */
    public int getPermissions() {
		return permissions;
	}

    /**
     * Sets permissions.
     *
     * @param permissions the permissions
     */
    public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
		return name;
	}

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
		this.name = name;
	}

    /**
     * Gets display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
		return displayName;
	}

    /**
     * Sets display name.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
		return type;
	}

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
		this.type = type;
	}

    @Transient
    @JsonIgnore
    @Override
    public void _prepare() {
        this.__prepare();
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

    @Transient
    @JsonIgnore
    @Override
    public boolean _match(Acl o) {
        if (o == this) return true;
        return o != null
                && Objects.equals(this.owner, o.owner)
                && Objects.equals(this.permissions, o.permissions)
                && Objects.equals(this.name, o.name)
                && Objects.equals(this.displayName, o.displayName)
                && Objects.equals(this.type, o.type)
                && Objects.equals(this.getExtraJson(), o.getExtraJson());
    }
}

