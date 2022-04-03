package files;

import com.fasterxml.jackson.annotation.JsonProperty;
import groovy.transform.builder.Builder;



@Builder
public class APIConfigaration {
	
	@JsonProperty("name")
	public static String name;
	
	@JsonProperty("icon")
	public static String icon;
	
	@JsonProperty("activePrivacyNoticeVersion")
	public static String activePrivacyNoticeVersion;
	
	@JsonProperty("activeTermsOfUse")
	public static String activeTermsOfUse;
	
	

	
	
	
	

}
