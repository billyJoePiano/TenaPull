package nessusData.entity;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;

public class AclsItem{

	@JsonProperty("owner")
	private Object owner;

	@JsonProperty("permissions")
	private int permissions;

	@JsonProperty("name")
	private Object name;

	@JsonProperty("id")
	private Object id;

	@JsonProperty("display_name")
	private Object displayName;

	@JsonProperty("type")
	private String type;

	public Object getOwner(){
		return owner;
	}

	public int getPermissions(){
		return permissions;
	}

	public Object getName(){
		return name;
	}

	public Object getId(){
		return id;
	}

	public Object getDisplayName(){
		return displayName;
	}

	public String getType(){
		return type;
	}
}
