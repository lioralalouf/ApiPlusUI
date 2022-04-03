package files;

public class Payload {
	static String random = StringRandom.randomMyString();
	static String random2 = StringRandom.randomMyString();
	
	public static String createPartner() {
		 
		return "{\r\n"
				+ "  \"name\": \""+random+"\"\",\r\n"
				+ "  \"icon\": \"http://www.example.com/dataPartner/logo.png\",\r\n"
				+ "  \"activePrivacyNoticeVersion\": \"1.0\",\r\n"
				+ "  \"activeTermsOfUse\": \"1.0\",\r\n"
				+ "  \"activeHipaaDisclosure\": \"1.0\",\r\n"
				+ "  \"activeSignature\": \"1.0\",\r\n"
				+ "  \"activeMarketingConsent\": \"1.0\",\r\n"
				+ "  \"redirects\": {\r\n"
				+ "    \"success\": \"http://www.example.com/connection/success\",\r\n"
				+ "    \"failure\": \"http://www.example.com/connection/success\"\r\n"
				+ "  },\r\n"
				+ "  \"callbacks\": {\r\n"
				+ "    \"success\": \"http://www.example.com/sucesss\",\r\n"
				+ "    \"failure\": \"http://www.example.com/failure\"\r\n"
				+ "  },\r\n"
				+ "  \"throttle\": {\r\n"
				+ "    \"rate\": 10,\r\n"
				+ "    \"burst\": 10\r\n"
				+ "  },\r\n"
				+ "  \"quota\": {\r\n"
				+ "    \"limit\": 10,\r\n"
				+ "    \"period\": \"day\"\r\n"
				+ "  },\r\n"
				+ "  \"contact\": {\r\n"
				+ "    \"firstName\": \""+random2+"\",\r\n"
				+ "    \"lastName\": \"Pizza\",\r\n"
				+ "    \"email\": \"tony.pizza@example.com\",\r\n"
				+ "    \"phoneNumber\": \"555-555-5555\"\r\n"
				+ "  }\r\n"
				+ "}";
		
	}
	public static String createPartner2() {
		return "{\r\n"
				+ "  \"location\": {\r\n"
				+ "    \"lat\": -38.383494,\r\n"
				+ "    \"lng\": 33.427362\r\n"
				+ "  },\r\n"
				+ "  \"accuracy\": 50,\r\n"
				+ "  \"name\": \"Frontline house\",\r\n"
				+ "  \"phone_number\": \"(+91) 983 893 3937\",\r\n"
				+ "  \"address\": \"29, side layout, cohen 09\",\r\n"
				+ "  \"types\": [\r\n"
				+ "    \"shoe park\",\r\n"
				+ "    \"shop\"\r\n"
				+ "  ],\r\n"
				+ "  \"website\": \"http://google.com\",\r\n"
				+ "  \"language\": \"French-IN\"\r\n"
				+ "}";
		
	}
	public static String generateApiKey() {
		return "{\r\n"
				+ "  \"ipAddresses\": [\r\n"
				+ "    \"73.16.242.154\",\r\n"
				+ "    \"73.16.242.152\",\r\n"
				+ "    \"31.154.45.78\",\r\n"
				+ "    \"77.125.35.89\",\r\n"
				+ "    \"192.168.19.229\"\r\n"
				+ "  ],\r\n"
				+ "  \"scopes\": [\r\n"
				+ "    \"data:api:partner:read\",\r\n"
				+ "    \"data:dsa:read\"\r\n"
				+ "  ]\r\n"
				+ "}";

	}
	
	

}
