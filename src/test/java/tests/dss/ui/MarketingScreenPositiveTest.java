package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pageObjects.*;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class MarketingScreenPositiveTest extends UiBaseTest {
    private String partnerNameTemp = "";
    private String partnerName = "";

    @Test(testName = "Check marketing consent document has the correct version")
    @Traceability(URS = {"1689"}, FS = {"1669"})
    public void tc01_checkAccesTokenCreated() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Onboard new partner");
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
        
        extentTest.info("Upload privacy notive document to the partner");
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
        
        extentTest.info("Upload terms of use document to the partner");
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

        extentTest.info("Upload marketing consent document to the partner");
        //upload marketing for the partner
        File marketingTxt = new File("./documents/marketing.txt");
        Response responseFileC = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", marketingTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardMarketing01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "marketing consent file uploaded successfully");

        extentTest.info("Upload signature document to the partner");
        //upload signature and conditions for the partner
        File signatureTxt = new File("./documents/signature.txt");
        Response responseFileD = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardSignature01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "terms of use file uploaded successfully");

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        
        extentTest.info("Generate A new api key to the partner");
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

        extentTest.info("Navigate to 'vendor mock tool page' and Login to 'Partner login page'");
        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);
        takeScreenshot(extentTest);

        PartnerLoginPage partnerLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnerLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest,actual, "digihelerLoginPage is displayed");
        takeScreenshot(extentTest);

        extentTest.info("Login to 'Patient selection screen''");
        partnerLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));

        ConsentPage cp = new ConsentPage(driver);
        String consentTitle = Utils.readProperty("consentTitle");
        this.partnerName = partnerNameTemp;
        String actualTitle = cp.getTitleText();
        String expectedTitle = partnerName + " " + consentTitle;
        TevaAssert.assertEquals(extentTest,actualTitle, expectedTitle, "The correct title is displayed to the user");
        takeScreenshot(extentTest);

        extentTest.info("Choose user and click continue");
        Thread.sleep(2000);
        cp.choosePerson(Utils.readProperty("guardianName"));
        cp.clickContinue();
        takeScreenshot(extentTest);
        
        extentTest.info("Verify the title correct and includes the user and partner");
        AuthorizationPage ap = new AuthorizationPage(driver);
        String actualTitle2 = ap.getTitle();
        String expectedTitle2 = ap.ReplaceTitleString("Lior Testing V3.3", partnerName);
        TevaAssert.assertEquals(extentTest,actualTitle2, expectedTitle2, "Authorization text for the user is correct");
        takeScreenshot(extentTest);
        
        extentTest.info("Check checkbox and click on accept button");
        ap = new AuthorizationPage(driver);
        ap.clickCheckbox();
        ap.clickAccept();
        takeScreenshot(extentTest);
        
        
        extentTest.info("User has been navigated to marketing consent page, Verify the marketing consent document version is correct");
        MarketingScreenPage marketingPage = new MarketingScreenPage(driver);
        TevaAssert.assertEquals(extentTest, marketingPage.getMarketingText(), Utils.readProperty("marketingText"), "The correct marketing text is displayed");
        takeScreenshot(extentTest);

    }

    @Test(testName = "Ceck signature name and date presented to the user are correct")
    @Traceability(URS = {"1689"}, FS = {"1668"})
    public void tc02_checkSignatureNameAndDate() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        MarketingScreenPage marketingPage = new MarketingScreenPage(driver);
        
        extentTest.info("Verify the date and time for signature equals to current date and time");
        String signatureDate = marketingPage.getSignatureDate();
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy h:mm aa");
        String dateString = dateFormat.format(new Date());

        TevaAssert.assertEquals(extentTest, signatureDate, dateString, "The signature date is correct");
        takeScreenshot(extentTest);
        
        extentTest.info("Verify the name for signature equals to correct user name");
        String signatureName = marketingPage.getSignatureName();
        TevaAssert.assertEquals(extentTest, signatureName, Utils.readProperty("signatureName"), "The signature name is correct");
        takeScreenshot(extentTest);
    }

    @Test(testName = "Verify signature policy document has the correct version")
    @Traceability(URS = {"1689"}, FS = {"1668"})
    public void tc03_checkSignatureDocument() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Click on signature policy link");
        MarketingScreenPage marketingPage = new MarketingScreenPage(driver);
        String expected = Utils.readProperty("signatureText");
        String actual = marketingPage.clickSignaturePolicy();
        extentTest.info("Verify signatue policy document version is correct");
        TevaAssert.assertEquals(extentTest, actual, expected, "Signature policy text is correct");
        takeScreenshot(extentTest);
        marketingPage.closeSignatureDoc();
    }

    @Test(testName = "Verify marketing consent signed by the user")
    @Traceability(URS = {"1689"}, FS = {"1671"})
    public void tc04_verifySinature() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        MarketingScreenPage marketingPage = new MarketingScreenPage(driver);

        extentTest.info("Sign the user name in the signature field without capital letters, error message excepted - Your signature must match the name printed below the signature line");
        //test signature is case sensitive
        marketingPage.sign("lior testing V3.3");
        String actualError = marketingPage.getErrorMsg();
        String expectedError = Utils.readProperty("signatureErrorMsg");
        TevaAssert.assertEquals(extentTest, actualError, expectedError, "Error for wrong signature is correct");
        takeScreenshot(extentTest);
        extentTest.info("Check Accept Button isnt clickable with wrong signature");
        TevaAssert.assertFalse(extentTest, marketingPage.CheckAcceptBtnIsClickable(), "Accept button shouldnt be clickable");
        takeScreenshot(extentTest);
        marketingPage.clearSignature();
        
        extentTest.info("Check Accept Button isnt clickable withouth checking the checbox");
        //test accept button isnt clickable without click the checbox although signature is valid
        marketingPage.sign(Utils.readProperty("signatureName"));
        TevaAssert.assertFalse(extentTest, marketingPage.CheckAcceptBtnIsClickable(), "Accept button shouldnt be clickable");
        takeScreenshot(extentTest);

        extentTest.info("Check Accept Button is clickable with A valid signature and checked checbox");
        //test accept button is clickable when checkbox checked and signature is valid
        marketingPage.clickSignatureCheckbox();
        TevaAssert.assertTrue(extentTest, marketingPage.CheckAcceptBtnIsClickable(), "Accept button should be clickable");
        takeScreenshot(extentTest);
    }
    
    @Test(testName = "Dont authorize the marketing consent")
    @Traceability(URS = {"1689"}, FS = {"1669"})
    public void tc05_dontConsent() throws IOException, ParseException, InterruptedException {
    	ExtentTest extentTest = ExtentManager.getTest(this.getClass());
    	
    	extentTest.info("Click on skip and expect to be navigated directly to success page without consenting the marketing");
    	MarketingScreenPage marketingPage = new MarketingScreenPage(driver);
    	marketingPage.clickSkip();
    	SuccessScreenPage successPage = new SuccessScreenPage(driver);
        TevaAssert.assertEquals(extentTest, successPage.getHeader(), "Success!", "Success text is displayed to the user");
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
               // .filter(new ConsoleReportFilter(extentTest))
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

