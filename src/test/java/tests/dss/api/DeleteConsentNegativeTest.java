package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.OnboardOrganizationRequest;
import models.request.OnboardProgramRequest;
import models.request.PartnerRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class DeleteConsentNegativeTest extends PartnerApiTestBase {

    private String organizationID;

    @Test(priority = 1, testName = "Delete document for used version by partner")
    @Traceability(FS = {"1628"})
    public void tc01_deletePartnerDocument() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner, activePrivacyNoticeVersion is 1.0");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Upload privacy notice version1 document for the partner");
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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "privacy notice file uploaded successfully");

        extentTest.info("Get the consents for this partner, Verify privacy notice version 1 is in the list");
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl"))
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);
        List<String> list1 = new ArrayList<>();
        list1.add("pn");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Privacy notice version 1.0 is displayed");

        extentTest.info("Delete privacy notice version version 1 consent, Expecting HTTP Response error code '404', because version is used by the partner");
        //Delete privecy notice version 1 for the partner
        Response deleteResponse = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                //.multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .delete(Utils.readProperty("onboardPn01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, deleteResponse.getStatusCode(), 400, "Cant delete the document, because version is used by the partner");

    }

    public static class ConsentsResponse {

        public List<Consent> consents;

        public static class Consent {
            public String version;
            public List<Locales> locales;

            public static class Locales {
                public String locale;
                public List<String> legalTypes;
            }
        }
    }

    @Test(priority = 2, testName = "Delete document for used version for specific organization")
    @Traceability(FS = {"1628"})
    public void tc02_deleteOrganizationConsent() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Create organization, activePrivacyNoticeVersion is 1.0");
        //Create organization
        OnboardOrganizationRequest organizationRequest = objectMapper.readValue(Utils.readRequest("organization", "onboardOrganization"),
                OnboardOrganizationRequest.class);

        extentTest.info("Upload privacy notice document version 1 for the organization");
        organizationRequest.mnemonic = UUID.randomUUID().toString();
        organizationRequest.activePrivacyNoticeVersion = "1.0";

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


        extentTest.info("Uploading privacy notice document");
        //upload privacy notice version 1 for the organization
        File pnTxt = new File("./documents/privacyNotice.txt");
        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", pnTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPn01ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "privacy notice file uploaded successfully");

        extentTest.info("Get the consents for this organization, Verify privacy notice version 1 is in the list");
        //Get the consent for the organization before delete it
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_organization"))
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);
        List<String> list1 = new ArrayList<>();
        list1.add("pn");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Privacy notice version 1.0 is displayed");

        extentTest.info("Delete privacy notice version version 1 consent, Expecting HTTP Response error code '404', because version is used by the partner");
        //Delete the consent version 1
        Response deleteResponse = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                //.multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .delete(Utils.readProperty("onboardPn01ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, deleteResponse.getStatusCode(), 400, "Cant delete the document, because version is used by the organization");
    }

    @Test(priority = 3, testName = "Delete documents for used version for specific program")
    @Traceability(FS = {"1628"})
    public void tc03_deleteProgramDocument() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Create organization");
        //Create organization
        OnboardOrganizationRequest organizationRequest = objectMapper.readValue(Utils.readRequest("organization", "onboardOrganization"),
                OnboardOrganizationRequest.class);

        organizationRequest.mnemonic = UUID.randomUUID().toString();
        organizationRequest.activePrivacyNoticeVersion = "1.0";

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

        extentTest.info("Create program, activePrivacyNoticeVersion is 1.0");
        //create program
        OnboardProgramRequest programRequest = objectMapper.readValue(Utils.readRequest("program", "onboardProgram"),
                OnboardProgramRequest.class);
        programRequest.organizationID = organizationID;
        programRequest.programName = UUID.randomUUID().toString();
        programRequest.activeTermsAndConditionsVersion = "1.0";

        Response responseProgram = given()
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

        JsonPath js2 = responseProgram.jsonPath();
        String programID = js2.get("programID");

        extentTest.info("Upload Terms Of Use document version 1 for the program");
        //upload terms of use document for the program
        File pnTxt = new File("./documents/termsAndConditions.txt");
        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .multiPart("file", pnTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardtou01ConsentUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "Terms of Use  file uploaded successfully");

        extentTest.info("Get the consents for this program, Verify terms of use version 1 is in the list");
        //Get the consent for the program before delete it
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_program"))
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);
        List<String> list1 = new ArrayList<>();
        list1.add("tou");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Terms of Use version 1.0 is displayed");

        extentTest.info("Delete terms of use version 1 consent, Expecting HTTP Response error code '404', because version is used by the program");
        //Delete the consent version 1
        Response deleteResponse = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                //.multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .delete(Utils.readProperty("onboardtou01ConsentUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, deleteResponse.getStatusCode(), 400, "Cant delete the document, because version is used by the program");
    }

    @Test(priority = 4, testName = "Delete documents for used version for specific program")
    @Traceability(FS = {"1628"})
    public void tc04_deleteDefaultProgram() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Get the consents for this program, Verify terms of use version 1 is in the list");
        //Get the consent for the program before delete it
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", "_default_")
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_program"))
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);
        List<String> list1 = new ArrayList<>();
        list1.add("pn");
        list1.add("tou");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Terms of Use version 1.0 is displayed");

        extentTest.info("Delete terms of use version 1 consent, Expeting HTTP Response error code '400', because can't delete default consent documents");
        //Delete the consent version 1
        Response deleteResponse = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", "_default_")
                .log().all()
                .delete(Utils.readProperty("onboardtou01ConsentUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, deleteResponse.getStatusCode(), 400, "Cant delete the document, because can't delete default consent documents");
    }

}
