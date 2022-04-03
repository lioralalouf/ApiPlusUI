package tests.qualification.maven;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.PartnerRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import requests.RestAssuredOAuth;
import annotations.Traceability;
import utils.TevaAssert;
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class ApiPositiveMethodsTest {
    private String accessToken = "";
    private String partnerID = "";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(priority = 0, testName = "Create a new partner to prove POST APIs can be used to test functionality.")
    @Traceability(URS = {"1718", "1728", "1732"})
    public void tc01_POST_request() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        this.accessToken = RestAssuredOAuth.getToken();

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.contact.email = "beforeUpdate@gmail.com";
        RestAssured.baseURI = Utils.readProperty("adminUrl");

        Response response =
                given()
                        .filter(new ConsoleReportFilter(extentTest))
                        .header("report", this.getClass().getSimpleName())
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .request()
                        .body(partnerRequest)
                        .when().log().all()
                        .post("/configuration/partners")
                        .then().log().all()
                        .extract().response();
        JsonPath js = response.jsonPath();
        this.partnerID = js.getString("partnerID");
        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");

        Thread.sleep(2000);
    }

    @Test(priority = 1,testName = "Get partner details to prove GET APIs can be used to test functionality.")
    @Traceability(URS = {"1718", "1728", "1732"})
    public void tc02_GET_request() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        Response response =
                given()
                        .filter(new ConsoleReportFilter(extentTest))
                        .baseUri(Utils.readProperty("adminUrl"))
                        .basePath("configuration/partners")
                        .header("Authorization", "Bearer " + accessToken)
                        .pathParam("partnerID", partnerID)
                        .when().log().all()
                        .get("/{partnerID}")
                        .then()
                        .log().all()
                        .extract().response();

        JsonPath extractor = response.jsonPath();
        String responseEmail = extractor.get("contact.email").toString();
        TevaAssert.assertEquals(extentTest, responseEmail, "beforeUpdate@gmail.com", "original email has been onboarded successfully");
        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
        Thread.sleep(2000);
    }

    @Test(priority = 2, testName = "Update partner to prove PUT APIs can be used to test functionality.")
    @Traceability(URS = {"1718", "1728", "1732"})
    public void tc03_PUT_request() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.contact.email = "afterUpdate@gmail.com";

        Response response =
                given()

                        .filter(new ConsoleReportFilter(extentTest))
                        .baseUri(Utils.readProperty("adminUrl"))
                        .basePath("configuration/partners")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .pathParam("partnerID", partnerID)
                        .request()
                        .body(partnerRequest)
                        .pathParam("partnerID", partnerID)
                        .when().log().all()
                        .put("/{partnerID}")
                        .then()
                        .log().all()
                        .extract().response();

        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
        Thread.sleep(2000);
    }

    @Test(priority = 3,testName = "Get partner details after PUT API to prove PUT APIs work as expected.")
    @Traceability(URS = {"1718", "1728", "1732"})
    public void tc04_GetPartnerDetails_updated() throws IOException, InterruptedException {

        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        Response response =
                given()
                        .filter(new ConsoleReportFilter(extentTest))
                        .baseUri(Utils.readProperty("adminUrl"))
                        .basePath("configuration/partners")
                        .header("Authorization", "Bearer " + accessToken)
                        .pathParam("partnerID", partnerID)
                        .when().log().all()
                        .get("/{partnerID}")
                        .then()
                        .log().all()
                        .extract().response();

        JsonPath extractor = response.jsonPath();
        String responseEmail2 = extractor.get("contact.email").toString();
        TevaAssert.assertEquals(extentTest, responseEmail2, "afterUpdate@gmail.com", "Email has been updated successfully");
        String ResponsePartnerID = extractor.get("partnerID").toString();
        TevaAssert.assertEquals(extentTest, ResponsePartnerID, partnerID, "Partner Id should be identical to the original");
        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
        Thread.sleep(2000);
    }

    @Test(priority = 4,testName = "Delete partner to prove DELETE APIs can be used to test functionality.")
    @Traceability(URS = {"1718", "1728", "1732"})
    public void tc05_DELETE_request() throws IOException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        Response response = given()
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

        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 200, "Request is expected to have HTTP Response Code `200`");
    }

    @Test(priority = 5,testName = "Get partner details after DELETE API to prove DELETE APIs work as expected.")
    @Traceability(URS = {"1718", "1728", "1732"})
    public void tc06_GetPartnerDetails_deleted() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .when()
                .log()
                .all()
                .when().get("/{partnerID}").then()
                .log().all().
                extract().response();

        TevaAssert.assertEquals(extentTest, response.getStatusCode(), 404, "Request is expected to have HTTP Response Code `404`");
        Thread.sleep(2000);
    }
}


