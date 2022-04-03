package tests.dss.api;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import models.request.account.DependentUpdateRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.aventstack.extentreports.ExtentTest;

import annotations.Traceability;
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
import models.request.account.DependentCreationRequest;
import models.request.account.ProfileCreationRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import tests.dss.api.GetUserInhalersByPartnerPositiveTest.InhalerResponse;
import utils.TevaAssert;
import utils.Utils;

@Listeners(TestListeners.class)
public class GetDependentDataE2ETest extends PartnerApiTestBase{
    private String accessToken;
    private String stateToken;
    private Profile profile;
    private Profile dependent;
    private Profile dependent2;
    private MedicalDevice medicalDevice;
    private MobileApplication mobileApplication;
    private MobileApplication mobileApplication2;
    private MobileApplication mobileApplication3;
    private String apiKey;
    private String partnerID;
    private Profile guardian;
    long min = 1000000000L;
    long max = 9999999999L ;   
    Random random = new Random();

    @Test(priority = 1, testName = "test1", description = "E2E test for pulling dependent's data by partner")
    @Traceability(FS = {"1635", "1656", "1698", "1704"})
    public void tc01_getDependentDataE2E() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        accessToken = RestAssuredOAuth.getToken();

        extentTest.info("Onboard A new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);

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

        this.guardian = ProfileGenerator.getProfile();
        ProfileCreationRequest profileCreationRequest = new ProfileCreationRequest(guardian);

        extentTest.info("Create A guardian profile");
        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("email", guardian.getEmail())
                .header("external-entity-id", guardian.getExternalEntityID())
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

        this.dependent = ProfileGenerator.getDependent();
        DependentCreationRequest dependentCreationRequest = new DependentCreationRequest(dependent);

        extentTest.info("Create dependent number 1 profile");
        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .request()
                .body(dependentCreationRequest)
                .when()
                .log().all()
                .post("/account/dependent")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();
            
        extentTest.info("Create dependent number 2 profile");
        this.dependent2 = ProfileGenerator.getDependent();
        DependentCreationRequest dependentCreationRequest2 = new DependentCreationRequest(dependent2);
        
        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .request()
                .body(dependentCreationRequest2)
                .when()
                .log().all()
                .post("/account/dependent")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();
 
        extentTest.info("Create mobile device for guardian");
        mobileApplication = MobileApplicationGenerator.getATTTE();
        RegisterMobileApplicationRequest registerMobileApplicationRequest = new RegisterMobileApplicationRequest(mobileApplication);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
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

        String guardianUUID = mobileApplication.getUUID();

        extentTest.info("Create mobile device for dependent number 1");
        mobileApplication2 = MobileApplicationGenerator.getATTTE();
        RegisterMobileApplicationRequest  registerMobileApplicationRequest2 = new RegisterMobileApplicationRequest(mobileApplication2);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent.getExternalEntityID())
                .request()
                .body(registerMobileApplicationRequest2)
                .when()
                .log().all()
                .post("/application/mobile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        String dependentUUID = mobileApplication2.getUUID();
        
        
        extentTest.info("Create mobile device for dependent number 2");
        mobileApplication3 = MobileApplicationGenerator.getATTTE();
        registerMobileApplicationRequest = new RegisterMobileApplicationRequest(mobileApplication3);
        
        given()
        .baseUri(Utils.readProperty("platformUrl"))
        .filter(new ConsoleReportFilter(extentTest))
        .header("Authorization", "Bearer " + accessToken)
        .header("Content-Type", "application/json")
        .header("external-entity-id", guardian.getExternalEntityID())
        .header("patient-external-entity-id", dependent2.getExternalEntityID())
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

        String dependentUUID2 = mobileApplication3.getUUID();

        extentTest.info("Create medical device for the guardian");
        medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);
        long randomNum =  random.nextLong() % (max - min) + max;
        registerMedicalDeviceRequest.inhaler.serialNumber = randomNum;
        long guardianSerielNumber = registerMedicalDeviceRequest.inhaler.serialNumber;
        
        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("device-uuid", guardianUUID)
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
    
