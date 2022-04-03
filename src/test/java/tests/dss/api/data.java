package tests.dss.api;

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
import models.request.account.DependentCreationRequest;
import models.request.account.ProfileCreationRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class data extends PartnerApiTestBase {
    private String accessToken;
    private String stateToken;
    private Profile profile;
    private MedicalDevice medicalDevice;
    private MobileApplication mobileApplication;
    private MobileApplication mobileApplication2;
    private String apiKey;
    private String partnerID;
    private Profile dependent;
    private Profile guardian;

    @Test(priority = 1, testName = "test1", description = "")
    @Traceability(FS = {"x.x.x"})
    public void tc01_createNewPartner() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        accessToken = RestAssuredOAuth.getToken();

        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);

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

        this.guardian = ProfileGenerator.getProfile();
        ProfileCreationRequest profileCreationRequest = new ProfileCreationRequest(guardian);

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


        // Need Mobile App
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

        // Need Mobile App for dependent
        mobileApplication2 = MobileApplicationGenerator.getATTTE();
        registerMobileApplicationRequest = new RegisterMobileApplicationRequest(mobileApplication2);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent.getExternalEntityID())
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

        String dependentUUID = mobileApplication2.getUUID();

        //  System.out.println(profile.getExternalEntityID());

        // Need Digihaler
        medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);

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

        // Need Digihaler for dependent
        medicalDevice = MedicalDeviceGenerator.getProAir();
        registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);

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
        
    /*   List<Inhalation> inhalations = new ArrayList<>();

        for (int i = 0 ; i < 10; i++) {
            Inhalation inhalation = InhalationGenerator.generateGoodInhalation(medicalDevice);
            inhalations.add(inhalation);
            Inhalation inhalation2 = InhalationGenerator.generateFairInhalation(medicalDevice);
            inhalations.add(inhalation2);
            inhalation.event.peakFlow = 222;
           
        }

        UploadInhalationsRequest uploadInhalationsRequest = new UploadInhalationsRequest(inhalations);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", guardian.getExternalEntityID())
                .header("patient-external-entity-id", dependent.getExternalEntityID())
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
                .extract().response(); */


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


        //provision dependent
        Response response4 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "999")
                .post("/data/provision/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor3 = response4.jsonPath();
        this.stateToken = extractor3.get("stateToken");

        String provisionID2 = getProvisionID(stateToken);

        // String partner = jsonToken.get("partnerID").toString();

        //account connect guardian
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

        //account connect dependent
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


        //get inhalers
        Response response5 = given().log().all()
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

        //get inhalers
        Response response6 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .pathParam("patientID", "999")
                .get("data/inhaler/{patientID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        //revoke api key
      /*  given()
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
        .extract().response();*/


    }
}
