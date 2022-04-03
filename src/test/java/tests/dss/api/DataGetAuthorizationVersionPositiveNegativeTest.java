package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import generators.ProfileGenerator;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.Profile;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import models.request.account.ProfileCreationRequest;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import tests.dss.api.OnboardPartnerConsentPositiveTest.ConsentsResponse;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class DataGetAuthorizationVersionPositiveNegativeTest extends PartnerApiTestBase {

    private Profile profile;
    private String apiKey;
    private String partnerID;

    @Test(priority = 1, testName = "Get partner local authorizations by patient positive test")
    @Traceability(FS = {"1662"})
    public void tc01_getPartnerLocalAuthrizationsPositive() throws IOException {
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

        extentTest.info("Generate a new api key for onboarded partner");
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
        registerApiKey(partnerID, apiKey);
        extentTest.info("Onboard documents for this partner 'hipaa, marketing, privacy notice, pac, signature policy and terms of use'");
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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "privacy notice file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "terms of use file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileC.getStatusCode(), "Hipaa file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileD.getStatusCode(), "marketing consent file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileE.getStatusCode(), "Signature policy file uploaded successfully");

        extentTest.info("Upload signature document to the partner");
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

        TevaAssert.assertEquals(extentTest, 200, responseFileF.getStatusCode(), "pac file uploaded successfully");

        extentTest.info("Get the partner's consents");
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

        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Correct Documents for version 1.0 are displayed");

        extentTest.info("Create new user profile");
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

        extentTest.info("Get the latest version of authorization language for this partner, by the user");
        Response resGetData = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .pathParam("partnerID", partnerID)
                .when()
                .log().all()
                .get("/partner/{partnerID}/authorization/en-US")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath js2 = resGetData.jsonPath();
        TevaAssert.assertEquals(extentTest, js2.get("authorizations.hipaaDisclosure"), Utils.readProperty("hipaaText"), "Hipaa Disclosure content is correct");
        TevaAssert.assertEquals(extentTest, js2.get("authorizations.signaturePolicy"), Utils.readProperty("signatureText"), "Signature Policy content is correct");
        TevaAssert.assertEquals(extentTest, js2.get("authorizations.marketingConsent"), Utils.readProperty("marketingText"), "Marketing Consent content is correct");
        TevaAssert.assertEquals(extentTest, js2.get("authorizations.termsOfUse"), Utils.readProperty("termsOfUseText"), "Terms Of Use content is correct");
        TevaAssert.assertEquals(extentTest, js2.get("authorizations.privacyNotice"), Utils.readProperty("privacyNoticeText"), "Privacy Notice content is correct");
        //TevaAssert.assertTrue(extentTest, js2.get("authorizations").toString().contains(""), "Pac content is not persent in HTTP Response");

    }

    @Test(priority = 1, testName = "Get partner local authorizations by patient for partner with no legal consent - negative test")
    @Traceability(FS = {"1662"})
    public void tc02_getLocalAuthorizationNoLegalNegative() throws IOException, ParseException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner, activeSignature version is 1.0 ");
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

        extentTest.info("Generate a new api key for onboarded partner");
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
        registerApiKey(partnerID, apiKey);

        extentTest.info("Onboard documents for this partner 'hipaa, marketing, privacy notice, pac, signature policy and terms of use, be sure to upload Signature Policy version 2.0'");
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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "privacy notice file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "terms of use file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileD.getStatusCode(), "marketing consent file uploaded successfully");

        File signatureTxt = new File("./documents/signature.txt");
        Response responseFileE = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardSignature02ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileE.getStatusCode(), "Signature consent file uploaded successfully");

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

        TevaAssert.assertEquals(extentTest, 200, responseFileF.getStatusCode(), "pac file uploaded successfully");

        extentTest.info("Get the partner's consent and verify Signature Policy is in version 2.0 list");
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
        list1.add("tou");

        List<String> list2 = new ArrayList<>();
        list2.add("signature");

        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Correct Documents for version 1.0 are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).locales.get(0).legalTypes, list2, "Documents for version 2.0 are displayed");

        extentTest.info("Create new user account");
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

        extentTest.info("Get the latest version of authorization language for this partner by the user, Expecting HTTP Response error code '400', because some partner legal consent not found");
        Response resGetData = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .pathParam("partnerID", partnerID)
                .when()
                .log().all()
                .get("/partner/{partnerID}/authorization/en-US")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, resGetData.getStatusCode(), 404, "Http response error code 404, because some partner legal consent not found");
    }

    @Test(priority = 1, testName = "Get partner local authorizations by patient for deleted partner - negative test")
    @Traceability(FS = {"1662"})
    public void tc03_getLocalAuthorizationDeletedPartnerNegative() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onbard a new partner");
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response = given()
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

        extentTest.info("Delete this partner");
        Response response2 = given()
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

        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 200, "Partner has been deleted successfully");

        extentTest.info("Get the latest version of authorization language for this partner by the user, Expecting HTTP Response error code '404', because partner doesnt exist");
        Response resGetData = given().log().all()
                .baseUri(Utils.readProperty("platformUrl"))
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("external-entity-id", profile.getExternalEntityID())
                .pathParam("partnerID", partnerID)
                .when()
                .log().all()
                .get("/partner/{partnerID}/authorization/en-US")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, resGetData.statusCode(), 404, "Error 404 expected, because partner doesnt exist");
    }
}