        extentTest.info("Create medical device for dependent number 1, Create unique serial number for this device");
        randomNum =  random.nextLong() % (max - min) + max;
        medicalDevice = MedicalDeviceGenerator.getProAir();
        registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);
        registerMedicalDeviceRequest.inhaler.serialNumber = randomNum;
        long dependentSerielNumber = registerMedicalDeviceRequest.inhaler.serialNumber;

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent.getExternalEntityID())
                .header("device-uuid", dependentUUID)
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
        
        extentTest.info("Create medical device for dependent number 2, Create unique serial number for this device");
        randomNum =  random.nextLong() % (max - min) + max;
        medicalDevice = MedicalDeviceGenerator.getProAir();
        registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);
        registerMedicalDeviceRequest.inhaler.serialNumber = randomNum;
        long dependent2SerielNumber = registerMedicalDeviceRequest.inhaler.serialNumber;

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent2.getExternalEntityID())
                .header("device-uuid", dependentUUID2)
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

        extentTest.info("Provision guardian and partner to get state token and extract the decoded provision ID from it");
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


        extentTest.info("Provision dependent number 1 and partner to get state token and extract the decoded provision ID from it");
        Response response4 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "111")
                .post("/data/provision/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor3 = response4.jsonPath();
        this.stateToken = extractor3.get("stateToken");
        String provisionID2 = getProvisionID(stateToken);

        extentTest.info("Insert the provision id in request body and connect the account of the guardian to the partner");
        ConnectRequest connectRequest = objectMapper.readValue(Utils.readRequest("data", "connect"),
                ConnectRequest.class);
        connectRequest.connection.provisionID = provisionID;

        given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", guardian.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .body(connectRequest)
                .when().log().all()
                .post("/account/connect")
                .then()
                .extract().response();

        extentTest.info("Insert the provision id in request body and connect the account of dependent number 1 to the partner");
        connectRequest = objectMapper.readValue(Utils.readRequest("data", "connect"),
                ConnectRequest.class);
        connectRequest.connection.provisionID = provisionID2;

        given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .body(connectRequest)
                .when().log().all()
                .post("/account/connect")
                .then()
                .extract().response();

        extentTest.info("Get dependent 1 inhaler details by partner and verify the correct serial number, drug and added date returns in response");
        Response response7 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "111")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        InhalerResponse inhalerResponse = response7.getBody().as(InhalerResponse.class);
        long s = Long.parseLong(inhalerResponse.inhalers.get(0).serialNumber);
        TevaAssert.assertEquals(extentTest, dependentSerielNumber, s, "Response inhaler serial number is correct");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).addedDate, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).lastConnectionDate, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).deviceStatus, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).addedDate, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).drug, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).brandName, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse.inhalers.get(0).strength, "");

        extentTest.info("Provision dependent number 2 and partner to get state token and extract the decoded provision ID from it");
        Response response8 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "222")
                .post("/data/provision/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor4 = response8.jsonPath();
        this.stateToken = extractor4.get("stateToken");
        String provisionID3 = getProvisionID(stateToken);
        
        extentTest.info("Insert the provision id in request body and connect the account of dependent number 2 to the partner");
        connectRequest = objectMapper.readValue(Utils.readRequest("data", "connect"),
                ConnectRequest.class);
        connectRequest.connection.provisionID = provisionID3;

        given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Content-Type", "application/json")
                .header("External-Entity-Id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent2.getExternalEntityID())
                .header("Authorization", "Bearer " + accessToken)
                .body(connectRequest)
                .when().log().all()
                .post("/account/connect")
                .then()
                .extract().response();
   
        extentTest.info("Get dependent 2 inhaler details by partner and verify the correct serial number, drug and added date returns in response");
        Response response9 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "222")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        InhalerResponse inhalerResponse2 = response9.getBody().as(InhalerResponse.class);
        long s2 = Long.parseLong(inhalerResponse2.inhalers.get(0).serialNumber);
        TevaAssert.assertEquals(extentTest, dependent2SerielNumber, s2, "Response inhaler serial number is correct");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).addedDate, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).lastConnectionDate, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).deviceStatus, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).addedDate, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).drug, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).brandName, "");
        TevaAssert.assertNotNull(extentTest, inhalerResponse2.inhalers.get(0).strength, "");

        extentTest.info("Get user account and verify dependent 2 exists under guardian before setting age of majority");
        Response resGetAccount = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .request()
                .when()
                .log().all()
                .get("/account")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();
        
        JsonPath js = resGetAccount.jsonPath();
        
        List<LinkedHashMap<String, String>> allDependets = js.getList("account.dependents");
        for (LinkedHashMap<String, String> dependent : allDependets) {
            if (dependent.get("externalEntityID").equalsIgnoreCase(dependent2.getExternalEntityID())) {
            	TevaAssert.assertEquals(extentTest, dependent.get("externalEntityID"), dependent.get("externalEntityID"), "dependent number 2 exists");

			}
		}
        
        extentTest.info("Set age of majority for dependent2 for today");
        LocalDate today = LocalDate.now(ZoneId.of("GMT"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String y = today.format(formatter);
        dependent2.setAgeOfMajority(y);

        DependentUpdateRequest dependentUpdateRequest = new DependentUpdateRequest(dependent2);
        dependentUpdateRequest.patient.ageOfMajority = y;
        
        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent2.getExternalEntityID())
                .request()
                .body(dependentUpdateRequest)
                .when()
                .log().all()
                .put("/account/profile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        Thread.sleep(3*60*1000);
        
        extentTest.info("Get user account and verify dependent has been removed from the account after setting age of majority");
        Response resGetAccount2 = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .request()
                .when()
                .log().all()
                .get("/account")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();
        
        JsonPath js2 = resGetAccount2.jsonPath();
        
        List<LinkedHashMap<String, String>> allDependets2 = js2.getList("account.dependents");
        for (LinkedHashMap<String, String> dependent : allDependets2) {
            if (!dependent.get("externalEntityID").equalsIgnoreCase(dependent2.getExternalEntityID())) {
            	TevaAssert.assertFalse(extentTest, dependent.get("externalEntityID").equalsIgnoreCase(dependent2.getExternalEntityID()), "dependent number 2 doesnt exist in account after reset age of majority");
			}
		}

        extentTest.info("Get dependent number 2 data by partner, Expecting HTTP Response error '404' because parnter and dependent connection doesnt exist anymore");
        Response response10 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "222")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .extract().response();
        
        TevaAssert.assertEquals(extentTest, response10.getStatusCode(), 400, "Expecting HTTP Response error '404' because parnter and dependent connection doesnt exist anymore");

	}
}
