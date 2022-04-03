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
import utils.Utils;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class ApiParametersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(priority = 1, testName = "Create partner with dynamic parameter to prove the tool can run the same test with different parameters"
            , dataProvider = "getPeriodTrue", dataProviderClass = models.DataProviders.class)
    @Traceability(URS = {"1724"})
    public void tc01_dynamic_parameters_test(String period) throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), period);

        String accessToken = RestAssuredOAuth.getToken();

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.quota.period = period;
        extentTest.pass("create partner with parameter - " + period);

        RestAssured.baseURI = Utils.readProperty("adminUrl");
        Response response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("cognito-role", "Admin")
                .request()
                .body(partnerRequest)
                .when()
                .log().all()
                .post("/configuration/partners")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response();

        JsonPath extractor = response.jsonPath();
        String partnerID = extractor.get("partnerID");

        //delete partner from DB
        given()
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

        Thread.sleep(2000);
    }
}
