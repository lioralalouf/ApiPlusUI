package requests;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import utils.Utils;

import java.util.Base64;

import static io.restassured.RestAssured.given;

public class RestAssuredOAuth {
    public static String username = "71a393h1mhgfpkub1bo57r749t";
    public static String password = "1k9mmujm5gskhcp2a5n3qbl4tlm8vcccml38bsbuem8ote36uk12";
    //public static String username = System.getenv("int_user");
    //public static String password = System.getenv("int_password");

    public static String encode(String str1, String str2) {
        return new String(Base64.getEncoder().encode((str1 + ":" + str2).getBytes()));
        
    }
	
    public static Response getCode() {
    	RestAssured.baseURI = Utils.readProperty("onicaUrl");
        String authorization = encode(username, password);

        return
                given().log().all()
                        .header("authorization", "Basic " + authorization)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .body("{\"grant_type\":\"client_credentials\"}")
                        .post("oauth2/token")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
    }

    public static String parseForOAuth2Code(Response response) {
        return response.jsonPath().getString("access_token");
        
    }
 
    public static String iShouldGetCode() {
        Response response = getCode();
        String code = parseForOAuth2Code(response);
        //Assert.assertNotNull(code);
		return code;
    }



   // @Step("get token{0}")
	public static String getToken() {
		RestAssured.baseURI = Utils.readProperty("onicaUrl");
		Response response = given()
				.auth().preemptive().basic(username, password)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.header("cognito-role", "Admin")
				.formParam("grant_type", "client_credentials")
				.when()
				.post("oauth2/token")
		  .then()
		  .extract()
		  .response();
	
		JsonPath js = response.jsonPath();
		String accessToken = js.getString("access_token");
		return accessToken;
	}

}

