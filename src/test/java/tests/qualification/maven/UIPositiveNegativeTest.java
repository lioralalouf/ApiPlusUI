package tests.qualification.maven;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
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
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static java.lang.Thread.sleep;

@Listeners(TestListeners.class)
public class UIPositiveNegativeTest extends UiBaseTest {
    private String accessToken = "";
    private String partnerID = "";
    private String apiKey = "";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(priority = 1, testName = "Prove that the tool can be used to show negative test scenarios for UIs")
    @Traceability(URS = {"1719","1720"})
    public void tc01_UI_negative_test() throws IOException {
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

        JsonPath js = new JsonPath(response);
        this.partnerID = js.getString("partnerID");


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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "Privacy Notice file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "Terms Of Use file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileC.getStatusCode(), "Hipaa consent file uploaded successfully");

        //upload marketing for the partner
        File marketingTxt = new File("./documents/marketing.txt");
        Response responseFileD = given()
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

        TevaAssert.assertEquals(extentTest, 200, responseFileD.getStatusCode(), "Marketing Consent file uploaded successfully");
        //upload signature and conditions for the partner
        File signatureTxt = new File("./documents/signature.txt");
        Response responseFileE = given()
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

        TevaAssert.assertEquals(extentTest, 200, responseFileE.getStatusCode(), "Signature Policy file uploaded successfully");

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

        vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);

        PartnerLoginPage partnerrLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnerrLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest, actual, "Partner Login Page is displayed");

        partnerrLoginPage.login(Utils.readProperty("invalidUser"), Utils.readProperty("idHubPassword"));
        String actualError = partnerrLoginPage.getEmptyFieldErrorMsg();
        extentTest.info("fill in wrong user name with correct password");
        takeScreenshot(extentTest);
        String ExpectedError = Utils.readProperty("partnerLoginError3");
        TevaAssert.assertEquals(extentTest, actualError, ExpectedError, "error message for wrong user name field is correct");

    }

    @Test(priority = 2, testName = "Prove that the tool can be used to show positive test scenarios for UIs")
    @Traceability(URS = {"1719","1720"})
    public void tc02_positive_test() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        driver.navigate().refresh();

        String currentPage = driver.getCurrentUrl();

        PartnerLoginPage partnerrLoginPage = new PartnerLoginPage(driver);
        partnerrLoginPage.login2(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));
        extentTest.info("Login with valid credentials");
        takeScreenshot(extentTest);
        partnerrLoginPage.clickLogin2();

        ConsentPage cp = new ConsentPage(driver);
        cp.getTitleText();
        cp.choosePerson(Utils.readProperty("guardianName"));
        extentTest.info("Next page is displayed - user selection");
        takeScreenshot(extentTest);

        TevaAssert.assertNotEquals(extentTest,currentPage, driver.getCurrentUrl(),"After clicking the login button the user should be redirected to a new page.");

        cp.clickContinue();

        AuthorizationPage ap = new AuthorizationPage(driver);
        sleep(2000);
        extentTest.info("Webpage screenshot before clicking the checkbox");
        takeScreenshot(extentTest);
        ap.clickCheckbox();
        extentTest.info("Webpage screenshot after clicking the checkbox");
        takeScreenshot(extentTest);

        RestAssured.baseURI = Utils.readProperty("adminUrl");
        given()
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



