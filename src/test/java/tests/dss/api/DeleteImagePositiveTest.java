package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.PartnerRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import utils.FileUtils;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class DeleteImagePositiveTest extends PartnerApiTestBase {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(priority = 0, testName = "Verify image has been deleted successfully")
    @Traceability(FS = {"1703"})
    public void tc01_testIconPath() throws IOException, NoSuchAlgorithmException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        String accessToken = RestAssuredOAuth.getToken();

        extentTest.info("Onboard a new image to S3 Bucket and save the image hash");
        File iconFile = new File("./icons/image1.jpg");
        String image1Hash = FileUtils.getFileHash(iconFile);

        Response responseFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .multiPart("file", iconFile, "image/jpg")
                .log().all()
                .post("/configuration/image")
                .then()
                .log().all()
                .extract().response();

        JsonPath extract = responseFile.jsonPath();
        extentTest.info("Extract the image URL from the HTTP Response");
        String iconUrl = extract.get("url");


        extentTest.info("Onboard a new partner and set the URL to logo field");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.icon = iconUrl;

        String partnerID = createPartner(extentTest, partnerRequest);

        extentTest.info("Get the partner details, Verify response image hash and path are identical to the original image hash and path");
        Response response2 =
                given()
                        .filter(new ConsoleReportFilter(extentTest))
                        .baseUri(Utils.readProperty("adminUrl"))
                        .basePath("configuration/partners")
                        .header("Authorization", "Bearer " + accessToken)
                        .pathParam("partnerID", partnerID)
                        .when()
                        .log().all()
                        .when()
                        .get("/{partnerID}")
                        .then()
                        .log().all()
                        .assertThat().statusCode(200)
                        .extract().response();

        JsonPath extractor = response2.jsonPath();
        String ResponsePartnerID = extractor.get("partnerID");
        String partnerIconPath = extractor.get("icon");
        String imageDownloadHash = FileUtils.getRemoteFileHash(partnerIconPath);
        File f = new File(partnerIconPath);
        String fileName = f.getName();

        TevaAssert.assertEquals(extentTest, partnerID, ResponsePartnerID, "Partner ID should be identical");
        TevaAssert.assertEquals(extentTest, partnerIconPath, iconUrl, "Icon path should be identical");
        TevaAssert.assertEquals(extentTest, image1Hash, imageDownloadHash, "Icon hash should be identical");

        extentTest.info("Delete the image, Expect HTTP Response '200' for successfull deleted");
        Response responseDeleteFile = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("imageID", fileName)
                .log().all()
                .delete("/configuration/image/{imageID}")
                .then()
                .log().all()
                .assertThat().statusCode(200)
                .extract().response();

        TevaAssert.assertEquals(extentTest, responseDeleteFile.getStatusCode(), 200, "HTTP Response status code 200 for delete image successfully");

        extentTest.info("Try delete the same image again, Expect HTTP Response '404' because image is not exists");
        Response responseDeleteFile2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("imageID", fileName)
                .log().all()
                .delete("/configuration/image/{imageID}")
                .then()
                .log().all()
                .extract().response();

        TevaAssert.assertEquals(extentTest, responseDeleteFile2.getStatusCode(), 404, "the image has not been found because we deleted it successfully");
    }
}
