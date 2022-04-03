package tests.examples;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.*;
import models.database.*;
import models.request.account.ProfileCreationRequest;
import models.request.inhalation.UploadInhalationsRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import models.request.questionnaire.UploadDsasRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import annotations.Traceability;
import utils.Utils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class PatientDataExample {

    private String accessToken;
    private Profile profile;
    private MedicalDevice medicalDevice;
    private MobileApplication mobileApplication;


    @Test(priority = 0,testName = "Create a patient account.")
    @Traceability(URS = {"x.x.x"})
    public void create_profile_test() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        accessToken = RestAssuredOAuth.getToken();

        profile = ProfileGenerator.getProfile();
        ProfileCreationRequest profileCreationRequest = new ProfileCreationRequest(profile);

        given()
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
    }

    @Test(priority = 1,testName = "Create mobile device.")
    @Traceability(URS = {"x.x.x"})
    public void create_mobile_application_test() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
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
    }

    @Test(priority = 2,testName = "Create a medical device.")
    @Traceability(URS = {"x.x.x"})
    public void create_medical_device_test() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
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
    }

    /*
        Bulk update medical device
        update medical device status

        Create dependent example

     */

    @Test(priority = 3,testName = "Create inhalation data.")
    @Traceability(URS = {"x.x.x"})
    public void create_medical_administration_test() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        List<Inhalation> inhalations = new ArrayList<>();

        for (int i = 0 ; i < 10; i++) {
            Inhalation inhalation = InhalationGenerator.generateGoodInhalation(medicalDevice);
            inhalations.add(inhalation);
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
    }

    @Test(priority = 4,testName = "Create DSA data.")
    @Traceability(URS = {"x.x.x"})
    public void create_dsa_test() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        List<DailySelfAssessment> dsas = new ArrayList<>();

        LocalDate date
                = LocalDate.parse("2022-02-22");

        for (int i = 0 ; i < 10; i++) {
            date = date.minusDays(i);

            DailySelfAssessment dsa = DsaGenerator.getDsa();
            dsa.date = date.format(DateTimeFormatter.ISO_DATE);

            dsas.add(dsa);

        }

        UploadDsasRequest uploadDsasRequest = new UploadDsasRequest(dsas);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(uploadDsasRequest)
                .when()
                .log().all()
                .post("/questionnaire/dsas")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();
    }

}
