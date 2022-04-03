package requests;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import java.util.Base64;

import static io.restassured.RestAssured.given;


public class Authentication {
    public static String username = "2jipgf29o16ci9u3m2nlda2emc";
    public static String password = "1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs";
	
	//@Test
	public void createBearerToken() {
		given().auth().basic("2jipgf29o16ci9u3m2nlda2emc", "1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs")
		.header("Content-Type", "application/x-www-form-urlencoded")
		.when()
		.post("oauth2/token").then().assertThat().log()
		.all().assertThat().statusCode(200);
	}
	
	//@Test
	public void createBearerToken2() {
		given().header("Username", "2jipgf29o16ci9u3m2nlda2emc").header("Password", "1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs").
		header("Content-Type", "application/x-www-form-urlencoded")
		.when()
		.post("oauth2/token").then().assertThat().log()
		.all().assertThat().statusCode(200);
	}
	
	//@Test
	public void createBearerToken3() {
	RestAssured.baseURI="https://onica-idhub-dev.auth.us-east-1.amazoncognito.com/";
	//PreemptiveBasicAuthScheme authScheme = new PreemptiveBasicAuthScheme();
	//authScheme.setUserName("2jipgf29o16ci9u3m2nlda2emc");
	//authScheme.setPassword("1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs");
	//RestAssured.authentication = authScheme;
	
	given().log().all().auth().preemptive().basic("2jipgf29o16ci9u3m2nlda2emc", "1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs")
	.header("Content-Type", "application/x-www-form-urlencoded")
	.when().post("oauth2/token").
	then().assertThat().log().all().statusCode(200);
	}
	//@Test
	public void createBearerToken4() {
		String response = given().auth().basic("2jipgf29o16ci9u3m2nlda2emc", "1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs").
		header("Username", "2jipgf29o16ci9u3m2nlda2emc")
		.header("Password", "1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs")
		.header("Content-Type", "application/x-www-form-urlencoded")
		.when()
		.post("oauth2/token").then().assertThat().log()
		.all().assertThat().statusCode(200).extract().response().asString();;
		System.out.println(response);
	}
	//@Test
	public void createBearerToken5(){
		RestAssured.baseURI="https://onica-idhub-dev.auth.us-east-1.amazoncognito.com/";
		RequestSpecification request = RestAssured.given();
		String credentials = "2jipgf29o16ci9u3m2nlda2emc:1apkotdd616qq1jcchuss137hlhpe4gn2uqi1bc7dc9nef2q9vqs";
		//byte[] encodedCredentials = Base64.encodeBase64(credentials.getBytes());
		//String encodedCredentialsAsString = new String(encodedCredentials);
		
		//String response = given().header("Authorization", encodedCredentialsAsString)
		//.when().post("oauth2/token").then().assertThat().log()
		//.all().assertThat().statusCode(200).extract().response().asString();;
		//System.out.println(response);
	}
	
    public static String encode(String str1, String str2) {
        return new String(Base64.getEncoder().encode((str1 + ":" + str2).getBytes()));
        
    }
	
    public static Response getCode() {
    	RestAssured.baseURI = "https://onica-idhub-dev.auth.us-east-1.amazoncognito.com/";
        String authorization = encode(username, password);
        System.out.println(username);

        return
                given().log().all()
                        .header("authorization", "Basic " + authorization)
                        .header("Content-Type", "application/x-www-form-urlencoded").body("grant_type=client_credentials")
                        //.contentType(ContentType.URLENC)
                        //.formParam("response_type", "access_token")
                        //.queryParam("client_id", clientId)
                        //.queryParam("redirect_uri", redirectUri)
                        //.queryParam("scope", scope)
                        .post("oauth2/token")
                        .then()
                        .statusCode(200)
                        .extract()
                        .response();
    }

    public static String parseForOAuth2Code(Response response) {
        return response.jsonPath().getString("access_token");
        
    }

    //@Before
    public static void setup() {
        RestAssured.baseURI = "https://some-url.com";
    }

    @Test
    public void iShouldGetCode() {
        Response response = getCode();
        String code = parseForOAuth2Code(response);
        System.out.println(code);

        //Assertions.assertNotNull(code);
    }

}
