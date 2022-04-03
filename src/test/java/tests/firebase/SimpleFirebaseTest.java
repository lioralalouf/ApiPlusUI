package tests.firebase;

import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.TestListeners;
import generators.MedicalDeviceGenerator;
import generators.MobileApplicationGenerator;
import generators.ProfileGenerator;
import models.database.MedicalDevice;
import models.database.MobileApplication;
import models.database.Profile;
import models.request.account.ProfileCreationRequest;
import models.request.account.UpdateProfileRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import requests.RestAssuredOAuth;
import utils.Utils;

import java.io.IOException;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class SimpleFirebaseTest extends FirebaseTest{

    private String accessToken;
    private Profile patient;
    private MobileApplication mobileApplication;
    private MedicalDevice digihaler;
    private ProfileCreationRequest profileCreationRequest;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeTest
    public void setup() {
        this.patient = ProfileGenerator.getProfile();
        this.mobileApplication = MobileApplicationGenerator.getDigihaler();
        this.digihaler = MedicalDeviceGenerator.getProAir();
    }


    @Test(priority = 1, testName = "Register Profile", description = "Creates a new profile")
    public void tc01_register_patient() throws InterruptedException, IOException {
        accessToken = RestAssuredOAuth.getToken();

        profileCreationRequest = objectMapper.readValue(Utils.readRequest("account", "newProfile"), ProfileCreationRequest.class);
        profileCreationRequest.patient.firstName = "Test";
        profileCreationRequest.patient.lastName = patient.getLastName();
        profileCreationRequest.patient.dateOfBirth = patient.getDateOfBirth();

        String responseFile = given()
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .header("external-entity-id", patient.getExternalEntityID())
                .header("email", patient.getEmail())
                .header("Content-Type", "application/json")
                .request()
                .body(profileCreationRequest)
                .when()
                .log().all()
                .post("/account/profile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();

    }

    @Test(priority = 2, testName = "Register Mobile Application", description = "Register new mobile application to user")
    public void tc01_register_mobile_device() throws InterruptedException, IOException {
        accessToken = RestAssuredOAuth.getToken();

        RegisterMobileApplicationRequest registerMobileApplicationRequest = objectMapper.readValue(Utils.readRequest("mobileDevice", "registerMobileDevice"), RegisterMobileApplicationRequest.class);
;
        registerMobileApplicationRequest.mobileDevice.appName = mobileApplication.getAppName();
        registerMobileApplicationRequest.mobileDevice.appVersionNumber = mobileApplication.getAppVersionNumber();
        registerMobileApplicationRequest.mobileDevice.operatingSystem = mobileApplication.getOperatingSystem();
        registerMobileApplicationRequest.mobileDevice.firebaseToken = firebaseToken;
        registerMobileApplicationRequest.mobileDevice.UUID = mobileApplication.getUUID();

        String responseFile = given()
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .header("external-entity-id", patient.getExternalEntityID())
                .header("Content-Type", "application/json")
                .request()
                .body(registerMobileApplicationRequest)
                .when()
                .log().all()
                .post("/application/mobile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();
    }

    @Test(priority = 3, testName = "Register Medical Device", description = "Register new medical device to user")
    public void tc01_register_medical_device() throws InterruptedException, IOException {
        accessToken = RestAssuredOAuth.getToken();

        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = objectMapper.readValue(Utils.readRequest("medicalDevice", "registerMedicalDevice"), RegisterMedicalDeviceRequest.class);

        registerMedicalDeviceRequest.inhaler.drugID = digihaler.getDrugID();
        registerMedicalDeviceRequest.inhaler.authenticationKey = digihaler.getAuthenticationKey();
        registerMedicalDeviceRequest.inhaler.nickName = digihaler.getNickName();
        registerMedicalDeviceRequest.inhaler.serialNumber = digihaler.getSerialNumber();

        String responseFile = given()
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .header("external-entity-id", patient.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .header("Content-Type", "application/json")
                .request()
                .body(registerMedicalDeviceRequest)
                .when()
                .log().all()
                .post("/device/digihaler")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();
    }

    @Test(priority = 4, testName = "Update Profile", description = "Updates a profile")
    public void tc01_update_patient() throws InterruptedException, IOException {
        accessToken = RestAssuredOAuth.getToken();

        UpdateProfileRequest updateProfileRequest = objectMapper.readValue(Utils.readRequest("account", "updateProfile"), UpdateProfileRequest.class);

        updateProfileRequest.patient = profileCreationRequest.patient;

        String responseFile = given()
                .baseUri(Utils.readProperty("platformUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .header("external-entity-id", patient.getExternalEntityID())
                .header("Content-Type", "application/json")
                .request()
                .body(updateProfileRequest)
                .when()
                .log().all()
                .put("/account/profile")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();

    }

    @Test(priority = 5, testName = "Validate Sync Message", description = "Get Firebase message")
    public void tc01_validate_sync_message() throws InterruptedException, IOException {

        getFirebaseMessages();


        System.out.println("asdsa");


    }
}
