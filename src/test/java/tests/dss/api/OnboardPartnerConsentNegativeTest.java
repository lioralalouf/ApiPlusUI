package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.response.Response;
import models.request.PartnerRequest;
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
public class OnboardPartnerConsentNegativeTest extends PartnerApiTestBase {

    private String partnerID = "";

    @Test(priority = 1, testName = "Get consents for partner with no consents")
    @Traceability(FS = {"1627", "1629", "1659"})
    public void tc01_getNoConsents() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard a new partner");
        //create partner
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        this.partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Get consents for the partner, Expecting HTTP Response Error code '404' for no existing consents for the partner");
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

        TevaAssert.assertEquals(extentTest, responseGet.getStatusCode(), 404, "Getting HTTP Response Error code 404 for no existing consents for the partner");

    }

    @Test(priority = 2, testName = "Onboard file with no content or mismatched consent legalTyp")
    @Traceability(FS = {"1627", "1629", "1659"})
    public void tc02_getMismatchedTypeOrNoContent() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Uploading privacy notice file for the partner with empty content");
        //upload privacy notice for the partner
        File pnTxt = new File("./icons/privacyNotice.txt");
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

        TevaAssert.assertEquals(extentTest, responseFile.getStatusCode(), 400, "Getting HTTP Response Error code 404 for File doesn't have proper content");

        extentTest.info("Uploading privacy notice file for the partner with wrong name");
        //upload privacy notice for the partner
        File pnTxt2 = new File("./documents/privacyNotic.txt");
        Response responseFile2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .multiPart("file", pnTxt2, "text/plain")
                .log().all()
                .post(Utils.readProperty("onboardPn01ConsentUrl"))
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, responseFile2.getStatusCode(), 400, "Getting HTTP Response Error code 404 for mismatched consent legalType pn");
    }
}
