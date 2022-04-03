package tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import io.restassured.RestAssured;
import models.request.PartnerRequest;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import requests.RestAssuredOAuth;
import tests.dss.api.PartnerApiTestBase;
import utils.Utils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.UUID;

import static io.restassured.RestAssured.given;

public class UiBaseTest extends PartnerApiTestBase {
    protected String accessToken = "";
    protected String partnerID = "";
    protected String apiKey = "";
    protected final ObjectMapper objectMapper = new ObjectMapper();

    public WebDriver driver;
    private String hash;
    private String filename;

    @BeforeClass
    public void setup(ITestContext testContext) {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.get(Utils.readProperty("url"));
        testContext.setAttribute("WebDriver", this.driver);

        accessToken = RestAssuredOAuth.getToken();
    }

    public void takeScreenshot(ExtentTest extentTest) throws IOException {
        String screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);

        extentTest.pass("<b><font color=green>" + "Screenshot \uD83D\uDCF7 (" + driver.getCurrentUrl() + "):" + "</font></b>",
                MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot).build());
    }

    @BeforeMethod(alwaysRun = true)
    public void BeforeMethod(Method method) throws IOException {

        Class clazz = method.getDeclaringClass();
        String path = clazz.getPackage().getName().replaceAll("\\.", "/");

        String currentDirectory = System.getProperty("user.dir");
        this.filename = currentDirectory + "/src/test/java/" + path + "/" + clazz.getSimpleName() + ".java";
        ProcessBuilder processBuilder = new ProcessBuilder();
        StringBuilder output = new StringBuilder();

        processBuilder.command("git", "hash-object", filename);

        Process process = null;
        process = processBuilder.start();

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        String line;

        while ((line = reader.readLine()) != null) {
            output.append(line);
            output.append(System.getProperty("line.separator"));
        }

        System.out.println("Filename: " + filename);
        System.out.println("Hash: " + output);

        this.hash = output.toString();

        Allure.addAttachment("Filename: ", filename);
        Allure.addAttachment("Hash: ", output.toString());

    }


   // @AfterClass
    public void tearDown() {
        driver.quit();
    }


    public void embedScreenshot(ITestResult result) {
        TakesScreenshot ts = (TakesScreenshot) driver;
        File srcFile = ts.getScreenshotAs(OutputType.FILE);
        try {

            FileUtils.copyFile(srcFile, new File("./ScreenShots/" + result.getName() + ".jpg"));
            try (InputStream is = FileUtils.openInputStream(srcFile)) {
                Allure.addAttachment("Screenshot", is);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String createPartnerShortcut() throws IOException {
        String accessToken = RestAssuredOAuth.getToken();

        PartnerRequest partnerRequest = objectMapper.readValue(Utils.readRequest("partner", "newPartner"),
                PartnerRequest.class);
        partnerRequest.name = UUID.randomUUID().toString();
        RestAssured.baseURI = Utils.readProperty("adminUrl");

        String response = given()
                .header("Authorization", "Bearer " + accessToken).header("Content-Type", "application/json").request()
                .body(partnerRequest).when().log().all().post("/configuration/partners").then().log().all().assertThat()
                .statusCode(200).extract().response().asString();
        return response;

    }


}
