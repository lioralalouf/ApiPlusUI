package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.MedicalDeviceGenerator;
import generators.MobileApplicationGenerator;
import generators.ProfileGenerator;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.MedicalDevice;
import models.database.MobileApplication;
import models.database.PartnerUserConnection;
import models.database.Profile;
import models.request.ConnectRequest;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import repository.PartnerUserConnectionRepository;
import tests.dss.api.OnboardPartnerConsentPositiveTest.ConsentsResponse;
import utils.IpAddressUtils;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class DataApiRequestAuthorizationNegativeTest extends PartnerApiTestBase {

    private String stateToken;
    private Profile profile;
    private MobileApplication mobileApplication;
    private String apiKey;
    private String partnerID;
    private MedicalDevice medicalDevice;

    private static final IpAddressUtils ipa = new IpAddressUtils();

    public static class InhalerResponse {

        public List<Inhalers> inhalers;
        public Patients patient;

        public static class Patients {
            //public String patient;
            public String consentStartDate;
        }

        public static class Inhalers {
            public String serialNumber;
            //public long Inhalers;
            public String lastConnectionDate;
            public int deviceStatus;
            public String addedDate;
            public String drug;
            public String brandName;
            public String strength;
        }
    }
    
    @Test(priority = 1, testName = "Get data when data start date is earlier than the consent date")
    @Traceability(FS = {"1634"})
    public void tc01_getDataStartDate() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate a new Api key for onboarded partner");
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
                .assertThat()
                .statusCode(200)
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

        extentTest.info("Create new mobile device for the user");
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

        extentTest.info("Create new digihaler medical device for the user");
        // Need Digihaler
        medicalDevice = MedicalDeviceGenerator.getProAir();
        medicalDevice.setAddedDate("2017-12-01T00:00:00+04:00");

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
        this.stateToken = extractor2.get("stateToken");

        String provisionID = getProvisionID(stateToken);

        extentTest.info("Insert the provision id in request body and connect the account of the user to the partner");
        //account connect
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

        extentTest.info("Set Consent Start Date for A later Date than inhaler creation");
        PartnerUserConnectionRepository partnerUserConnectionRepository = new PartnerUserConnectionRepository();
        PartnerUserConnection partnerUserConnection = partnerUserConnectionRepository.findConsentByPatientPartner(profile.getExternalEntityID(), partnerID);

        partnerUserConnection.setConsentStartDate("2022-03-01T00:00:00");

        partnerUserConnectionRepository.updatePatientPartnerConsent(partnerUserConnection);

        JsonPath js2 = resGetAccount.jsonPath();
        List<LinkedHashMap<String, String>> allConsents = js2.getList("account.consents");
        for (LinkedHashMap<String, String> consent : allConsents) {
            if (consent.get("consentType").equalsIgnoreCase("dataTransferConsent")) {
                if (consent.get("partnerID").equalsIgnoreCase(partnerID)) {
                    TevaAssert.assertEquals(extentTest, consent.get("status"), "Active", "Data transfer consent status is active");
                }
            }
        }

        extentTest.info("Get user's inhaler details by partner, Expecting error because inhaler added date is earlier than data transfer consent date");
        //get inhaler
        Response response2 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .queryParam("startDate", "2020-01-01T06:30:00Z")
                .pathParam("patientID", "123456")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 416, "Expecting HTTP Response error code 416, because requested start date is prior to consent date");

    }

    @Test(priority = 2, testName = "Get partner local authorizations by patient after revoking connection")
    @Traceability(FS = {"1634"})
    public void tc02_getInhalerAfterRevoke() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .request()
                .body(partnerRequest).when()
                .log().all()
                .post("/configuration/partners")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        JsonPath js = response.jsonPath();
        this.partnerID = js.getString("partnerID");

        extentTest.info("Generate new api key for onboarded partner");
        //generate api key
        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        Response response2 = given().log().all()
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
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");

        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 200, "Api key has bben generated successfully - HTTP Response Code `200`");

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

        extentTest.info("Create new mobile device for the user");
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

        extentTest.info("Create new Digihaler medical device for the user");
        // Need Digihaler
        medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);
        long actualDeviceSerial = registerMedicalDeviceRequest.inhaler.serialNumber;

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
        this.stateToken = extractor2.get("stateToken");

        String provisionID = getProvisionID(stateToken);
        String partner = getPartnerID(stateToken);

        extentTest.info("Insert the provision id in request body and connect the account of the user to the partner");
        //account connect
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

        extentTest.info("Get user's inhaler details by partner and verify the correct serial number returns in response");
        //get inhaler
        Response response4 = given().log().all()
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

        InhalerResponse inhalerResponse = response4.getBody().as(InhalerResponse.class);
        long s = Long.parseLong(inhalerResponse.inhalers.get(0).serialNumber);
        TevaAssert.assertEquals(extentTest, actualDeviceSerial, s, "Response inhaler serial number is correct");

        extentTest.info("Revoke partner and user connection");
        //revoke connection
        Response response5 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .when().log().all()
                .delete("/data/connection/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Get user account and verify the user's data transfer consent status to this partner is Withdrawn in Http Response");
        //Get account after revoke connection
        Response resGetAccount2 = given().log().all()
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


        JsonPath js3 = resGetAccount2.jsonPath();
        List<LinkedHashMap<String, String>> allConsents2 = js3.getList("account.consents");

        for (LinkedHashMap<String, String> consent : allConsents2) {
            if (consent.get("consentType").equalsIgnoreCase("dataTransferConsent")) {
                if (consent.get("partnerID").equalsIgnoreCase(partnerID)) {
                    TevaAssert.assertEquals(extentTest, consent.get("status"), "Withdrawn", "Data transfer consent status is Withdrawn");

                }

            }
        }

        extentTest.info("Get user inhaler details after revoking connection, Expecting Error code '400' because connection between patient and partner is not active");
        //get inhaler after revoke connection
        Response response6 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response6.getStatusCode(), 400, "Error code '400' because connection between patient and partner is not active");

    }

    @Test(priority = 3, testName = "Get partner local authorizations by patient for partner with no legal consent")
    @Traceability(FS = {"1634"})
    public void tc03_getLocalAuthorizationNoLegal() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        this.partnerID = createPartner(extentTest, partnerRequest);
        this.apiKey = generateApiKey(extentTest, partnerID);

        extentTest.info("Onboard all consent documents for the partner, all documents are version 1.0");
        //upload privacy notice for the partner
        File pnTxt = new File("./documents/privacyNotice.txt");
        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", pnTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPn01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "Privacy Notice file uploaded successfully");

        //upload terms and conditions for the partner
        File termsTxt = new File("./documents/termsAndConditions.txt");
        Response responseFileB = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", termsTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardtou01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "Terms Of Use file uploaded successfully");

        //upload hipaa for the partner
        File hippaTxt = new File("./documents/hipaa.txt");
        Response responseFileC = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", hippaTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardHipaa01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileC.getStatusCode(), "Hipaa consent file uploaded successfully");

        //upload marketing for the partner
        File marketingTxt = new File("./documents/marketing.txt");
        Response responseFileD = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", marketingTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardMarketing01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileD.getStatusCode(), "Marketing Consent file uploaded successfully");

        //upload signature and conditions for the partner
        File signatureTxt = new File("./documents/signature.txt");
        Response responseFileE = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardSignature01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileE.getStatusCode(), "Signature Policy file uploaded successfully");

        File pacTxt = new File("./documents/pac.txt");
        Response responseFileF = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", pacTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPac01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileF.getStatusCode(), "Pac file uploaded successfully");

        extentTest.info("Get partner consents, Expecting all consents to be version 1.0");
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl"))
                .then()
                .log().all()
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);

        List<String> list1 = new ArrayList<>();
        list1.add("hipaa");
        list1.add("marketing");
        list1.add("pac");
        list1.add("pn");
        list1.add("signature");
        list1.add("tou");


        // TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).version, "1.0", "Version 1 documenents are displayed");
        // TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).version, "2.0", "Version 2 documenents are displayed");
        // TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).locale, "en-US", "en-US documenents are displayed");
        // TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).locales.get(0).locale, "es-US", "es-US documenents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Documents for version 1.0 are displayed");

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

        extentTest.info("Create new mobile device for the user");
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

        extentTest.info("Create new digihaler medical device for the user");
        // Need Digihaler
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
        this.stateToken = extractor2.get("stateToken");


        String provisionID = getProvisionID(stateToken);
        String partner = getPartnerID(stateToken);
        System.out.println("provisio id is - " + provisionID);
        System.out.println("PARTNER id is - " + partner);

        extentTest.info("Insert the provision id in request body and connect the account of the user to the partner");
        //account connect
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


        extentTest.info("Update partner's activeSignature from version 1.0 to version 2.0");
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.activeSignature = "2.0";

        given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .pathParam("partnerID", partnerID)
                .request()
                .body(partnerRequest)
                .when().log().all()
                .put("/{partnerID}")
                .then().log().all()
                .assertThat().statusCode(200)
                .extract().response().asString();


        extentTest.info("Get user's inhaler details by partner, Expecting error '451' because partner's has mismatch in legal consents for activeSignature");
        //get inhaler
        Response response4 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 451, "Expecting HTTP Response error code 451, because of Unavailable for legal reasons");
    }

    @Test(priority = 4, testName = "Get partner local authorizations by patient for partner with no active api key")
    @Traceability(FS = {"1634"})
    public void tc04_getLocalAuthorizationNoActiveKey() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Revoke the api key of the onboarded partner");
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response3 = given()
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

        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "Api Key have been revokes successfully");

        extentTest.info("Get the api key data by the partner, Expecting Http response error code 401, because the Api Key Doesnt Exist");
        Response response4 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", "apiKey")
                .when()
                .get("/data/api/key")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 401, "Http response error code 401, because the Api Key Doesnt Exist");
    }

    @Test(priority = 5, testName = "Get api key details for no whitelisted ip address")
    @Traceability(FS = {"1634"})
    public void tc05_getInvalidIpAddressApiKeyDetails() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);
        apiKeyRequest.ipAddresses[0] = "7.125.34.140";

        extentTest.info("Generate a new api key with different ip from my local ip, ip is 7.125.34.140");
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

        extentTest.info("Get the api key details, HTTP Response expected error code is '401', because our IP Address is not whitelisted");
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
    }

}
