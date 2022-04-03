package tests.dss.ui;

import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import pageObjects.FailureScreenPage;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;
import reporter.ConsoleReportFilter;
import tests.UiBaseTest;
import annotations.Traceability;
import utils.FileUtils;
import utils.LocalStorageUtils;
import utils.TevaAssert;
import utils.Utils;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class FailureScreenPositiveTest extends UiBaseTest {
    private final String partnerNameTemp = "";
    private String image1Hash;


    @Test(priority = 1, testName = "Invalid state token should navigate the user to failure screen")
    @Traceability(URS = {"x.x.x"}, FS = {"x.x.x"})
    public void tc01_getFailureScreen() throws IOException, NoSuchAlgorithmException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        File iconFile = new File("./icons/image1.jpg");
        image1Hash = FileUtils.getFileHash(iconFile);

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
        String iconUrl = extract.get("url");

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        partnerRequest.icon = iconUrl;

        RestAssured.baseURI = Utils.readProperty("adminUrl");
        String response = given()
                .filter(new ConsoleReportFilter(extentTest))
                .header("Authorization", "Bearer " + accessToken).
                header("Content-Type", "application/json")
                .request()
                .body(partnerRequest).when()
                .log().all()
                .post("/configuration/partners")
                .then()
                .log().all()
                .assertThat()
                .statusCode(200)
                .extract().response().asString();

        JsonPath js = new JsonPath(response);
        this.partnerID = js.getString("partnerID");
        registerPartnerID(partnerID);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        Response response2 = given()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID).body(apiKeyRequest)
                .when().post("/{partnerID}/key")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        this.apiKey = extractor.get("apiKey");

        VendorToolPage vendorToolPage = new VendorToolPage(driver);
        vendorToolPage.login("123456", apiKey);

        System.out.println(apiKey);

        PartnerLoginPage partnererLoginPage = new PartnerLoginPage(driver);
        boolean actual = partnererLoginPage.checkTitle();
        TevaAssert.assertTrue(extentTest,actual, "digihelerLoginPage is displayed");

        partnererLoginPage.login(Utils.readProperty("idHubUser"), Utils.readProperty("idHubPassword"));

        Base64.Encoder encoder = Base64.getEncoder();
        String stateToken = encoder.encodeToString("{\"patientID\": \"FAKE\", \"provisionID\": \"FAKE\"}".getBytes());
        LocalStorageUtils localStorageUtils = new LocalStorageUtils(driver);
        localStorageUtils.setItemInLocalStorage("stateToken", stateToken);
        driver.navigate().refresh();

        FailureScreenPage fs = new FailureScreenPage(driver);
        TevaAssert.assertEquals(extentTest, fs.getError(), "Account not linked", "Navigated to error message succesfully");


        //REVOKE API KEY AND DELETE PARTNER FROM DB
        RestAssured.baseURI = Utils.readProperty("adminUrl");
        given()
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
    }
}
