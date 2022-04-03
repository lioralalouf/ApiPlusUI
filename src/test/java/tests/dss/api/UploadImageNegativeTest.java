package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class UploadImageNegativeTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Onboard not 'image type' file")
    @Traceability(FS = {"1702"})
    public void tc01_uploadInvalidImageType() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        String accessToken = RestAssuredOAuth.getToken();

        extentTest.info("Onboard A txt file as an image file to S3 Bucket, Expect HTTP Response error code '400', because Image must be of image type");
        File InvalidImageFile = new File("./documents/privacyNotice.txt");

        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", InvalidImageFile, "text/plain")
                .log().all()
                .post("/configuration/image")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 400, "Image must be of image type");
    }
}
