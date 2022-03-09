package nessusData.entity;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.annotation.*;

public class SeverityBaseSelectionsItem{

	@JsonProperty("display")
	private String display;

	@JsonProperty("value")
	private String value;

	public String getDisplay(){
		return display;
	}

	public String getValue(){
		return value;
	}
}
