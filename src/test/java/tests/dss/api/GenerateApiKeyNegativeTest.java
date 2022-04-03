package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
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
public class GenerateApiKeyNegativeTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Generate a new api key to parner with invalid scopes, Request is expected to have HTTP Response Code `400`",
            dataProvider = "getScopeInvalid", dataProviderClass = models.DataProviders.class)
    @Traceability(FS = {"1645", "1605"})
    public void tc01_generatePartnerApiKeyInvalidScope(String scope) throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), scope);

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        extentTest.info("Onboard a new partner");
        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate a new api key with invalid scope");
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        apiKeyRequest.scopes[4] = scope;

        Response response2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .body(apiKeyRequest)
                .when()
                .log().all()
                .post("/{partnerID}/key").
                then()
                .log().all()
                .extract().response();
        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 400, "Request is expected to have HTTP Response Code `400`");

    }
}
