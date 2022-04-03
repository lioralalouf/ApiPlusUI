package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.MobileApplicationGenerator;
import generators.ProfileGenerator;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.MobileApplication;
import models.database.Profile;
import models.request.ConnectRequest;
import models.request.GenerateApiRequest;
import models.request.account.ProfileCreationRequest;
import models.request.mobiledevice.RegisterMobileApplicationRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import tests.dss.api.PartnerApiTestBase;
import annotations.Traceability;
import utils.TevaAssert;
import utils.Utils;
import java.io.IOException;
import java.text.ParseException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;


import static io.restassured.RestAssured.given;
@Listeners(TestListeners.class)
public class DeletePartnerPositiveTest extends PartnerApiTestBase {
    private String stateToken;
	private Profile profile;
	private MobileApplication mobileApplication;
	private String apiKey;
	private String partnerID;

    @Test(priority = 1, testName = "Delete partner successfully after revoking api key")
    @Traceability(URS = {"x.x.x"}, FS = {"1602"})
    public void tc01_deletePartner() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        
        extentTest.info("Onboarding A new partner");
        this.partnerID = createPartner(extentTest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        extentTest.info("Generate A new api key to onboarded partner");
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
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Revoke the partner's api key");
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

        extentTest.info("Delete partner");
        Response response4 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .when()
                .delete("/{partnerID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        extentTest.info("Get the deleted partner details");
        Response response5 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .when()
                .log()
                .all()
                .when().get("/{partnerID}")
                .then()
                .log().all().
                extract().response();

        TevaAssert.assertEquals(extentTest, response5.getStatusCode(), 404, "Request is expected to have HTTP Response Code `404`");

    }

    @Test(priority = 2, testName = "After delete partner, Verify the data transfer consent status of the user connected to it, is Withdrawn")
    @Traceability(URS = {"x.x.x"}, FS = {"1602"})
    public void tc02_deletePartnerConsentWithdrawn() throws IOException, ParseException, org.json.simple.parser.ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboarding A new partner");
        this.partnerID = createPartner(extentTest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        extentTest.info("Generate A new api key to onboarded partner");
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
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Create A new user profile");
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
        
        extentTest.info("Create A new mobile device");
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
       this.stateToken = extractor2.get("stateToken");
      
       Base64.Decoder dec = Base64.getDecoder();
       String decodedToken = new String(dec.decode(stateToken));
       JSONParser parser = new JSONParser();
       JSONObject jsonToken = (JSONObject) parser.parse(decodedToken);
       String provisionID = jsonToken.get("provisionID").toString();
       String partner = jsonToken.get("partnerID").toString();
       System.out.println("provisio id is - "+provisionID);
       System.out.println("PARTNER id is - "+partner);
       
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
        
        extentTest.info("Get the user account");
        //Get account
        Response response5 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("External-Entity-Id", profile.getExternalEntityID())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .get("/account")
                .then()
                .assertThat().statusCode(200)
                .log().all()
                .extract().response();

        JsonPath json = response5.jsonPath();


        List<LinkedHashMap<String, String>> allConsents = json.getList("account.consents");

			/*
				Check to active
			 */
        
        extentTest.info("Verify the user's data transfer consent status to this partner is active in Http Response");
        for (LinkedHashMap<String, String> consent : allConsents) {
            if (consent.get("consentType").equalsIgnoreCase("dataTransferConsent")) {
                if (consent.get("partnerID").equalsIgnoreCase(partnerID)) {
                    TevaAssert.assertEquals(extentTest,consent.get("status"), "Active",
                            "Data Transfer Consent status should be active");
                }
            }
        }

        extentTest.info("Revoke the partner's api key");
        //Revoke api key
        Response response6 = given()
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
        
        extentTest.info("Delete the partner");
        //delete partner
        Response response4 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .when()
                .delete("/{partnerID}")
                .then()
                .log().all()
                .extract().response();
        
        TevaAssert.assertEquals(extentTest, response4.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        extentTest.info("Verify the user's data transfer consent status to this partner is whithdrawn in Http Response, after delete the partner");
        extentTest.info("Get the user account after delete partner");
      //Get account after delete partner
        Response response7 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("External-Entity-Id", profile.getExternalEntityID())
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .get("/account")
                .then()
                .log().all()
                .extract().response();

        JsonPath json2 = response7.jsonPath();

        List<LinkedHashMap<String, String>> allConsents2 = json2.getList("account.consents");

			/*
				Check to Withdrawn
			 */

        for (LinkedHashMap<String, String> consent : allConsents2) {
            if (consent.get("consentType").equalsIgnoreCase("dataTransferConsent")) {
                if (consent.get("partnerID").equalsIgnoreCase(partnerID)) {
                    TevaAssert.assertEquals(extentTest, consent.get("status"), "Withdrawn",
                            "Data Transfer Consent status should be Withdrawn");
                }
            }
        }

    }
}
