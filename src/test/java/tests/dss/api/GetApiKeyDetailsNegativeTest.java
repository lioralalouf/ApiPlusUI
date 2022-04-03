package tests.dss.api;

import annotations.Traceability;
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
import reporter.ConsoleReportFilter;
import utils.IpAddressUtils;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class GetApiKeyDetailsNegativeTest extends PartnerApiTestBase {
    private String apiKey = "";

    @Test(priority = 1, testName = "Get inactive api key details, Request is expected to have HTTP Response Code `401`")
    @Traceability(FS = {"1654"})
    public void tc01_getInactiveApiKeyDetails() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate a new api key to onboarded partner");
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
                .then().log()
                .all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        TevaAssert.assertNotNull(extentTest, apiKey, "api key field is not null");

        extentTest.info("Revoke the api key");
        Response response3 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .pathParam("apiKey", apiKey).when()
                .delete("/{partnerID}/key/{apiKey}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        extentTest.info("Get the revoked api key details, Http expected error code is 401");
        Response response4 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .when()
                .get("/data/api/key")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 401, "Request is expected to have HTTP Response Code `401`");

    }

    @Test(priority = 2, testName = "Get api key details for no whitelisted ip address, Request is expected to have HTTP Response Code `401`")
    @Traceability(FS = {"1654"})
    public void tc02_getInvalidApiAadressApiKeyDetails() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("My Ip is - " + IpAddressUtils.getPublicIpAddress());
        extentTest.info("Onboard a new partner");

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        apiKeyRequest.ipAddresses[0] = "7.125.34.140";

        extentTest.info("Generate a new api key with different ip from my local ip");
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
                .then().log()
                .all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Get the api key details, Http expected error code is 401");
        Response response3 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .when()
                .get("/data/api/key")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 401, "Request is expected to have HTTP Response Code `401`");

        //REVOKE API KEY AND DELETE PARTNER FROM DB
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        given()
                // .filter(new ConsoleReportFilter(extentTest))
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
