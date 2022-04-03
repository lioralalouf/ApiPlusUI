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
public class DataGetKeyByPartnerNegativeTest extends PartnerApiTestBase {


    @Test(priority = 1, testName = "Get api key details by partner with only 'CREATE KEY' scope")
    @Traceability(FS = {"1655", "1660"})
    public void tc01_generateNewPrtnerApiKey() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        extentTest.info("Generate api key for the partner by admin with key creation scope only");
        apiKeyRequest.scopes = new String[]{"data:api:key:create"};

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
        TevaAssert.assertNotNull(extentTest, apiKey, "Api key in response is not null");

        extentTest.info("Get the generated api key details, Expect HTTP Response Error Code 403 because Api out of partner's allowed scope");
        Response response3 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .when()
                .get("/data/api/key")
                .then()
                .log()
                .all()
                .extract()
                .response();

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 403, "Getting HTTP Response Error Code 403 because Api out of partner's allowed scopes");
    }

    @Test(priority = 2, testName = "Generate new api key by partner with only 'READ KEY' scope")
    @Traceability(FS = {"1655", "1660"})
    public void tc02_generateNewPartnerApiKey() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate api key for the partner by admin key read scope only");
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);


        apiKeyRequest.scopes = new String[]{"data:api:key:read"};

        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .body(apiKeyRequest)
                .when()
                .log()
                .all()
                .post("/{partnerID}/key")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response.jsonPath();
        String apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        TevaAssert.assertNotNull(extentTest, apiKey, "Api key in response is not null");

        extentTest.info("Generate api key by partner, Expect HTTP Response Error Code 403 because Api out of partner's allowed scope");
        Response response2 = generateApiKeyByPartner(extentTest, apiKey);

        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 403, "Getting HTTP Response Error Code 403 because Api out of partner's allowed scopes");
    }
}
