package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.PartnerRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class CreatePartnerPositiveTest extends PartnerApiTestBase {

    private String partnerID = "";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(priority = 0, testName = "Onboard a new partner with 'period'", dataProvider = "getPeriodTrue", dataProviderClass = models.DataProviders.class)
    @Traceability(FS = {"1599", "1604"})
    public void tc01_createNewPartner(String period) throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), period);

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.quota.period = period;

        RestAssured.baseURI = Utils.readProperty("adminUrl");
        String response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("cognito-role", "Admin")
                .request()
                .body(partnerRequest)
                .when()
                .log().all()
                .post("/configuration/partners")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        this.partnerID = js.getString("partnerID");
        this.registerPartnerID(partnerID);
    }

    @Test(priority = 1, testName = "Get the onboarded partner details")
    @Traceability(FS = {"1599", "1604"})
    public void tc02_getSpecificPartnerDetails() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .when()
                .log().all()
                .when()
                .get("/{partnerID}")
                .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response();

        JsonPath extractor = response.jsonPath();
        String ResponsePartnerID = extractor.get("partnerID").toString();

        TevaAssert.assertEquals(extentTest, ResponsePartnerID, partnerID, "Partner Id should be identical to the original");
        TevaAssert.assertNotNull(extentTest, extractor.get("callbacks.success"), "The callbacks.success should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("callbacks.failure"), "The callbacks.failure should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("redirects.success"), "The redirects.success should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("redirects.failure"), "The redirects.failure should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("icon"), "The icon should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("contact.firstName"), " The firstName should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("contact.lastName"), "lastName should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("contact.phoneNumber"), " The phoneNumber should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("contact.email"), "The email should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("throttle.rate"), " The rate should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("throttle.burst"), "The burst should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("quota.limit"), " The limit should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("quota.period"), "The period should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("usagePlanID"), "The usagePlanID should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("activePrivacyNoticeVersion"), "The activePrivacyNoticeVersion should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("activeMarketingConsent"), "The burst should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("activeSignature"), " The limit should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("activeHipaaDisclosure"), "The period should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("onDone"), "onDone should be present");
        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
    }
}
