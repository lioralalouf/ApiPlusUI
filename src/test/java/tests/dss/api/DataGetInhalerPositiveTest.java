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
import models.request.ConnectRequest;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import models.request.inhalation.UploadInhalationsRequest;
import models.request.medicaldevice.RegisterMedicalDeviceRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;


@Listeners(TestListeners.class)
public class DataGetInhalerPositiveTest extends PartnerApiTestBase {
    private String accessToken;

    @Test(priority = 1, testName = "test1", description = "")
    @Traceability(FS = {"x.x.x"})
    public void tc01_testDifferentInhalers() throws IOException, ParseException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        accessToken = RestAssuredOAuth.getToken();

        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        String partnerID = createPartner(extentTest, partnerRequest);

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

        // Need Digihaler
        MedicalDevice medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest = new RegisterMedicalDeviceRequest(medicalDevice);
        String actualDeviceSerial = String.valueOf(registerMedicalDeviceRequest.inhaler.serialNumber);

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

        // Need Digihaler2
        medicalDevice = MedicalDeviceGenerator.getAirDuo();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest2 = new RegisterMedicalDeviceRequest(medicalDevice);
        String actualDeviceSerial2 = String.valueOf(registerMedicalDeviceRequest.inhaler.serialNumber);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(registerMedicalDeviceRequest2)
                .when()
                .log().all()
                .post("/device/digihaler")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        // Need Digihaler3
        medicalDevice = MedicalDeviceGenerator.getArmonAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest3 = new RegisterMedicalDeviceRequest(medicalDevice);
        String actualDeviceSerial3 = String.valueOf(registerMedicalDeviceRequest.inhaler.serialNumber);
        registerMedicalDeviceRequest2.inhaler.drugID = "F100";

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(registerMedicalDeviceRequest3)
                .when()
                .log().all()
                .post("/device/digihaler")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        // Need Digihaler4
        medicalDevice = MedicalDeviceGenerator.getProAir();
        RegisterMedicalDeviceRequest registerMedicalDeviceRequest4 = new RegisterMedicalDeviceRequest(medicalDevice);
        String actualDeviceSerial4 = String.valueOf(registerMedicalDeviceRequest.inhaler.serialNumber);

        given()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .header("device-uuid", mobileApplication.getUUID())
                .request()
                .body(registerMedicalDeviceRequest4)
                .when()
                .log().all()
                .post("/device/digihaler")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

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

        String provisionID = getProvisionID(stateToken);
        String partner = getPartnerID(stateToken);
        System.out.println("provisio id is - " + provisionID);
        System.out.println("PARTNER id is - " + partner);

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

        //get inhalers
        given().log().all()
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

        //get medical device
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


        JsonPath json = response5.jsonPath();

        List<LinkedHashMap<String, String>> allInhalers = json.getList("inhalers");
        for (LinkedHashMap<String, String> inhaler : allInhalers) {
            if (inhaler.get("serialNumber").equalsIgnoreCase(actualDeviceSerial)) {
                TevaAssert.assertEquals(extentTest, inhaler.get("drugID"), "AAA200", "Serial number is matched to the original one");
            }
        }


        TevaAssert.assertNotNull(extentTest, "patient.consentStartDate", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.serialNumber", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.lastConnectionDate", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.deviceStatus", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.addedDate", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.drug", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.brandName", "");
        TevaAssert.assertNotNull(extentTest, "inhalers.strength", "");
        String dataDeviceSerial = json.getString("inhalers[0].serialNumber");
        // String dataDeviceSerial2 = json.getString("inhalers[1].serialNumber");
        // TevaAssert.assertEquals(extentTest, dataDeviceSerial, actualDeviceSerial, "Serial number is matched to the original one");
        // TevaAssert.assertEquals(extentTest, dataDeviceSerial2, actualDeviceSerial2, "Serial number is matched to the original one");

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
