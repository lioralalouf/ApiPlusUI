package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
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
public class GetPartnerDetailsPositiveTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "create new partner and getting this partner's details")
    @Traceability(FS = {"1603"})
    public void tc01_getPartnerDetails(String period) throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), period);

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.quota.period = period;

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Getting partner details");
        Response response2 =
                given()
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

        JsonPath extractor = response2.jsonPath();
        String ResponsePartnerID = extractor.get("partnerID").toString();
        extentTest.info("Verify HTTP Response status code is '200'");
        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        extentTest.info("Verify partner Id is identical to the onboarded partner ID");
        TevaAssert.assertEquals(extentTest, ResponsePartnerID, partnerID, "Partner Id should be identical to the original");

        extentTest.info("Verify all fields are displayed in the HTTP Response");
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
        TevaAssert.assertNotNull(extentTest, extractor.get("activePrivacyNoticeVersion"), " The activePrivacyNoticeVersion should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("activeMarketingConsent"), "The burst should be present");
        TevaAssert.assertNotNull(extentTest, extractor.get("activeSignature"), " The limit should be present.");
        TevaAssert.assertNotNull(extentTest, extractor.get("activeHipaaDisclosure"), "The period should be present");
    }
}
