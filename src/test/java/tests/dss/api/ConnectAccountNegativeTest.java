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
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class ConnectAccountNegativeTest extends PartnerApiTestBase {
    private Profile profile;
    private MobileApplication mobileApplication;
    private String partnerID;
    private String apiKey;

    @Test(priority = 1, testName = "Connect user and partner with invalid provision ID")
    @Traceability(FS = {"1698", "1704"})
    public void tc01_provisionInvalidID() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);
        this.apiKey = generateApiKey(extentTest, partnerID);

        extentTest.info("Create a new user account");
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

        extentTest.info("Create a new mobile device for the user");
        // Need Mobile App
        mobileApplication = MobileApplicationGenerator.getATTTE();
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

        //account connect
        ConnectRequest connectRequest = objectMapper.readValue(Utils.readRequest("data", "connect"),
                ConnectRequest.class);

        //Generate Invalid provision
        extentTest.info("Generate invalid provision ID");
        connectRequest.connection.provisionID = UUID.randomUUID().toString();

        extentTest.info("Insert Invalid provision id in request body and connect the account of the user to the partner");
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
                .extract().response();

        TevaAssert.assertEquals(extentTest, connectRes.getStatusCode(), 400, "Expecting HTTP Response error code 400, because Invalid provisionID");
    }

    @Test(priority = 2, testName = "Connect user and partner with invalid api key")
    @Traceability(FS = {"1698", "1704"})
    public void tc02_provisionInvalidKey() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);
        this.apiKey = generateApiKey(extentTest, partnerID);

        //Generate Invalid api key
        this.apiKey = UUID.randomUUID().toString();

        extentTest.info("Create a new user account");
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

        extentTest.info("Create a new mobile device for the user");
        // Need Mobile App
        mobileApplication = MobileApplicationGenerator.getATTTE();
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
                .extract().response();

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 401, "Expecting HTTP Response error code 400, because Invalid api key");

    }
}
	
