package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.OnboardOrganizationRequest;
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
public class OnboardOrganizationConsentPositiveTest extends PartnerApiTestBase {

    private String organizationID = "";

    @Test(priority = 1, testName = "Onboard documents to specific organization")
    @Traceability(FS = {"1627", "1629", "1659"})
    public void tc01_checkAccessTokenCreated() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new organization");
        //Create organization
        OnboardOrganizationRequest organizationRequest = objectMapper.readValue(Utils.readRequest("organization", "onboardOrganization"),
                OnboardOrganizationRequest.class);

        organizationRequest.mnemonic = UUID.randomUUID().toString();

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

        extentTest.info("Onboard documents to this organization 'hipaa, marketing, privacy notice,pac, signature policy and terms of use'");
        //upload privacy notice for the organization
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

        TevaAssert.assertEquals(extentTest, 200, responseFile.getStatusCode(), "Privacy Notice file uploaded successfully");

        //upload terms and conditions for the organization
        File termsTxt = new File("./documents/termsAndConditions.txt");
        Response responseFileB = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", termsTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardtou01ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileB.getStatusCode(), "Terms Of Use file uploaded successfully");

        //upload hipaa for the organization
        File hippaTxt = new File("./documents/hipaa.txt");
        Response responseFileC = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", hippaTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardHipaa01ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileC.getStatusCode(), "Hipaa file uploaded successfully");

        //upload marketing for the organization
        File marketingTxt = new File("./documents/marketing.txt");
        Response responseFileD = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", marketingTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardMarketing02ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileD.getStatusCode(), "Marketing Consent file uploaded successfully");

        //upload signature for the organization
        File signatureTxt = new File("./documents/signature.txt");
        Response responseFileE = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardSignature02ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileE.getStatusCode(), "Signature Policy file uploaded successfully");

        //upload pac for the organization
        File pacTxt = new File("./documents/pac.txt");
        Response responseFileF = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", pacTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPac02ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileF.getStatusCode(), "Pac file uploaded successfully");

        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);

        List<String> list1 = new ArrayList<>();
        list1.add("hipaa");
        list1.add("pn");
        list1.add("tou");

        List<String> list2 = new ArrayList<>();
        list2.add("marketing");
        list2.add("pac");
        list2.add("signature");

        extentTest.info("Verify all versions are displayed, all locales and every document is displayed in the correct version and locale ");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).version, "1.0", "Version 1 documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).version, "2.0", "Version 2 documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).locale, "en-US", "en-US documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).locales.get(0).locale, "es-US", "es-US documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Documents for version 1.0 are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).locales.get(0).legalTypes, list2, "Documents for version 2.0 are displayed");
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

    @Test(priority = 2, testName = "Update documents to specific organization")
    @Traceability(FS = {"1627", "1629", "1659"})
    public void tc02_UpdateDocuments() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard signature policy document in different version 'Version 1'");
        //upload signature and conditions for the organization
        File signatureTxt = new File("./documents/signature.txt");
        Response responseFileE = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .multiPart("file", signatureTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardSignature01ConsentUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, 200, responseFileE.getStatusCode(), "Signature Policy file uploaded successfully");

        extentTest.info("Get the consents for this organization");
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("organizationID", organizationID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_organization"))
                .then()
                .log().all()
                .extract().response();

        ConsentsResponse consentsResponse = responseGet.getBody().as(ConsentsResponse.class);

        List<String> list1 = new ArrayList<>();
        list1.add("hipaa");
        list1.add("pn");
        list1.add("signature");
        list1.add("tou");


        List<String> list2 = new ArrayList<>();
        list2.add("marketing");
        list2.add("pac");
        list2.add("signature");

        extentTest.info("Verify all versions are displayed, all locales and every document is displayed in the correct version and locale");
        extentTest.info("Verify that updated signature document version 1 and version 2 are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).version, "1.0", "Version 1 documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).version, "2.0", "Version 2 documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).locale, "en-US", "en-US documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).locales.get(0).locale, "es-US", "es-US documents are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(0).locales.get(0).legalTypes, list1, "Documents for version 1.0 are displayed");
        TevaAssert.assertEquals(extentTest, consentsResponse.consents.get(1).locales.get(0).legalTypes, list2, "Documents for version 2.0 are displayed");
    }
}
