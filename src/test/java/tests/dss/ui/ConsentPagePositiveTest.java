package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pageObjects.ConsentPage;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
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
import java.time.Duration;
import java.util.UUID;

import static io.restassured.RestAssured.given;

// bugs
@Listeners(TestListeners.class)
public class ConsentPagePositiveTest extends UiBaseTest {
    private String partnerNameTemp = "";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private String partnerID;


    @Test(priority = 1,testName = "Verify icon image has been uploaded successfully")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc01_testIconPath() throws IOException, NoSuchAlgorithmException {

        File iconFile = new File("./icons/image1.jpg");
        String image1Hash = FileUtils.getFileHash(iconFile);
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

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
        vendorToolPage.login("123456", apiKey);

        System.out.println(apiKey);

        PartnerLoginPage partnererLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnererLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest, actual, "digihelerLoginPage is displayed");

        partnererLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));

        ConsentPage cp = new ConsentPage(driver);
        String iconPath = cp.getIconPath();

        String imageDownloadHash = FileUtils.getRemoteFileHash(cp.getIconPath());

        TevaAssert.assertEquals(extentTest, iconPath, iconUrl, "Icon path should be identical");
        TevaAssert.assertEquals(extentTest, image1Hash, imageDownloadHash, "Icon hash should be identical");

        this.partnerNameTemp = partnerRequest.name;

    }

    @Test(priority = 2,testName = "Check text for the user includes partner name")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc02_checkTitleIncludesPartnerName() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        String consentTitle = Utils.readProperty("consentTitle");
        String partnerName = this.partnerNameTemp;
        ConsentPage cp = new ConsentPage(driver);
        String actualTitle = cp.getTitleText();
        String expectedTitle = partnerName + " " + consentTitle;
        TevaAssert.assertEquals(extentTest, actualTitle, expectedTitle, "The correct title is displayed to the user");
    }

    @Test(priority = 3,testName = "check if users are displayed")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc03_checkPersonExists() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        ConsentPage cp = new ConsentPage(driver);
        TevaAssert.assertTrue(extentTest, cp.checkGardianIsFirst("Myself"), "");
        TevaAssert.assertTrue(extentTest, cp.checkPersonExist(Utils.readProperty("guardianName"), Utils.readProperty("guardianDob")), "");
        TevaAssert.assertTrue(extentTest, cp.checkPersonExist(Utils.readProperty("dependent3Name"), Utils.readProperty("dependent3Dob")), "");
        TevaAssert.assertTrue(extentTest, cp.checkPersonExist(Utils.readProperty("dependent4Name"), Utils.readProperty("dependent4Dob")), "");
        TevaAssert.assertFalse(extentTest, cp.checkPersonExistByName(Utils.readProperty("dependent1Name")), "");
        TevaAssert.assertFalse(extentTest, cp.checkPersonExistByName(Utils.readProperty("dependent2Name")), "");
    }

    @Test(priority = 4, testName = "Verify updating icon image is displayed correctly",description = "check that the updated image has the correct new image path.")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc04_checkPathUpdateImage() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        File iconFile = new File("./icons/image2.jpg");
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
        String iconUrl2 = extract.get("url");

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = this.partnerNameTemp;
        partnerRequest.icon = iconUrl2;
        
        System.out.println("partner id now is - "+ partnerID);

        String response =
        		given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .pathParam("partnerID", partnerID)
                .request()
                .body(partnerRequest)
                .when().log().all()
                .put("/{partnerID}")
                .then().log().all()
                .assertThat().statusCode(200)
                .extract().response().asString();

        ConsentPage cp = new ConsentPage(driver);

        driver.navigate().to(Utils.readProperty("mockToolUrl"));
        VendorToolPage vendorToolPage = new VendorToolPage(driver);

        vendorToolPage.login("123456", apiKey);

        cp = new ConsentPage(driver);
        String iconPath2 = cp.getIconPath();
        TevaAssert.assertEquals(extentTest, iconPath2, iconUrl2, "Updated Icon path should be identical");
        
    }
    
        @Test(priority = 5,testName = "Check that clicking cancel, navigate the user to vendor tool page")
        @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
        public void tc05_checkPathUpdateImage() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());	
        ConsentPage cp = new ConsentPage(driver);
        cp.clickCancel();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#partner-connect")));
        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        TevaAssert.assertEquals(extentTest, vendorToolPage.getTitle(), "Compatible Apps", "");

        //revoke apikey and delete partner
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response2 = given()
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

        Response response3 = given()
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

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
    }
}
