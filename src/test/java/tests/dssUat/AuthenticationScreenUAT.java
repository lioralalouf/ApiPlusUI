package tests.dssUat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.testng.Assert;
import org.testng.annotations.Test;
import pageObjects.ConsentPage;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import requests.RestAssuredOAuth;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class AuthenticationScreenUAT extends UiBaseTest {
	String accessToken = "";
	String partnerID = "";
	String apiKey = "";
	String partnerNameTemp = "";
	private final ObjectMapper objectMapper = new ObjectMapper();


	@Severity(SeverityLevel.CRITICAL)
	@Story("As a DevOps user I want to create API keys and assign scopes that allow a partner to call the DHP and have their requests authorized")
	@Test(priority = 1, description = "Generate api key to partner and getting the api key details, expecting the correct scopes to be displayed.")
	@Description("Generate api key to partner")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc01_loginSuccesfully() throws IOException {
		accessToken = RestAssuredOAuth.getToken();

		PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
				PartnerRequest.class);
		partnerRequest.name = UUID.randomUUID().toString();

		RestAssured.baseURI = Utils.readProperty("adminUrl");
		String response = given()
				.header("Authorization", "Bearer " + accessToken).
				header("Content-Type", "application/json")
				.request()
				.body(partnerRequest).when()
				.log().all()
				.post("/configuration/partners")
				.then()
				.log().all()
				.assertThat()
				.statusCode(200)
				.extract().response().asString();

		System.out.println(response);

		JsonPath js = new JsonPath(response);
		this.partnerID = js.getString("partnerID");
		this.partnerNameTemp = partnerRequest.name;
		
		GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
				GenerateApiRequest.class);

		Response response2 = given()
				.baseUri(Utils.readProperty("adminUrl"))
				.basePath("configuration/partners")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + accessToken)
				.pathParam("partnerID", partnerID).body(apiKeyRequest)
				.when().post("/{partnerID}/key")
				.then()
				.log().all()
				.extract().response();

		JsonPath extractor = response2.jsonPath();
		this.apiKey = extractor.get("apiKey");
		System.out.println(apiKey);
		
		LocalStorage localStorage = ((WebStorage) driver).getLocalStorage();
		localStorage.removeItem("stateToken");
		
		VendorToolPage vendorToolPage = new VendorToolPage(driver);
		vendorToolPage.login("123456", apiKey);
		
		System.out.println(apiKey);
		
		PartnerLoginPage partnerLoginPage = new PartnerLoginPage(driver);
		boolean actual = partnerLoginPage.checkTitle();
		Assert.assertTrue(actual, "digihelerLoginPage is displayed");
		
		partnerLoginPage.login(Utils.readProperty("idHubUser"),Utils.readProperty("idHubPassword"));
		
		
		String consentTitle = Utils.readProperty("consentTitle");
		String partnerName = this.partnerNameTemp;
		ConsentPage cp = new ConsentPage(driver);
		String actualTitle  = cp.getTitleText();
		String expectedTitle = partnerName+" "+consentTitle;
		Assert.assertEquals(actualTitle, expectedTitle, "The correct title is displayed to the user");
	}
}
