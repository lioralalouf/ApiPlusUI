package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import models.database.PartnerKey;
import models.request.GenerateApiRequest;
import models.request.PartnerRequest;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import reporter.ConsoleReportFilter;
import repository.PartnerRepository;
import utils.TevaAssert;
import utils.Utils;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@Listeners(TestListeners.class)
public class ExpiredApiKeyPositiveTest extends PartnerApiTestBase {
	private PartnerRepository partnerRepository = new PartnerRepository();

    @Test(priority = 1, testName = "Check that api key is expired successfully exactly after 12 months")
    @Traceability(FS = {"1653"})
    public void tc01_keyExpirationTrue() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard A new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        extentTest.info("Generate A new api key for onboarded partner");
        Response response2 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .then().log().all()
                .request()
                .body(apiKeyRequest)
                .when()
                .post("/{partnerID}/key")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        String apiKey = extractor.get("apiKey");
        TevaAssert.assertNotNull(extentTest, apiKey, "");
        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 200, "Api key has been generated successfully");
        
        List<PartnerKey> partnerKeys = partnerRepository.findApiKeyByPartnerID(partnerID);
        registerApiKey(partnerID, apiKey);
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Set api key setGrantAccessTimestamp in DB 1 year back");
        LocalDate today =  LocalDate.now(); 
        LocalDate yearBack = today.minusDays(365);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String y = yearBack.format(formatter);
        System.out.println("updatedDate is "+y);

        PartnerKey partnerKey = partnerKeys.get(0);
        Assert.assertNotNull(partnerKey);
        partnerKey.setGrantAccessDate(y);
        partnerKey.setGrantAccessTimestamp(y);
        partnerRepository.persistPartnerKey(partnerKey);
        
        List<PartnerKey> updateKeys =partnerRepository.findApiKeyByPartnerID(partnerID);
        PartnerKey updatedPartnerKey = updateKeys.get(0);
        Assert.assertEquals(y,updatedPartnerKey.getGrantAccessDate());
        
        Thread.sleep(3 * 60 * 1000);
        
        extentTest.info("Get the api key details, Expecting HTTP Response error code '404', because Api Key doesnt exist");
        Response response3 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .when()
                .log().all()
                .get("/data/api/key")
                .then()
                .log()
                .all()
                .extract()
                .response();
        
        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 404, "Expecting HTTP Response error code '404', because Api Key doesnt exist");

	}
  
    @Test(priority = 1, testName = "Check that api key is not expired one day before expected expiration date")
    @Traceability(FS = {"1653"})
    public void tc01_keyExpirationFalse() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());

        extentTest.info("Onboard A new partner");
        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();

        String partnerID = createPartner(extentTest, partnerRequest);

        GenerateApiRequest apiKeyRequest = objectMapper.readValue(Utils.readRequest("partner", "newApiKey"),
                GenerateApiRequest.class);

        extentTest.info("Generate A new api key for onboarded partner");
        Response response2 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("adminUrl"))
                .basePath("configuration/partners")
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .pathParam("partnerID", partnerID)
                .then().log().all()
                .request()
                .body(apiKeyRequest)
                .when()
                .post("/{partnerID}/key")
                .then()
                .log().all()
                .extract().response();

        JsonPath extractor = response2.jsonPath();
        String apiKey = extractor.get("apiKey");
        TevaAssert.assertNotNull(extentTest, apiKey, "");
        TevaAssert.assertEquals(extentTest, response2.getStatusCode(), 200, "Api key has been generated successfully");
   
        List<PartnerKey> partnerKeys =partnerRepository.findApiKeyByPartnerID(partnerID);
        registerApiKey(partnerID, apiKey);
        TevaAssert.assertNotNull(extentTest, apiKey, "");

        extentTest.info("Set api key setGrantAccessTimestamp in DB 364 days back");
        LocalDate today =  LocalDate.now(); 
        LocalDate yearBack = today.minusDays(364);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String y = yearBack.format(formatter);
        System.out.println("updatedDate is "+y);

        PartnerKey partnerKey = partnerKeys.get(0);
        Assert.assertNotNull(partnerKey);
        partnerKey.setGrantAccessDate(y);
        partnerKey.setGrantAccessTimestamp(y);
        partnerRepository.persistPartnerKey(partnerKey);
        
        List<PartnerKey> updateKeys =partnerRepository.findApiKeyByPartnerID(partnerID);
        PartnerKey updatedPartnerKey = updateKeys.get(0);
        Assert.assertEquals(y,updatedPartnerKey.getGrantAccessDate());
        
        Thread.sleep(3 * 60 * 1000);
        
        extentTest.info("Get the created api key details");
        Response response3 = given().log().all()
                .filter(new ConsoleReportFilter(extentTest))
                .baseUri(Utils.readProperty("platformUrl"))
                .header("X-API-Key", apiKey)
                .when()
                .log().all()
                .get("/data/api/key")
                .then()
                .log()
                .all()      
                .extract()
                .response();
        
        JsonPath extractor2 = response3.jsonPath();
        TevaAssert.assertEquals(extentTest, response3.getStatusCode(), 200, "Expected HTTP Response status code '200' because api didnt expire yet");
        TevaAssert.assertEquals(extentTest, extractor2.get("apiKey.grantAccessDate"), y, "grantAccessDate is correct");
	}
    
    public static String getDate(Calendar cal){
        return "" + cal.get(Calendar.DATE) +"/" +
                (cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.YEAR);
	}

}
