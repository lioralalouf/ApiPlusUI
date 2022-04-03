package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.InhalationGenerator;
import generators.MedicalDeviceGenerator;
import generators.MobileApplicationGenerator;
import generators.ProfileGenerator;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.Inhalation;
import models.database.MedicalDevice;
import models.database.MobileApplication;
import models.database.Profile;
import models.request.*;
import models.request.account.ProfileCreationRequest;
import models.request.inhalation.UploadInhalationsRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.parser.ParseException;
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
public class PartnerConfigurationLimitationPositiveTest extends PartnerApiTestBase {

    private String stateToken;
    private Profile profile;
    private MedicalDevice medicalDevice;
    private MobileApplication mobileApplication;
    private String apiKey;
    private String partnerID;
    private String organizationID;

    @Test(priority = 1, testName = "Send 2 requests to get inhalations prove there's a limit will stop more than 1 request")
    @Traceability(FS = {"x.x.x"})
    public void tc01_createNewPartner() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
       
        extentTest.info("Onboard A new partner, rate=1000, burst=100, limit=1, period=day");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.throttle.rate = 1000;
        partnerRequest.throttle.burst = 100;
        partnerRequest.quota.limit = 1;
        partnerRequest.quota.period = "day";
        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Create A new organization");
        OnboardOrganizationRequest organizationRequest = objectMapper.readValue(Utils.readRequest("organization", "onboardOrganization"),
                OnboardOrganizationRequest.class);
        organizationRequest.mnemonic = UUID.randomUUID().toString();

        Response responseOrganization = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .request()
                .body(organizationRequest)
                .when()
                .log().all()
                .post("/configuration/organizations")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath js = responseOrganization.jsonPath();
        this.organizationID = js.get("organizationID");

        extentTest.info("Create A new program");
        OnboardProgramRequest programRequest = objectMapper.readValue(Utils.readRequest("program", "onboardProgram"),
                OnboardProgramRequest.class);
        programRequest.organizationID = organizationID;
        programRequest.programName = UUID.randomUUID().toString();

        given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .request()
                .body(programRequest)
                .when()
                .log().all()
                .post("/configuration/programs")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Generate A new api key for onboarded partner");
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
        
        extentTest.info("Create A new user account");
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

        extentTest.info("Create A new mobile device for the user");
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

