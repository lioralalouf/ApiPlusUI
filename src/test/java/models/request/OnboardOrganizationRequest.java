package models.request;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OnboardOrganizationRequest {
	

    public class Settings {
        public String featureFlag1;
        public String featureFlag2;
        public String featureFlag3;
        public String featureFlag4;
    }
    
    public class Address {
    	public String street;
        public String city;
        public String region;
        public String country;
        public String postalCode;
    }
 
 /*   public static class Messages {
    	static class EsSv {
        	public String message1;
            public String message2;
            public String message3;
        }
    	static class EnUS {
        	public String message4;
            public String message5;
            public String message6;
        }
    } */

    public String name;
    public String mnemonic;
    public String description;
    public String logo;
    public String url;;
   // public Messages messages;
    public Settings settings;
    public Address address;
    public String activeTermsAndConditionsVersion;
    public String activePrivacyNoticeVersion;

    
  /*  @JsonIgnore
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("es-SV")
    public EsSv esSV;
    
    @JsonIgnore
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonProperty("en-US")
    public EnUS enUS;*/
}
