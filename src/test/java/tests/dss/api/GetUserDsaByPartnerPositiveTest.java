package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.*;
import models.request.ConnectRequest;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import models.request.inhalation.UploadInhalationsRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import models.request.questionnaire.DailySelfAssessmentDto;
import models.request.questionnaire.UploadDsasRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import repository.PartnerUserConnectionRepository;
import utils.IpAddressUtils;
import utils.LambdaUtils;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class GetUserDsaByPartnerPositiveTest extends PartnerApiTestBase {
    private String stateToken;
    private Profile profile;
    private MedicalDevice medicalDevice;
    private MobileApplication mobileApplication;
    private String apiKey;
    private String partnerID;
    private static final IpAddressUtils ipa = new IpAddressUtils();
    double dVolume;

    public static class InhalerResponse {

        public List<Inhalers> inhalers;
        public Patients patient;

        public static class Patients {
            public String patient;
            public String consentStartDate;
        }

        public static class Inhalers {
            public String serialNumber;
            public long Inhalers;
            public String lastConnectionDate;
            public int deviceStatus;
            public String addedDate;
            public String drug;
            public String brandName;
            public String strength;
        }
    }

    public static class InhalationResponse {

        public List<Inhalation> inhalations;
        public Patients patient;

        public static class Patients {
            public String consentStartDate;
        }

        public static class Inhalation {
            public String id;
            public String time;
            public String category;
            public String drug;
            public int volume;
            public int peakFlow;
        }
    }


    @Test(priority = 1, testName = "Get user's dsa data by partner")
    @Traceability(FS = {"1657"})
    public void tc01_getDsa() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        Map<String, String> environment = LambdaUtils.getLambdaConfiguration(Utils.readProperty("getPatientInhalations"));

        extentTest.info("Set volumeTransferEnabled and peakFlowTransferEnabled As true");
        environment.put("volumeTransferEnabled", "true");
        environment.put("peakFlowTransferEnabled", "true");

        LambdaUtils.updateLambdaConfiguration(Utils.readProperty("getPatientInhalations"), environment);

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate new api key with valid read data scope");

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
        String apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        
        extentTest.info("Create new user account");
        profile = ProfileGenerator.getProfile();
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
        mobileApplication = MobileApplicationGenerator.getATTTE();

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

        String userUUID = mobileApplication.getUUID();


        extentTest.info("Create digihaler medical device to the user");
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println("Timestamp is " + ts);

        MedicalDevice medicalDevice = MedicalDeviceGenerator.getProAir();

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

        extentTest.info("Upload inhalations for the user");
        List<Inhalation> inhalations = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Inhalation inhalation = InhalationGenerator.generateGoodInhalation(medicalDevice);
            Inhalation inhalation2 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalations.add(inhalation);
            inhalations.add(inhalation2);
            inhalation.event.id = 1;
            inhalation.event.peakFlow = 90;
            inhalation.event.volume = 5;
            inhalation2.event.id = 2;
            inhalation2.event.peakFlow = 90;
            inhalation2.event.volume = 5;

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

        extentTest.info("Upload DSA's for the user");
        List<DailySelfAssessment> dsaList = new ArrayList<>();

        DailySelfAssessment dsa = DsaGenerator.getDsa();
        DailySelfAssessment dsa2 = DsaGenerator.getDsa();
        DailySelfAssessment dsa3 = DsaGenerator.getDsa();
        dsaList.add(dsa);
        dsaList.add(dsa2);
        dsaList.add(dsa3);
        dsa.date = "2020-01-01";
        dsa.assessment = 1;
        dsa2.date = "2021-01-01";
        dsa2.assessment = 2;
        dsa3.date = "2022-01-01";
        dsa3.assessment = 3;


        UploadDsasRequest uploadDsaRequest = new UploadDsasRequest(dsaList);

        given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", profile.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .header("device-uuid", userUUID)
                .body(uploadDsaRequest)
                .when().log().all()
                .post("/questionnaire/dsas")
                .then()
                .log()
                .all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Get user's  dsa by partner and verify the date and assessments are correct");
        Response response4 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/dsa/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor3 = response4.jsonPath();
        List<String> date = extractor3.getList("dsas.date");
        List<String> assessment = extractor3.getList("dsas.assessment");
        TevaAssert.assertEquals(extentTest, date.get(0), dsa.date, "Date for dsa 1 is correct");
        TevaAssert.assertEquals(extentTest, date.get(1), dsa2.date, "Date for dsa 2 is correct");
        TevaAssert.assertEquals(extentTest, date.get(2), dsa3.date, "Date for dsa 3 is correct");
        TevaAssert.assertEquals(extentTest, assessment.get(0), dsa.assessment, "Assessment for dsa 1 is correct");
        TevaAssert.assertEquals(extentTest, assessment.get(1), dsa2.assessment, "Assessment for dsa 2 is correct");
        TevaAssert.assertEquals(extentTest, assessment.get(2), dsa3.assessment, "Assessment for dsa 3 is correct");

    }


    @Test(priority = 2, testName = "Get user's dsa's data by partner, after updating dsa's")
    @Traceability(FS = {"1657"})
    public void tc02_getDsaAfterUpdateData() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        Map<String, String> environment = LambdaUtils.getLambdaConfiguration(Utils.readProperty("getPatientInhalations"));

        extentTest.info("Set volumeTransferEnabled and peakFlowTransferEnabled As true");
        environment.put("volumeTransferEnabled", "true");
        environment.put("peakFlowTransferEnabled", "true");

        LambdaUtils.updateLambdaConfiguration(Utils.readProperty("getPatientInhalations"), environment);

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Generate new api key with valid read data scope");

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
        String apiKey = extractor.get("apiKey");
        registerApiKey(partnerID, apiKey);
        
        extentTest.info("Create new user account");
        profile = ProfileGenerator.getProfile();
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
        mobileApplication = MobileApplicationGenerator.getATTTE();

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

        String userUUID = mobileApplication.getUUID();


        extentTest.info("Create digihaler medical device to the user");
        // medicalDevice = MedicalDeviceGenerator.getProAir();

        // Need Digihaler
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        System.out.println("Timestamp is " + ts);

        MedicalDevice medicalDevice = MedicalDeviceGenerator.getProAir();

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

        extentTest.info("Upload inhalations for the user");
        List<Inhalation> inhalations = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            Inhalation inhalation = InhalationGenerator.generateGoodInhalation(medicalDevice);
            Inhalation inhalation2 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalations.add(inhalation);
            inhalations.add(inhalation2);
            inhalation.event.id = 1;
            inhalation.event.peakFlow = 90;
            inhalation.event.volume = 5;
            inhalation2.event.id = 2;
            inhalation2.event.peakFlow = 90;
            inhalation2.event.volume = 5;

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

        extentTest.info("Set Consent Start Date for A later Date than dsas creation");
        PartnerUserConnectionRepository partnerUserConnectionRepository = new PartnerUserConnectionRepository();
        PartnerUserConnection partnerUserConnection = partnerUserConnectionRepository.findConsentByPatientPartner(profile.getExternalEntityID(), partnerID);

        partnerUserConnection.setConsentStartDate("2022-03-01T00:00:00");

        partnerUserConnectionRepository.updatePatientPartnerConsent(partnerUserConnection);

        List<DailySelfAssessment> dsaList = new ArrayList<>();

        DailySelfAssessment dsa = DsaGenerator.getDsa();
        DailySelfAssessment dsa2 = DsaGenerator.getDsa();
        DailySelfAssessment dsa3 = DsaGenerator.getDsa();
        dsaList.add(dsa);
        dsaList.add(dsa2);
        dsaList.add(dsa3);
        dsa.date = "2020-01-01";
        dsa.assessment = 1;
        dsa2.date = "2021-01-01";
        dsa2.assessment = 2;
        dsa3.date = "2022-01-01";
        dsa3.assessment = 3;

        UploadDsasRequest uploadDsaRequest = new UploadDsasRequest(dsaList);

        extentTest.info("Upload DSA for the user");
        given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", profile.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .header("device-uuid", userUUID)
                .body(uploadDsaRequest)
                .when().log().all()
                .post("/questionnaire/dsas")
                .then()
                .log()
                .all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Get user's  dsa by partner, Expecting HTTP Response error code 404, because No DSAs found after consent date");
        Response response4 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/dsa/{patientID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 404, "Expecting HTTP Response error code 404, because No DSAs found that have been created after consent date");

        DailySelfAssessmentDto d = new DailySelfAssessmentDto();
        d.timestamp = "2021-01-01T00:00:00+05:00";
        DailySelfAssessment dsa4 = DsaGenerator.getDsa();

        List<DailySelfAssessment> dsaList2 = new ArrayList<>();

        dsaList2.add(dsa4);
        dsa4.date = "2022-10-10";
        dsa4.assessment = 3;

        UploadDsasRequest uploadDsaRequest2 = new UploadDsasRequest(dsaList2);

        extentTest.info("Modify the DSA for the user, Change Assessment in 2020-01-01 from 1 to 3");

        given()
                .log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", profile.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .header("device-uuid", userUUID)
                .body(uploadDsaRequest2)
                .when().log().all()
                .post("/questionnaire/dsas")
                .then()
                .log()
                .all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        extentTest.info("Get user's dsa by partner again, Expecting after modify dsa assessment for dsa equals to 3");
        Response response5 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "123456")
                .get("data/dsa/{patientID}")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor1 = response5.jsonPath();
        List<String> date = extractor1.getList("dsas.date");
        List<String> assessment = extractor1.getList("dsas.assessment");

        TevaAssert.assertEquals(extentTest, date.get(0), dsa4.date, "Date for dsa 1 is correct");
        TevaAssert.assertEquals(extentTest, assessment.get(0), dsa4.assessment, "assessment for dsa equals to 3");
    }
}
