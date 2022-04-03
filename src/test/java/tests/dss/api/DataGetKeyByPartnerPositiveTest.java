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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class DataGetKeyByPartnerPositiveTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Generate 6 api keys by the partner and getting the api key details, Expecting that after 6 api keys, the first one will be revoked")
    @Traceability(FS = {"1655", "1660"})
    public void tc01_generateNewPartnerApiKey() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate api key number 1");
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

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
        TevaAssert.assertNotNull(extentTest, apiKey, "Api key in HTTP Response is not null");

        extentTest.info("Get api key number 1 details");
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


        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "First api key has been generated successfully");

        //As A partner generate 5 more api keys
        extentTest.info("Generate api key number 2");
        Response apiKey2Response = generateApiKeyByPartner(extentTest, apiKey);
        JsonPath extractor1 = apiKey2Response.jsonPath();
        String apiKey2 = extractor1.get("apiKey");
        String expirationDate2 = extractor1.get("grantExpirationDate");

        registerApiKey(partnerID, apiKey2);
        TevaAssert.assertEquals(extentTest, apiKey2Response.getStatusCode(), 200, "Second api key has been generated successfully");

        extentTest.info("Generate api key number 3");
        Response apiKey3Response = generateApiKeyByPartner(extentTest, apiKey);
        JsonPath extractor2 = apiKey3Response.jsonPath();
        String apiKey3 = extractor2.get("apiKey");
        String expirationDate3 = extractor2.get("grantExpirationDate");

        registerApiKey(partnerID, apiKey3);
        TevaAssert.assertEquals(extentTest, apiKey3Response.getStatusCode(), 200, "Third api key has been generated successfully");

        extentTest.info("Generate api key number 4");
        Response apiKey4Response = generateApiKeyByPartner(extentTest, apiKey);
        JsonPath extractor3 = apiKey4Response.jsonPath();
        String apiKey4 = extractor3.get("apiKey");
        String expirationDate4 = extractor3.get("grantExpirationDate");

        registerApiKey(partnerID, apiKey4);
        TevaAssert.assertEquals(extentTest, apiKey4Response.getStatusCode(), 200, "Fourth api key has been generated successfully");

        extentTest.info("Generate api key number 5");
        Response apiKey5Response = generateApiKeyByPartner(extentTest, apiKey);
        JsonPath extractor4 = apiKey5Response.jsonPath();
        String apiKey5 = extractor4.get("apiKey");
        String expirationDate5 = extractor4.get("grantExpirationDate");

        registerApiKey(partnerID, apiKey5);
        TevaAssert.assertEquals(extentTest, apiKey5Response.getStatusCode(), 200, "Fifth api key has been generated successfully");


        // Response getApiKey1 = getApiKeyByPartner(extentTest, apiKey);
        Response getApiKey2 = getApiKeyByPartner(extentTest, apiKey2);
        Response getApiKey3 = getApiKeyByPartner(extentTest, apiKey3);
        Response getApiKey4 = getApiKeyByPartner(extentTest, apiKey4);
        Response getApiKey5 = getApiKeyByPartner(extentTest, apiKey5);

        //Request scopes list
        List<String> requestScopes = new ArrayList<>();
        requestScopes.add("data:inhalation:read");
        requestScopes.add("data:inhaler:read");
        requestScopes.add("data:dsa:read");
        requestScopes.add("data:connection:delete");
        requestScopes.add("data:api:partner:read");
        requestScopes.add("data:api:key:read");
        requestScopes.add("data:api:key:create");

        extentTest.info("Get api key number 2 details");
        //Get details for the api keys we generated
        JsonPath js2 = getApiKey2.jsonPath();
        List<String> scopes2 = js2.getList("apiKey.scopes");
        String responseDate2 = js2.get("apiKey.grantExpirationDate");
        TevaAssert.assertEquals(extentTest, expirationDate2, responseDate2, "expiration date for api key 2 is correct");
        TevaAssert.assertEquals(extentTest, scopes2, requestScopes, "scopes for api key 2 are correct");

        extentTest.info("Get api key number 3 details");
        JsonPath js3 = getApiKey3.jsonPath();
        List<String> scopes3 = js3.getList("apiKey.scopes");
        String responseDate3 = js3.get("apiKey.grantExpirationDate");
        TevaAssert.assertEquals(extentTest, expirationDate3, responseDate3, "expiration date for api key 3 is correct");
        TevaAssert.assertEquals(extentTest, scopes3, requestScopes, "scopes for api key 3 are correct");

        extentTest.info("Get api key number 4 details");
        JsonPath js4 = getApiKey4.jsonPath();
        List<String> scopes4 = js4.getList("apiKey.scopes");
        String responseDate4 = js4.get("apiKey.grantExpirationDate");
        TevaAssert.assertEquals(extentTest, expirationDate4, responseDate4, "expiration date for api key 4 is correct");
        TevaAssert.assertEquals(extentTest, scopes4, requestScopes, "scopes for api key 4 are correct");

        extentTest.info("Get api key number 5 details");
        JsonPath js5 = getApiKey5.jsonPath();
        List<String> scopes5 = js5.getList("apiKey.scopes");
        String responseDate5 = js5.get("apiKey.grantExpirationDate");
        TevaAssert.assertEquals(extentTest, expirationDate5, responseDate5, "expiration date for api key 5 is correct");
        TevaAssert.assertEquals(extentTest, scopes5, requestScopes, "scopes for api key 5 are correct");

        extentTest.info("Get api key number 1 details again and verify its still active");
        // Get the first api key details we generated
        Response getApiKey = getApiKeyByPartner(extentTest, apiKey);
        TevaAssert.assertEquals(extentTest, getApiKey.getStatusCode(), 200, "First api key is still active");

        extentTest.info("Generate api key number 6");
        //Generate As partner the sixth api key
        Response apiKey6Response = generateApiKeyByPartner(extentTest, apiKey5);
        JsonPath extractor5 = apiKey6Response.jsonPath();
        String apiKey6 = extractor5.get("apiKey");
        registerApiKey(partnerID, apiKey6);

        TevaAssert.assertEquals(extentTest, apiKey6Response.getStatusCode(), 200, "Sixth api key has been generated successfully");

        extentTest.info("Get api key number 1 details again and verify it has been deleted after generating the sixth api key");
        // Get the first api key details we generated again
        Response getApiKey1 = getApiKeyByPartner(extentTest, apiKey);
        TevaAssert.assertEquals(extentTest, getApiKey1.getStatusCode(), 401, "Expecting HTTP Response Error Code 401, because Api key doesn't exist");
    }

}
