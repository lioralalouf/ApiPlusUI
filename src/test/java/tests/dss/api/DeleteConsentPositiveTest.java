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
public class DeleteConsentPositiveTest extends PartnerApiTestBase {

    private String organizationID = "";

    @Test(priority = 1, testName = "Delete consents for specific organization")
    @Traceability(FS = {"1628"})
    public void tc01_deleteOrganizationConsent() throws IOException {
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

        TevaAssert.assertEquals(extentTest, 200, responseOrganization.getStatusCode(), "Organization has been created successfully");

        JsonPath js = responseOrganization.jsonPath();
        this.organizationID = js.get("organizationID");

        extentTest.info("Upload privacy notice document");
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

        //Update active privacy notice version to organization

        extentTest.info("Update the active privacy notice version for the organization from 1.0 to 2.0");
        organizationRequest.activePrivacyNoticeVersion = "2.0";

        Response responseOrganization2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .request()
                .body(organizationRequest)
                .when()
                .log().all()
                .put("/configuration/organizations/{organizationID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseOrganization2.getStatusCode(), "active privacy notice version has been updated successfully");

        extentTest.info("Delete the privacy notice version 1 consent");
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
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, deleteResponse.getStatusCode(), "Privacy notice deleted successfully from organization");

        extentTest.info("Get consents for organization, Expecting HTTP response error code '404', because No consents exist for this resource");
        //Get the consent for the organization after delete it
        Response responseGet2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 404, responseGet2.getStatusCode(), "HTTP error code response '404', because No consents exist for this resource");
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

    @Test(priority = 2, testName = "Delete consents for specific program")
    @Traceability(FS = {"1628"})
    public void tc02_deleteProgramConsent() throws IOException {
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

        TevaAssert.assertEquals(extentTest, 200, responseOrganization.getStatusCode(), "Organization has been created successfully");

        JsonPath js = responseOrganization.jsonPath();
        this.organizationID = js.get("organizationID");

        extentTest.info("Create program for this organization");
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

        TevaAssert.assertEquals(extentTest, 200, responseProgram.getStatusCode(), "program has been created successfully");

        JsonPath js2 = responseProgram.jsonPath();
        String programID = js2.get("programID");

        extentTest.info("Upload terms of use document");
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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "terms of use file uploaded successfully");

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
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Terms of use version 1.0 is displayed");

        extentTest.info("Update the active terms of use version for the program from 1.0 to 2.0");
        //Update active privacy notice version to program
        programRequest.activeTermsAndConditionsVersion = "2.0";

        Response responseProgram2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .request()
                .body(programRequest)
                .when()
                .log().all()
                .put("/configuration/programs/{programID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseProgram2.getStatusCode(), "active terms of use version has been updated successfully");

        extentTest.info("Delete the terms of use version version 1 consent");
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
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, deleteResponse.getStatusCode(), "Terms of use deleted successfully from program");

        extentTest.info("Get consents for the program, Expecting HTTP response error code '404', because No consents exist for this resource ");
        //Get the consent for the program after delete it
        Response responseGet2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 404, responseGet2.getStatusCode(), "HTTP error code response '404', because No consents exist for this resource");
    }

    @Test(priority = 3, testName = "Delete documents for specific partner")
    @Traceability(FS = {"1628"})
    public void tc03_deletePartnerConsent() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Upload hipaa document");
        //upload hipaa for the partner
        File hippaTxt = new File("./documents/hipaa.txt");
        Response responseFile = given()
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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "Hipaa file uploaded successfully");

        extentTest.info("Get the consents for this partner, Verify hipaa version 1 is in the list");
        //Get the consents for the partner before delete it
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
        list1.add("hipaa");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Hipaa version 1.0 is displayed");

        extentTest.info("Update the active Hipaa Disclosure version for the partner from 1.0 to 2.0");
        //Update active hippa disclosure version to partner from 1.0 to 2.0
        partnerRequest.activeHipaaDisclosure = "2.0";

        Response response3 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .request()
                .body(partnerRequest)
                .when()
                .log().all()
                .put("/configuration/partners/{partnerID}")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, response3.getStatusCode(), "Hipaa version has been updated successfully");

        extentTest.info("Delete hipaa version version 1 consent");
        //Delete the consent version 1
        Response deleteResponse = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                //.multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .delete(Utils.readProperty("onboardHipaa01ConsentUrl"))
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, deleteResponse.getStatusCode(), "Hipaa of use deleted successfully from program");

        extentTest.info("Get consents for the partner, Expecting HTTP response error code '404', because No consents exist for this resource ");
        //Get the consent for the program after delete it
        Response responseGet2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 404, responseGet2.getStatusCode(), "HTTP error code response '404', because No consents exist for this resource");
    }
}
