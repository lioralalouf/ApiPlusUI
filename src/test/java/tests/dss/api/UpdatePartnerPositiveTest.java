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
public class UpdatePartnerPositiveTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Update partner email details, expecting HTTP Response Code `200` and partner's email to be updated")
    @Traceability(FS = {"1600"})
    public void tc01_UpdatePartner() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.contact.email = "beforeUpdate@gmail.com";

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Onboard a new partner");
        // get partner details after onboarding
        String response2 =
                given()
                        .filter(new ConsoleReportFilter(extentTest))
                        .baseUri(Utils.readProperty("adminUrl"))
                        .basePath("configuration/partners")
                        .header("Authorization", "Bearer " + accessToken)
                        .pathParam("partnerID", partnerID)
                        .when().log().all()
                        .get("/{partnerID}")
                        .then()
                        .log().all()
                        .assertThat().statusCode(200)
                        .extract().response().asString();

        JsonPath js2 = new JsonPath(response2);
        String responseEmail = js2.getString("contact.email");
        extentTest.info("Verify partner's email equals to 'beforeUpdate@gmail.com'");
        TevaAssert.assertEquals(extentTest, responseEmail, "beforeUpdate@gmail.com", "original email has been updated successfully");

        extentTest.info("Update partner's email to 'afterUpdate@gmail.com'");
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.contact.email = "afterUpdate@gmail.com";


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

        extentTest.info("Get partner details after email update");
        // get partner details after onboarding
        Response response4 =
                given()
                        .filter(new ConsoleReportFilter(extentTest))
                        .baseUri(Utils.readProperty("adminUrl"))
                        .basePath("configuration/partners")
                        .header("Authorization", "Bearer " + accessToken)
                        .pathParam("partnerID", partnerID)
                        .when().log().all()
                        .get("/{partnerID}")
                        .then()
                        .log().all()
                        .extract().response();

        extentTest.info("Verify partner's email equals to 'beforeUpdate@gmail.com'");
        JsonPath extractor = response4.jsonPath();
        String responseEmail2 = extractor.get("contact.email").toString();
        TevaAssert.assertEquals(extentTest, responseEmail2, "afterUpdate@gmail.com", "new email has been updated succesfully");
        String ResponsePartnerID = extractor.get("partnerID").toString();

        extentTest.info("Verify Http Response status code is '200'");
        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
        extentTest.info("Verify The partner ID after update is identical to the original Partner ID");
        TevaAssert.assertEquals(extentTest, ResponsePartnerID, partnerID, "Partner Id should be identical to the original");

        extentTest.info("Verify All fields are displayed in the HTTP Response");
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
        TevaAssert.assertNotNull(extentTest, extractor.get("onDone"), "onDone should be present");

	}
}
