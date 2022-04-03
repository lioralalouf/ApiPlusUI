package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.MobileApplicationGenerator;
import generators.ProfileGenerator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.MobileApplication;
import models.database.Profile;
import models.request.ConnectRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class ConnectAccountPositiveTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Connect user and partner")
    @Traceability(FS = {"1698", "1704"})
    public void tc01_provisionAndConnect() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        String partnerID = createPartner(extentTest, partnerRequest);
        String apiKey = generateApiKey(extentTest, partnerID);

        extentTest.info("Create a new user account");
        //create account
        Profile profile = ProfileGenerator.getProfile();
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

        extentTest.info("Create a new mobile device for the user");
        // Need Mobile App
        MobileApplication mobileApplication = MobileApplicationGenerator.getATTTE();
        RegisterMobileApplicationRequest registerMobileApplicationRequest = new RegisterMobileApplicationRequest(mobileApplication);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .request()
                .body(registerMobileApplicationRequest)
                .when()
                .log().all()
                .post("/application/mobile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Provision user and partner to get state token and extract the decoded provision ID from it");
        //provision
        Response response3 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .post("/data/provision/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor2 = response3.jsonPath();
        String stateToken = extractor2.get("stateToken");
        TevaAssert.assertNotNull(extentTest, stateToken, "State token is present in response");

        String provisionID = getProvisionID(stateToken);
        TevaAssert.assertNotNull(extentTest, provisionID, "Provision ID is present in response");

        extentTest.info("Insert the provision id in request body and connect the account of the user to the partner");
        //account connect
        ConnectRequest connectRequest = objectMapper.readValue(Utils.readRequest("data", "connect"),
                ConnectRequest.class);
        connectRequest.connection.provisionID = provisionID;

        Response connectRes = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", profile.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .body(connectRequest)
                .when().log().all()
                .post("/account/connect")
                .then()
                .log()
                .all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, connectRes.getStatusCode(), 200, "Successful connection between partner and user");

        extentTest.info("Get user account and verify the user's data transfer consent status to this partner is active in Http Response");
        //Get account
        Response resGetAccount = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .request()
                .when()
                .log().all()
                .get("/account")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath js2 = resGetAccount.jsonPath();
        List<LinkedHashMap<String, String>> allConsents = js2.getList("account.consents");
        for (LinkedHashMap<String, String> consent : allConsents) {
            if (consent.get("consentType").equalsIgnoreCase("dataTransferConsent")) {
                if (consent.get("partnerID").equalsIgnoreCase(partnerID)) {
                    TevaAssert.assertEquals(extentTest, consent.get("status"), "Active", "Data transfer consent status is active");
                }
            }
        }

    }
}
