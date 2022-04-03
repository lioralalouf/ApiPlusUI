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
public class DataGetPartnerDetailsByPartnerNegativeTest extends PartnerApiTestBase {


    @Test(priority = 1, testName = "Get as A partner, information on partner with missing data:api:partner:read scope ")
    @Traceability(FS = {"1631"})
    public void tc01_createNewPartner() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate a new api key with missing 'Data Read' scope for onboarded partner");
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        apiKeyRequest.scopes = new String[]{"data:inhalation:read", "data:api:key:read", "data:inhaler:read"};

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
        TevaAssert.assertNotNull(extentTest, apiKey, "Api key field is not null in HTTP Response");

        extentTest.info("Get information on the partner, Expect HTTP Response Error Code 403 because Api out of partner's allowed scope");
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
                .extract().response();

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 403, "Request is expected to have HTTP Response Code `403` because Api out of partner's allowed scope");
    }
}