        extentTest.info("Create A new digihaler medical device for the user");
        medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);

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

        extentTest.info("Upload inhalations by the user");
        List<Inhalation> inhalations = new ArrayList<>();

        for (int i = 0; i < 1; i++) {
            Inhalation inhalation = InhalationGenerator.generateGoodInhalation(medicalDevice);
            inhalation.event.id = 1;
            inhalations.add(inhalation);
            Inhalation inhalation2 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalation2.event.id = 2;
            inhalations.add(inhalation2);
            Inhalation inhalation3 = InhalationGenerator.generateGoodInhalation(medicalDevice);
            inhalation3.event.id = 3;
            inhalations.add(inhalation3);
            Inhalation inhalation4 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalation4.event.id = 4;
            inhalations.add(inhalation4);


        }

        UploadInhalationsRequest uploadInhalationsRequest = new UploadInhalationsRequest(inhalations);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(uploadInhalationsRequest)
                .when()
                .log().all()
                .post(" /medication/administration/inhalations")
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
        this.stateToken = extractor2.get("stateToken");

        String provisionID = getProvisionID(stateToken);
        String partner = getPartnerID(stateToken);
        System.out.println("provisio id is - " + provisionID);
        System.out.println("PARTNER id is - " + partner);

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

            extentTest.info("Get inhalations data by partner for the first timetoday, Expecting successfull HTTP Response code '200'");
            Response response4 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/inhalation/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();
  
            extentTest.info("Get inhalations data by partner for the second time today, Expecting HTTP Response errore code 429, because Setting request limit to 1 shall result in a 429 for more than 1 requests");
            Response response5 = given().log().all()
                    .filter(new ConsoleReportFilter(extentTest))
                    .baseUri(Utils.readProperty("platformUrl"))
                    .header("X-API-Key", apiKey)
                    .pathParam("patientID", "123456")
                    .get("data/inhalation/{patientID}")
                    .then()
                    .log().all()
                    .extract().response();
            
            TevaAssert.assertEquals(extentTest, response5.getStatusCode(), 429, "Expecting HTTP Response errore code 429, because Setting request limit to 1 shall result in a 429 for more than 1 requests");   
        }


    @Test(priority = 2, testName = "Send hundreds of requests to get inhalations in a loop prove that throttle will evantually reject the requests")
    @Traceability(FS = {"x.x.x"})
    public void tc02_createNewPartner() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard A new partner, rat=1, burst=1, limit=10000, period=day");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.throttle.rate = 1;
        partnerRequest.throttle.burst = 1;
        partnerRequest.quota.limit = 10000;
        partnerRequest.quota.period = "day";
        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Create A new organization");
        OnboardOrganizationRequest organizationRequest = objectMapper.readValue(Utils.readRequest("organization", "onboardOrganization"),
                OnboardOrganizationRequest.class);
        organizationRequest.mnemonic = UUID.randomUUID().toString();

        Response responseOrganization = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .request()
                .body(organizationRequest)
                .when()
                .log().all()
                .post("/configuration/organizations")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath js = responseOrganization.jsonPath();
        this.organizationID = js.get("organizationID");

        extentTest.info("Create A new program");
        OnboardProgramRequest programRequest = objectMapper.readValue(Utils.readRequest("program", "onboardProgram"),
                OnboardProgramRequest.class);
        programRequest.organizationID = organizationID;
        programRequest.programName = UUID.randomUUID().toString();

        given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .request()
                .body(programRequest)
                .when()
                .log().all()
                .post("/configuration/programs")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Generate A new api key for onboarded partner");
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

        extentTest.info("Create A new user account");
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

        extentTest.info("Create A new mobile device for the user");
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

        System.out.println(profile.getExternalEntityID());

        extentTest.info("Create A new digihaler medical device for the user");
        medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);

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

        extentTest.info("Upload inhalations by the user");
        List<Inhalation> inhalations = new ArrayList<>();
        for (int i = 0; i < 1; i++) {
            Inhalation inhalation = InhalationGenerator.generateGoodInhalation(medicalDevice);
            inhalation.event.id = 1;
            inhalations.add(inhalation);
            Inhalation inhalation2 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalation2.event.id = 2;
            inhalations.add(inhalation2);
            Inhalation inhalation3 = InhalationGenerator.generateGoodInhalation(medicalDevice);
            inhalation.event.id = 3;
            inhalations.add(inhalation3);
            Inhalation inhalation4 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalation2.event.id = 4;
            inhalations.add(inhalation4);


        }

        UploadInhalationsRequest uploadInhalationsRequest = new UploadInhalationsRequest(inhalations);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(uploadInhalationsRequest)
                .when()
                .log().all()
                .post(" /medication/administration/inhalations")
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
        this.stateToken = extractor2.get("stateToken");

        String provisionID = getProvisionID(stateToken);
        String partner = getPartnerID(stateToken);
        System.out.println("provisio id is - " + provisionID);
        System.out.println("PARTNER id is - " + partner);

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

        boolean encounteredError = false;

        extentTest.info("Send 500 request for pulling the user's inhalations data");
        for (int i = 0; i < 500; i++) {
            Response response2 = given().log().all()
                    .filter(new ConsoleReportFilter(extentTest))
                    .baseUri(Utils.readProperty("platformUrl"))
                    .header("X-API-Key", apiKey)
                    .pathParam("patientID", "123456")
                    .get("data/inhalation/{patientID}")
                    .then()
                    .log().all()
                    .assertThat()
                    .extract().response();

            if (response2.getStatusCode() == 429) {
                TevaAssert.assertEquals(extentTest,response2.getStatusCode(), 429, "Setting burst too low shall result in a 429.");
                encounteredError = true;
                break;
            }
        }

        TevaAssert.assertTrue(extentTest, encounteredError, "Encountered burst limit at least one time");
    }
}
