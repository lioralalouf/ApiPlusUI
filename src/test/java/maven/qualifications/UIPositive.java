package maven.qualifications;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;

import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import pageObjects.AuthorizationPage;
import pageObjects.ConsentPage;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.Utils;

@Listeners(TestListeners.class)
public class UIPositive extends UiBaseTest {
	private String accessToken = "";
	private String partnerID = "";
	private String apiKey = "";
	private final ObjectMapper objectMapper = new ObjectMapper();
	private static ExtentReports extent = ExtentManager.createInstance();
	private static ThreadLocal<ExtentTest> extentest = new ThreadLocal<ExtentTest>();

	@Test(priority = 1, testName = "Invalid credentials login")
	@Traceability(URS = "x.x.x", FS = "x.x.x")
	public void tc01_invalidLogin() throws IOException {
		ExtentTest extentTest = ExtentManager.getTest(this.getClass());
		accessToken = RestAssuredOAuth.getToken();

		PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
				PartnerRequest.class);
		partnerRequest.name = UUID.randomUUID().toString();

		RestAssured.baseURI = Utils.readProperty("adminUrl");
		String response = given()
				.filter(new ConsoleReportFilter(extentTest))
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

		GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
				GenerateApiRequest.class);

		Response response2 = given()
				.filter(new ConsoleReportFilter(extentTest))
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

		VendorToolPage vendorToolPage = new VendorToolPage(driver);
		Assert.assertEquals(vendorToolPage.getTitle(), "Compatible Apps");
			
		vendorToolPage = new VendorToolPage(driver);
		vendorToolPage.login("123456", apiKey);
		extentTest.pass("Fill in wrong cresentials - patient id + api key");
	
      
		PartnerLoginPage partnerrLoginPage = new PartnerLoginPage(driver);
		boolean actual = partnerrLoginPage.checkTitle();
		Assert.assertTrue(actual, "PartnerLoginPage is displayed");
		extentTest.pass("UI - PartnerLoginPage is displayed");
		
		partnerrLoginPage.login(Utils.readProperty("invalidUser"),Utils.readProperty("idHubPassword"));
		String actualError = partnerrLoginPage.getEmptyFieldErrorMsg();
		extentTest.info("fill in wrong user name with correct password");
		takeScreenshot(extentTest);
		extentTest.pass("Error message for wrong user name is equal to 'Please check your username or password'");
		String ExpectedError = Utils.readProperty("partnerLoginError3");
		Assert.assertEquals(actualError, ExpectedError, "error message for wrong user name field is correct");
		
	}
		@Test(priority = 1, testName = "Valid credentials login")
		@Traceability(URS = "x.x.x", FS = "x.x.x")
		public void tc02_validLogin() throws IOException, InterruptedException {
		ExtentTest extentTest = ExtentManager.getTest(this.getClass());	
		PartnerLoginPage partnerrLoginPage = new PartnerLoginPage(driver);
		partnerrLoginPage.login2(Utils.readProperty("idHubUser"),Utils.readProperty("idHubPassword"));
		extentTest.info("Login with valid credentials");
		takeScreenshot(extentTest);
		partnerrLoginPage.clickLogin2();
		
		ConsentPage cp = new ConsentPage(driver);
		String actualTitle  = cp.getTitleText();
		cp.choosePerson(Utils.readProperty("gurdianName"));
		extentTest.info("choose user and click continue");
		takeScreenshot(extentTest);
		cp.clickContinue();
		
		AuthorizationPage ap = new AuthorizationPage(driver);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".checkbox-outer img")));
		extentTest.info("Webpage screenshot before clicking the checkbox");
		takeScreenshot(extentTest);
		ap.clickCheckbox();
		extentTest.info("Webpage screenshot after clicking the checkbox");
		takeScreenshot(extentTest);
		
	
				
		//REVOKE API KEY AND DELETE PARTNER FROM DB
		RestAssured.baseURI = Utils.readProperty("adminUrl");
		Response response4 = given()
				.baseUri(Utils.readProperty("adminUrl"))
				.basePath("configuration/partners")
				.header("Content-Type", "application/json")
				.header("Authorization", "Bearer " + accessToken)
				.pathParam("partnerID", partnerID)
				.pathParam("apiKey", apiKey)
				.when()
				.delete("/{partnerID}/key/{apiKey}")
				.then()
				.log().all()
				.extract().response();

         Response response5 = given()
		.baseUri(Utils.readProperty("adminUrl"))
		.basePath("configuration/partners")
		.header("Content-Type", "application/json")
		.header("Authorization", "Bearer " + accessToken)
		.pathParam("partnerID", partnerID)
		.when()
		.delete("/{partnerID}")
		.then()
		.log().all()
		.extract().response();
         System.out.println(apiKey);
		}
	}
