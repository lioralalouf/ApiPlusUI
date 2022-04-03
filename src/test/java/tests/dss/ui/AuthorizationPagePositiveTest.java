package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.By;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pageObjects.AuthorizationPage;
import pageObjects.ConsentPage;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;

// bugs
@Listeners(TestListeners.class)
public class AuthorizationPagePositiveTest extends UiBaseTest {
    private String partnerID = "";
    private String partnerNameTemp = "";


    @Test(priority = 1, testName = "Get access token from local storage")
    @Traceability(URS = {"1647"}, FS = {"1741"})
    public void tc01_checkAccesTokenCreated() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Onboard A new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .request()
                .body(partnerRequest).when()
                .log().all()
                .post("/configuration/partners")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        this.partnerNameTemp = partnerRequest.name;
        JsonPath js = response.jsonPath();
        this.partnerID = js.getString("partnerID");
        
        extentTest.info("Uploading hipaa, privacy notice and terms of use documents");
        //upload privacy notice for the partner
        File pnTxt = new File("./documents/privacyNotice.txt");
        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", pnTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPn01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "privacy notice file uploaded successfully");

        //upload terms and conditions for the partner
        File termsTxt = new File("./documents/termsAndConditions.txt");
        Response responseFileB = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", termsTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardtou01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "terms of use file uploaded successfully");

        //upload hipaa for the partner
        File hippaTxt = new File("./documents/hipaa.txt");
        Response responseFileC = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", hippaTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardHipaa01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileC.getStatusCode(), "hipaa file uploaded successfully");
        
        extentTest.info("Generate new api key to the partner");

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        Response response2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .body(apiKeyRequest)
                .when().post("/{partnerID}/key")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");

        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
        TevaAssert.assertNotNull(extentTest, apiKey, "Api key is present in response");
        
        extentTest.info("Navigete to vendor mock tool page ");
        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);
        takeScreenshot(extentTest);
        
        extentTest.info("Login to the partner login page");
        PartnerLoginPage partnerLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnerLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest, actual, "PartnerLoginPage is displayed");

        partnerLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));
        ConsentPage cp = new ConsentPage(driver);
        extentTest.info("Extract provisio ID from state token");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div > section > button.button")));
        LocalStorage localStorage = ((WebStorage) driver).getLocalStorage();
        Base64.Decoder dec = Base64.getDecoder();
        String decodedToken = new String(dec.decode(localStorage.getItem("stateToken")));

        JSONParser parser = new JSONParser();
        JSONObject jsonToken = (JSONObject) parser.parse(decodedToken);
        String provisionID = jsonToken.get("provisionID").toString();
        TevaAssert.assertNotNull(extentTest,provisionID,"");     
    }

    @Test(priority = 2,testName = "Show the disclouse data with user and partner names")
    @Traceability(URS = {"1647"}, FS = {"1673"})
    public void tc02_showDiscloseData() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Login to patient selection page, verify the title includes the partner name");
        ConsentPage cp = new ConsentPage(driver);
        String consentTitle = Utils.readProperty("consentTitle");
        String partnerName = partnerNameTemp;
        String actualTitle = cp.getTitleText();
        String expectedTitle = partnerName + " " + consentTitle;
        TevaAssert.assertEquals(extentTest,actualTitle, expectedTitle, "The correct title is displayed to the user");
        takeScreenshot(extentTest);

        cp.choosePerson(Utils.readProperty("guardianName"));
        cp.clickContinue();
        
        extentTest.info("Choose user and click continue - user has been navigated to authorization page");
        AuthorizationPage ap = new AuthorizationPage(driver);
        String actualTitle2 = ap.getTitle();
        String expectedTitle2 = ap.ReplaceTitleString("Lior Testing V3.3", partnerName);
        TevaAssert.assertEquals(extentTest,actualTitle2, expectedTitle2, "Authorization text for the user is correct");
        takeScreenshot(extentTest);
        System.out.println(ap.ReplaceTitleString("Lior Testing V3.3", partnerName));
    }


    @Test(priority = 3,testName = "Upload consent documents files to partner and show them on webpage")
    @Traceability(URS = {"1647"}, FS = {"1670"})
    public void tc03_uploadDocuments() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Check documents have been uploaded succesfully with the correct version");
        AuthorizationPage ap = new AuthorizationPage(driver);
        String terms = ap.getTermsOfUseText();
        TevaAssert.assertEquals(extentTest,terms, Utils.readProperty("termsOfUseText"), "terms of use document version is correct");
        takeScreenshot(extentTest);
        String privacy = ap.getPrivacyNoticeText();
        TevaAssert.assertEquals(extentTest,privacy, Utils.readProperty("privacyNoticeText"), "privacy notice version is correct");
        takeScreenshot(extentTest);
        String hipaa = ap.getHipaaTxt();
        TevaAssert.assertEquals(extentTest,hipaa, Utils.readProperty("hipaaText"), "hipaa document version is correct");
        takeScreenshot(extentTest);
    }

    @Test(priority = 4,testName = "Do not authorize - decline authorization")
    @Traceability(URS = {"1647"}, FS = {"1672"})
    public void tc04_doNotAuthorize() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        AuthorizationPage ap = new AuthorizationPage(driver);
        extentTest.info("Click on decline authorization - verify user has been navigated  back to vendor tool page");
        ap.clickDecline();
        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#partner-connect")));
        TevaAssert.assertEquals(extentTest,vendorToolPage.getTitle(), "Compatible Apps","");
        takeScreenshot(extentTest);

        //REVOKE API KEY AND DELETE PARTNER FROM DB
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        given()
                //.filter(new ConsoleReportFilter(extentTest))
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

        given()
                //.filter(new ConsoleReportFilter(extentTest))
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
    }
}

