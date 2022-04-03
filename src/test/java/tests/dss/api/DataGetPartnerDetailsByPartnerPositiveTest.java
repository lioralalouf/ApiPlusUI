package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
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
public class DataGetPartnerDetailsByPartnerPositiveTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Get as A partner, information on the partner")
    @Traceability(FS = {"1631"})
    public void tc01_createNewPartner() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        String partnerTempName = partnerRequest.name;

        String partnerID = createPartner(extentTest, partnerRequest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        extentTest.info("Generate a new api key for onboarded partner");
        Response response2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .body(apiKeyRequest)
                .when()
                .post("/{partnerID}/key")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        String apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Get information on the partner, Expect partner name to be correct and all fields to be displayed");
        Response response3 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .when()
                .log().all()
                .when()
                .get("data/api/partner")
                .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response();

        JsonPath extractor2 = response3.jsonPath();
        String ResponsePartnerName = extractor2.get("partner.partnerName").toString();

        TevaAssert.assertEquals(extentTest, ResponsePartnerName, partnerTempName, "Partner name should be identical to the original");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.accessGranted"), "The accessGranted should be present.");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.scopes"), "scopes should be present");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.contact.firstName"), " The firstName should be present.");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.contact.lastName"), "lastName should be present");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.contact.phoneNumber"), " The phoneNumber should be present.");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.contact.email"), "The email should be present");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.throttle.rate"), " The rate should be present.");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.throttle.burst"), "The burst should be present");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.quota.limit"), " The limit should be present.");
        TevaAssert.assertNotNull(extentTest, extractor2.get("partner.quota.period"), "The period should be present");
        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

    }
}
