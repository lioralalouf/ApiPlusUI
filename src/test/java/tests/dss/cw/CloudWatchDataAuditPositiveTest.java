package tests.dss.cw;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.MedicalDeviceGenerator;
import generators.MobileApplicationGenerator;
import generators.ProfileGenerator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.MedicalDevice;
import models.database.MobileApplication;
import models.database.Profile;
import models.request.ConnectRequest;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import repository.DataApiAuditCWRepository;
import tests.dss.api.DataApiRequestAuthorizationPositiveTest;
import tests.dss.api.PartnerApiTestBase;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class CloudWatchDataAuditPositiveTest extends PartnerApiTestBase {

    private final DataApiAuditCWRepository dataApiAuditCWRepository = new DataApiAuditCWRepository();

    @Test(priority = 0, testName = "Generate data to be used for proving DSS API calls are being audited")
    @Traceability( FS = {""})
    public void generate_data_for_test() throws IOException, ParseException {

        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard A new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        //generate api key
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        apiKeyRequest.ipAddresses[0] = "*";

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
        String apiKey = extractor.get("apiKey");

        extentTest.info("Create new user account");

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


        extentTest.info("Create new mobile device to the user");

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


        extentTest.info("Create digihaler medical device to the user");

        // Need Digihaler
        MedicalDevice medicalDevice = MedicalDeviceGenerator.getProAir();

        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);
        long actualDeviceSerial = registerMedicalDeviceRequest.inhaler.serialNumber;
        String actualAddedDate = registerMedicalDeviceRequest.inhaler.addedDate;

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(registerMedicalDeviceRequest)
                .when()
                .log().all()
                .post("/device/digihaler")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Provision user and partner to get state token and extract the decoded provision ID from it");
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

        extentTest.info("Insert the provision id in request body and connect the account of the user to the partner");
        ConnectRequest connectRequest = objectMapper.readValue(Utils.readRequest("data", "connect"),
                ConnectRequest.class);
        connectRequest.connection.provisionID = provisionID;

        given().log().all()
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

        extentTest.info("Get user account and verify the user's data transfer consent status to this partner is active in Http Response");
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

        extentTest.info("Get user's inhaler details by partner and verify the correct serial number, drug and added date returns in response");
        Response response2 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        DataApiRequestAuthorizationPositiveTest.InhalerResponse inhalerResponse = response2.getBody().as(DataApiRequestAuthorizationPositiveTest.InhalerResponse.class);
        long s = Long.parseLong(inhalerResponse.inhalers.get(0).serialNumber);
        TevaAssert.assertEquals(extentTest, actualDeviceSerial, s, "Response inhaler serial number is correct");
        TevaAssert.assertEquals(extentTest, actualAddedDate, inhalerResponse.inhalers.get(0).addedDate, "added date is correct");
        TevaAssert.assertEquals(extentTest, "AAA200", inhalerResponse.inhalers.get(0).drug, "added date is correct");
    }

    @Test(priority = 1, testName = "Fetch an audit entry for DSS from CloudWatch")
    @Traceability( FS = {""})
    public void fetch_cloudwatch_data() throws InterruptedException {
        long tenMinutesAgo = System.currentTimeMillis() - (10* 60 * 1000);
        long timeNow = System.currentTimeMillis();

        Thread.sleep(290_000);

        List<JSONObject> filteredLogEvents = dataApiAuditCWRepository.findLogsByTimeRange(tenMinutesAgo, timeNow);

        for (JSONObject filteredLogEvent : filteredLogEvents) {
            System.out.println(filteredLogEvent);
        }
    }
}
