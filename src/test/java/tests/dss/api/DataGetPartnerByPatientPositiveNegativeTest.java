package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.ProfileGenerator;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.Profile;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class DataGetPartnerByPatientPositiveNegativeTest extends PartnerApiTestBase {

    private Profile profile;
    private String apiKey;
    private String partnerID;

    @Test(priority = 1, testName = "Get partner data by patient - positive test")
    @Traceability(FS = {"1661"})
    public void tc01_getPartnerDataPositive() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate a new api key to onboarded partner");
        //generate api key
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        Response response = given().log().all()
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

        JsonPath extractor = response.jsonPath();
        this.apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        extentTest.info("Create new user account");
        //create account
        profile = ProfileGenerator.getProfile();
        ProfileCreationRequest profileCreationRequest = new ProfileCreationRequest(profile);

        given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("email", profile.getEmail())
                .header("external-entity-id", profile.getExternalEntityID())
                .request()
                .body(profileCreationRequest)
                .when()
                .log().all()
                .post("/account/profile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Get partner details by user, Verify this is the correct partner and verify getting all fields in HTTP Response");
        Response resGetData = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .pathParam("partnerID", partnerID)
                .when()
                .log().all()
                .get("/partner/{partnerID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath js2 = resGetData.jsonPath();
        TevaAssert.assertEquals(extentTest, js2.get("partner.partnerID"), partnerID, "User got the correct partner id detaild");
        TevaAssert.assertNotNull(extentTest, js2.get("partner.name"), "Partner name is displayed in response");
        TevaAssert.assertNotNull(extentTest, js2.get("partner.icon"), "Partner icon is displayed in response");
        TevaAssert.assertNotNull(extentTest, js2.get("partner.onDone"), "onDone is displayed in response");
        TevaAssert.assertNotNull(extentTest, js2.get("partner.redirects.success"), "Partner success redirect path is displayed in response");
        TevaAssert.assertNotNull(extentTest, js2.get("partner.redirects.failure"), "Partner failure redirect path is displayed in response");

    }

    @Test(priority = 2, testName = "Get partner data by patient for deleted partner - negative test")
    @Traceability(FS = {"1661"})
    public void tc02_getPartnerDataNegative() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Revoke partner's api key");
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
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

        TevaAssert.assertEquals(extentTest, response.statusCode(), 200, "Api key has been revoked successfully");

        extentTest.info("Delete the partner");
        Response response2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .when()
                .delete("/{partnerID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response2.statusCode(), 200, "Partner has been deleted successfully");

        extentTest.info("Get partner details by user, Error 404 expected because partner doesnt exist");
        Response resGetData = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .pathParam("partnerID", partnerID)
                .when()
                .log().all()
                .get("/partner/{partnerID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, resGetData.statusCode(), 404, "Error 404 expected, because partner doesnt exist");
    }
}



