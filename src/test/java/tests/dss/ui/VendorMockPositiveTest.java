package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class VendorMockPositiveTest extends UiBaseTest {

    @Test(priority = 1,
            testName = "test1",
            description = "Generate api key to partner and getting the api key details, expecting the correct scopes to be displayed.")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc01_testVendorMockTool() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        this.partnerID = createPartner(extentTest);

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

        LocalStorage localStorage = ((WebStorage) driver).getLocalStorage();
        localStorage.removeItem("stateToken");

        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);

        System.out.println(apiKey);

        PartnerLoginPage digihelerLoginPage = new PartnerLoginPage(driver);
        boolean actual = digihelerLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest, actual, "digihelerLoginPage is displayed");

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
    }
}
