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
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class GenerateApiKeyPositiveTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Generate a new apikey to onboarded partner and getting its details", description = "Generate api key to partner and getting the api key details, expecting the correct scopes to be displayed.")
    @Traceability(FS = {"1645", "1605"})
    public void tc01_generateNewPartnerApiKey() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        extentTest.info("Onboard a new partner");
        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate a new api key to onboarded partner and verify api key"
                + " is not null in response");
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
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Get the generated api key details");
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

        LocalDate date = LocalDate.now(ZoneId.of("GMT"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        System.out.println("Now Date Is - " + date.format(formatter));
        String localDate = date.format(formatter);
        String localMonth = localDate.substring(5, 7);
        String localDay = localDate.substring(8, 10);


        JsonPath json = response3.jsonPath();
        String grantAccessDate = json.get("apiKey.grantAccessDate");
        System.out.println("answer is " + grantAccessDate);
        String grantAccessFullDate = grantAccessDate.substring(0, 10);
        String grantAccessYearStr = grantAccessDate.substring(0, 4);
        int grantAccessYearInt = Integer.parseInt(grantAccessYearStr);
        extentTest.info("Varify the access date is correct");
        TevaAssert.assertEquals(extentTest, localDate, grantAccessFullDate, "grant access date is correct");

        String grantExpirationDate = json.get("apiKey.grantExpirationDate");
        String grantExpirationYearStr = grantExpirationDate.substring(0, 4);
        int grantExpirationYearInt = Integer.parseInt(grantExpirationYearStr);
        String grantExpirationMonth = grantExpirationDate.substring(5, 7);
        String grantExpirationDay = grantExpirationDate.substring(8, 10);
        extentTest.info("Varify the grant expiration date is correct");
        TevaAssert.assertEquals(extentTest, (grantAccessYearInt + 1), grantExpirationYearInt, "Year of grant access is 1 year from now");
        TevaAssert.assertEquals(extentTest, localMonth, grantExpirationMonth, "grant expiration month is correct");
        TevaAssert.assertEquals(extentTest, localDay, grantExpirationDay, "grant expiration day is correct");


        List<Object> scopes = json.getList("apiKey.scopes");
        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
        //System.out.println(scopes);
        extentTest.info("Varify scope field is not null and all scopes are presented");
        TevaAssert.assertNotNull(extentTest, scopes, "scops fied is not null");
        String responseAsStr = response3.getBody().asString();
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("data:inhalation:read"), "");
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("data:inhaler:read"), "");
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("data:dsa:read"), "");
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("data:connection:delete"), "");
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("ata:api:partner:read"), "");
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("data:api:key:read"), "");
        TevaAssert.assertTrue(extentTest, responseAsStr.contains("data:api:key:create"), "");

        //REVOKE API KEY AND DELETE PARTNER FROM DB
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        given()
                //.filter(new ConsoleReportFilter(extentTest))
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
