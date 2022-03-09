package nessusData.entity;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;

public class LicenseInfo{

	@JsonProperty("limit")
	private String limit;

	@JsonProperty("trimmed")
	private String trimmed;

	public String getLimit(){
		return limit;
	}

	public String getTrimmed(){
		return trimmed;
	}
}
