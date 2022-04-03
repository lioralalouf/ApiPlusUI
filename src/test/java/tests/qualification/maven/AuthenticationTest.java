package tests.qualification.maven;

import com.aventstack.extentreports.ExtentTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import extentReports.ExtentManager;
import extentReports.TestListeners;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import requests.RestAssuredOAuth;
import annotations.Traceability;
import utils.TevaAssert;

import java.io.IOException;

@Listeners(TestListeners.class)
public class AuthenticationTest {
    private String accessToken = "";
    private String partnerID = "";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test(testName = "Requesting an access token returns an access token.")
    @Traceability(URS = {"1733"})
    public void tc01_Authentication_request() throws IOException, InterruptedException {
        ExtentTest extentTest = ExtentManager.getTest(this.getClass());
        this.accessToken = RestAssuredOAuth.getToken();
        TevaAssert.assertNotNull(extentTest, this.accessToken, "Valid access token returned proving that the tool can be used for testing APIs which require authentication.");
        Thread.sleep(2000);
    }
}
