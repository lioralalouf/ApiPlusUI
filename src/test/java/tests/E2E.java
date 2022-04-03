package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import files.Payload;
import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.PartnerRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import requests.RestAssuredOAuth;
import annotations.Traceability;
import utils.Utils;

import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Epic(value= "DSS")
@Feature("Verify the end to end functionality of the DSS feature.")
public class E2E extends UiBaseTest {
	private String accessToken = "";
	private String partnerID ="";
	private String apiKey ="";
	private final ObjectMapper objectMapper = new ObjectMapper();


	@Severity(SeverityLevel.CRITICAL)
	@Story("As A devops, I supposed to create A new partner.")
	@Test(priority = 1,description = "Get access token for service account Aand create A new partner")
	@Description("Create a partner")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc01_createNewPartner() throws IOException {
		accessToken = RestAssuredOAuth.getToken();

		PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner","newPartner"), PartnerRequest.class);
		partnerRequest.name = UUID.randomUUID().toString();

		RestAssured.baseURI=Utils.readProperty("adminUrl");
		String response = given()
				//.filter(new AllureRestAssured())
				//.filter(new TevaReportFilter())
				.header("Authorization", "Bearer "+accessToken)
				.header("Content-Type","application/json")
				.request().body(partnerRequest)
				.when()
				.log().all()
				.post("/configuration/partners")
				.then().log().all().assertThat().statusCode(200).extract().response().asString();

		JsonPath js = new JsonPath(response);
		this.partnerID = js.getString("partnerID");
		System.out.println(partnerID);
	}

	@Story("As a member of the DevOps team, I want to know that I've onboarded a partner correctly, so I can feel confident I performed my job correctly.")
	@Test(priority = 2,description = "Confirm the partner was onboarded correctly.")
	@Description("Get Partner Details")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc02_getSpecificPartnerDetails() {
		//RestAssured.baseURI="https://dev.dhp.ehealth.teva/admin";

		Response response =
				given()
						.filter(new AllureRestAssured())
						.baseUri(Utils.readProperty("adminUrl"))
						.basePath("configuration/partners")
						.header("Authorization", "Bearer "+accessToken)
						.pathParam("partnerID", partnerID)
						.when()
						.log()
						.all()
						.when()
						.get("/{partnerID}")
						.then()
						.log()
						.all()
						.assertThat().statusCode(200)
						.extract().response();

	}

	@Severity(SeverityLevel.CRITICAL)
	@Story("As A devops, I want the ability to generate A new api key for the parner.")
	@Test(priority = 3,description = "Generate A new api key for the partner")
	@Description("Generate API Key")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc03_generateApiKey() {
		String apiKey = given()
				.filter(new AllureRestAssured())
				.baseUri(Utils.readProperty("adminUrl"))
				.basePath("configuration/partners")
				.header("Content-Type","application/json")
				.header("Authorization", "Bearer "+accessToken)
				.pathParam("partnerID", partnerID)
				.body(Payload.generateApiKey())
				.when().post("/{partnerID}/key")
				.then()
				.log()
				.all()
				.extract().path("apiKey");


		System.out.println(apiKey);
	}

	@Severity(SeverityLevel.CRITICAL)
	@Story("As a partner, I expect to be able to enter a patient's MRN and my partner API, so that I can provide this information to Teva. ")
	@Test(priority = 4,description = "Check that after pressing connect button, im redirected to digiheler login page")
	@Description("Validate Mock Tool")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc04_validMockPartnerTool() throws ParseException {
		//WebDriver myDriver = getDriver();
		VendorToolPage vendorToolPage = new VendorToolPage(driver);
		vendorToolPage.login("123456", apiKey);
	}

	@Severity(SeverityLevel.CRITICAL)
	@Story("As a patient, when I get to the login page I expected to provide valid credentials, to prove my account is secure.")
	@Test(priority = 5,description = "Check that after pressing login button, im redirected to the DSS Profile Selection page")
	@Description("Login to IDH")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc05_validIdhLogin() throws ParseException {

		PartnerLoginPage digihelerLoginPage = new PartnerLoginPage(driver);
		boolean actual = digihelerLoginPage.checkTitle();

		digihelerLoginPage.login("testpatient@yopmail.com","Teva000000");
	}

	@Severity(SeverityLevel.CRITICAL)
	@Story("As a patient, when I have provided valid credentials, then I expect to see a patient selector.")
	@Test(priority = 6,description = "We will confirm the stateToken passed is accessible with the correct partner ID.")
	@Description("View DSS Landing Page")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc06_validateDssLandingPage() throws ParseException {

		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body > app-root > app-data-consent > div > section > button.button")));

		LocalStorage localStorage = ((WebStorage) driver).getLocalStorage();

		Base64.Decoder dec = Base64.getDecoder();
		String decodedToken = new String(dec.decode(localStorage.getItem("stateToken")));

		JSONParser parser = new JSONParser();
		JSONObject jsonToken = (JSONObject) parser.parse(decodedToken);

}

}

