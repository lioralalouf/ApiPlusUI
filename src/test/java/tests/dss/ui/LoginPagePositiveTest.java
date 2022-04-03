package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pageObjects.ConsentPage;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import reporter.ConsoleReportFilter;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class LoginPagePositiveTest extends UiBaseTest {
    private String partnerNameTemp = "";

    @Test(testName = "Login with invalid credentials")
    @Traceability(URS = {"1648"}, FS = {"1663","1664"})
    public void tc01_loginInvalidCred() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Onboard A new partner");

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
        this.partnerNameTemp = partnerRequest.name;
        
        extentTest.info("Generate A new api key to the onboarded partner");
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
        System.out.println(apiKey);

        //LocalStorage localStorage = ((WebStorage) driver).getLocalStorage();
        //localStorage.removeItem("stateToken");
        
        extentTest.info("Navigate to vendor mock tool and login to 'Authentication Page'");
        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);
        takeScreenshot(extentTest);

        System.out.println(apiKey);

        PartnerLoginPage partnerLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnerLoginPage.checkTitle();
        
        extentTest.info("On Authentication Page, Try Login with invalid credentials");

        partnerLoginPage.login(Utils.readProperty("idHubUser"), "");
        String actualError1 = partnerLoginPage.getEmptyFieldErrorMsg();
        String ExpectedError1 = Utils.readProperty("partnerLoginError1");
        TevaAssert.assertEquals(extentTest, actualError1, ExpectedError1, "error message for empty password field is correct");
        takeScreenshot(extentTest);

        partnerLoginPage.login("", Utils.readProperty("idHubPassword"));
        String actualError2 = partnerLoginPage.getEmptyFieldErrorMsg();
        String ExpectedError2 = Utils.readProperty("partnerLoginError2");
        TevaAssert.assertEquals(extentTest, actualError2, ExpectedError2, "error message for empty user name field is correct");
        takeScreenshot(extentTest);

        partnerLoginPage.login(Utils.readProperty("invalidUser"), Utils.readProperty("idHubPassword"));
        String actualError3 = partnerLoginPage.getEmptyFieldErrorMsg();
        String ExpectedError3 = Utils.readProperty("partnerLoginError3");
        TevaAssert.assertEquals(extentTest, actualError3, ExpectedError3, "error message for wrong user name field is correct");
        takeScreenshot(extentTest);

        partnerLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("invalidPaaword"));
        String actualError4 = partnerLoginPage.getEmptyFieldErrorMsg();
        String ExpectedError4 = Utils.readProperty("partnerLoginError3");
        TevaAssert.assertEquals(extentTest, actualError4, ExpectedError4, "error message for wrong password field is correct");
        takeScreenshot(extentTest);
    }

    @Test(testName = "Login with valid credentials")
    @Traceability(URS = {"1648"}, FS = {"1663","1664"})
    public void tc02_loginValidCred() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        PartnerLoginPage partnerLoginPage = new PartnerLoginPage(driver);
        partnerLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));
        
        extentTest.info("Login again with valid credentials");
   
        String consentTitle = Utils.readProperty("consentTitle");
        String partnerName = this.partnerNameTemp;
        ConsentPage cp = new ConsentPage(driver);
        String actualTitle = cp.getTitleText();
        String expectedTitle = partnerName + " " + consentTitle;
        TevaAssert.assertEquals(extentTest, actualTitle, expectedTitle, "The correct title is displayed to the user");
        takeScreenshot(extentTest);

        //revoke apikey and delete partner
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response3 = given()
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

        Response response4 = given()
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
