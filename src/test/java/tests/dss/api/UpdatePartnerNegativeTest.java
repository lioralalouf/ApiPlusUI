package tests.dss.api;

import annotations.Traceability;
import com.aventstack.extentreports.ExtentTest;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import io.restassured.response.Response;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import requests.CreatePartnerTemp;
import utils.TevaAssert;

import java.io.IOException;

@Listeners(TestListeners.class)
public class UpdatePartnerNegativeTest extends PartnerApiTestBase {

    @Test(priority = 1, testName = "Update partner with invalid rate", dataProvider = "getLimits", dataProviderClass = models.DataProviders.class)
    @Traceability(FS = {"1600"})
    public void tc01_updatePartnerInvalidRate(int rate) throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), rate);

        extentTest.pass("This is a test with parameters");
        extentTest.info("Onboard a new partner with invalid rate");
        String partnerID = createPartner(extentTest);
        extentTest.info("Update partner with invalid rate");
        Response response = CreatePartnerTemp.updateInvalidRate(partnerID, rate);
        int code = response.getStatusCode();
        TevaAssert.assertEquals(extentTest, code, 400, "Request is expected to have HTTP Response Code `400`");
        cleanUp();
    }

    @Test(priority = 2, testName = "Update partner with invalid burst", dataProvider = "getLimits", dataProviderClass = models.DataProviders.class)
    @Traceability(FS = {"1600"})
    public void tc02_updatePartnerInvalidBurst(int burst) throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), burst);

        extentTest.pass("This is a test with parameters");
        extentTest.info("Onboard a new partner");
        String partnerID = createPartner(extentTest);
        extentTest.info("Update partner with invalid burst");
        Response response = CreatePartnerTemp.updateInvalidBurst(partnerID, burst);
        int code = response.getStatusCode();
        TevaAssert.assertEquals(extentTest, code, 400, "Request is expected to have HTTP Response Code `400`");
        cleanUp();
    }

    @Test(priority = 3, testName = "Update partner with invalid limit", dataProvider = "getLimits", dataProviderClass = models.DataProviders.class)
    @Traceability(FS = {"1600"})
    public void tc03_updatePartnerInvalidLimit(int limit) throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), limit);

        extentTest.pass("This is a test with parameters");
        extentTest.info("Onboard a new partner");
        String partnerID = createPartner(extentTest);
        extentTest.info("Update partner with invalid limit");
        Response response = CreatePartnerTemp.updateInvalidLimit(partnerID, limit);
        int code = response.getStatusCode();
        TevaAssert.assertEquals(extentTest, code, 400, "Request is expected to have HTTP Response Code `400`");
        cleanUp();
    }

    @Test(priority = 4, testName = "Update partner with invalid period", dataProvider = "getPeriodFalse", dataProviderClass = models.DataProviders.class)
    @Traceability(FS = {"1600"})
    public void tc04_updatePartnerInvalidPeriod(String period) throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass(), period);

        extentTest.pass("This is a test with parameters");
        extentTest.info("Onboard a new partner");
        String partnerID = createPartner(extentTest);
        extentTest.info("Update partner with invalid period");
        Response response = CreatePartnerTemp.updateInvalidPeriod(partnerID, period);
        int code = response.getStatusCode();
        TevaAssert.assertEquals(extentTest, code, 400, "Request is expected to have HTTP Response Code `400`");
        cleanUp();
    }
}
