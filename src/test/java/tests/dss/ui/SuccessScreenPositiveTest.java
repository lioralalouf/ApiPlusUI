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
import utils.FileUtils;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class SuccessScreenPositiveTest extends UiBaseTest {
    private String partnerNameTemp = "";
    private String partnerName = "";
    private String image1Hash;

    @Test(priority = 1, testName = "Check logo is displayed")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc01_checkAccesTokenCreated() throws IOException, ParseException, InterruptedException, NoSuchAlgorithmException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        //upload image
        File iconFile = new File("./icons/image1.jpg");
        image1Hash = FileUtils.getFileHash(iconFile);

        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", iconFile, "image/jpg")
                .log().all()
                .post("/configuration/image")
                .then()
                .log().all()
                .extract().response();

        JsonPath extract = responseFile.jsonPath();
        String iconUrl = extract.get("url");

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.icon = iconUrl;

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

        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);

        PartnerLoginPage partnerLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnerLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest,actual, "digihelerLoginPage is displayed");

        partnerLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));

        ConsentPage cp = new ConsentPage(driver);
        String consentTitle = Utils.readProperty("consentTitle");
        this.partnerName = partnerNameTemp;
        String actualTitle = cp.getTitleText();
        String expectedTitle = partnerName + " " + consentTitle;
        TevaAssert.assertEquals(extentTest,actualTitle, expectedTitle, "The correct title is displayed to the user");

        Thread.sleep(2000);
        cp.choosePerson(Utils.readProperty("guardianName"));
        cp.clickContinue();

        AuthorizationPage ap = new AuthorizationPage(driver);
        String actualTitle2 = ap.getTitle();
        String expectedTitle2 = ap.ReplaceTitleString("Lior Testing V3.3", partnerName);
        TevaAssert.assertEquals(extentTest,actualTitle2, expectedTitle2, "Authorization text for the user is correct");

        ap = new AuthorizationPage(driver);
        ap.clickCheckbox();
        ap.clickAccept();

        MarketingScreenPage marketingPage = new MarketingScreenPage(driver);
        String actualMarketingTitle = marketingPage.checkTitle();
        TevaAssert.assertEquals(extentTest, actualMarketingTitle, Utils.readProperty("marketingPageTitle"), "Navigated to marketing consent page succesfully");

        marketingPage.sign(Utils.readProperty("signatureName"));
        marketingPage.clickSignatureCheckbox();
        marketingPage.clickAccept();

        SuccessScreenPage successPage = new SuccessScreenPage(driver);

        String iconPath = successPage.getIconPath();
        String imageDownloadHash = FileUtils.getRemoteFileHash(successPage.getIconPath());

        TevaAssert.assertEquals(extentTest,iconPath, iconUrl, "Icon path should be identical");
        TevaAssert.assertEquals(extentTest,image1Hash, imageDownloadHash, "Icon hash should be identical");
    }

    @Test(priority = 2, testName = "Check titles are correct and includes the correct patient+partner ID's")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc02_checkTitles() throws IOException, ParseException, InterruptedException, NoSuchAlgorithmException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        SuccessScreenPage successPage = new SuccessScreenPage(driver);
        String actualTitle = successPage.getTitle();
        String expectedTitle = successPage.ReplaceTitleString(partnerName, Utils.readProperty("signatureName"));
        TevaAssert.assertEquals(extentTest, actualTitle, expectedTitle, "Success text has the correct parameters");
        TevaAssert.assertEquals(extentTest, successPage.getHeader(), "Success!", "Success text is displayed to the user");
    }

    @Test(priority = 3, testName = "Clicking on Done button executes JS")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc03_clickDone() throws IOException, ParseException, InterruptedException, NoSuchAlgorithmException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        SuccessScreenPage successPage = new SuccessScreenPage(driver);
        successPage.clickDone();

        //REVOKE API KEY AND DELETE PARTNER FROM DB
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        given()
                .filter(new ConsoleReportFilter(extentTest))
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
                .filter(new ConsoleReportFilter(extentTest))
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
