package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.OnboardOrganizationRequest;
import models.request.OnboardProgramRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class OnboardProgramConsentNegativeTest extends PartnerApiTestBase {
    private String programID = "";

    @Test(priority = 1, testName = "Getting consents for program with no consents")
    @Traceability(FS = {"1627", "1629", "1659"})
    public void tc01_getNoConsents() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Create a new organization");
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
        String organizationID = js.get("organizationID");

        extentTest.info("Create a new program");
        //create program
        OnboardProgramRequest programRequest = objectMapper.readValue(Utils.readRequest("program", "onboardProgram"),
                OnboardProgramRequest.class);
        programRequest.organizationID = organizationID;
        programRequest.programName = UUID.randomUUID().toString();

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
        this.programID = js2.get("programID");

        extentTest.info("Get consents for the program, Expecting HTTP Response Error code '404' for no existing consents for the program");
        Response responseGet = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .log().all()
                .get(Utils.readProperty("getConsentsUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, responseGet.getStatusCode(), 404, "Getting HTTP Response Error code 404 for no existing consents for the program");
    }

    @Test(priority = 2, testName = "Onboard file with no content or mismatched consent legalTyp")
    @Traceability(FS = {"1627", "1629", "1659"})
    public void tc02_getMismatchedTypeOrNoContent() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Uploading privacy notice file for the program with empty content");
        //upload privacy notice for the program
        File pnTxt = new File("./icons/privacyNotice.txt");
        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .multiPart("file", pnTxt, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPn01ConsentUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, responseFile.getStatusCode(), 400, "Getting HTTP Response Error code 404 for File doesn't have proper content");


        extentTest.info("Uploading privacy notice file for the program with wrong name");
        //upload privacy notice for the program
        File pnTxt2 = new File("./documents/privacyNotic.txt");
        Response responseFile2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("programID", programID)
                .multiPart("file", pnTxt2, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPn01ConsentUrl_program"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, responseFile2.getStatusCode(), 400, "Getting HTTP Response Error code 404 for mismatched consent legalType pn");
    }
}
