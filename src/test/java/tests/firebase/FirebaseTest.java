package tests.firebase;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import pageObjects.FireBasePage;
import service.JobsService;
import service.MedicationEventService;
import service.ProfileAccessService;
import service.PushNotificationHistoryService;
import utils.DateUtils;
import utils.Utils;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FirebaseTest {

    public WebDriver driver;
    private JobsService jobsService = new JobsService();
    private ProfileAccessService profileAccessService = new ProfileAccessService();
    private PushNotificationHistoryService userNotificationService = new PushNotificationHistoryService();
    private MedicationEventService medicationEventService = new MedicationEventService();
    private FirebaseInterceptor firebaseInterceptor;

    protected String firebaseToken;
    protected String firebaseMesage;
    protected String firebaseMessages;
    protected String filename;
    protected String hash;
    protected FireBasePage fireBasePage;

    @BeforeClass
    public void setup(ITestContext testContext) throws InterruptedException {
        firebaseInterceptor = new FirebaseInterceptor();
        firebaseInterceptor.start();


        WebDriverManager.chromedriver().setup();

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 1);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
        driver.get(Utils.readProperty("firebaseUrl"));
        testContext.setAttribute("WebDriver", this.driver);

        fireBasePage = new FireBasePage(driver);
        this.firebaseToken = fireBasePage.getToken();
        Assert.assertNotNull(firebaseToken);
    }

    @BeforeMethod(alwaysRun = true)
    public void BeforeMethod(Method method) throws IOException, InterruptedException {

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

    @AfterMethod
    public void embedScreenshot(ITestResult result)  {
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

    @AfterClass
    public void tearDown() {
    //    driver.close();
    //    driver.quit();


    }



    public String  getRunDay() {

        int now = DateUtils.utcDay();
        String adjustDay = "";

        switch (now) {
            case 0:
                adjustDay = "sun";
                break;
            case 1:
                adjustDay = "mon";
                break;
            case 2:
                adjustDay = "tue";
                break;
            case 3:
                adjustDay = "wed";
                break;
            case 4:
                adjustDay = "thu";
                break;
            case 5:
                adjustDay = "fri";
                break;
            case 6:
                adjustDay = "sat";
                break;
        }

        return adjustDay;
    }

    public String  getRunDayNegative() {

        int now = DateUtils.utcDay();

        now++;

        if (now > 6) {
            now = 0;
        }

        String adjustDay = "";

        switch (now) {
            case 0:
                adjustDay = "sun";
                break;
            case 1:
                adjustDay = "mon";
                break;
            case 2:
                adjustDay = "tue";
                break;
            case 3:
                adjustDay = "wed";
                break;
            case 4:
                adjustDay = "thu";
                break;
            case 5:
                adjustDay = "fri";
                break;
            case 6:
                adjustDay = "sat";
                break;
        }

        return adjustDay;
    }

    public void resetPatientTz() {

    }

    public void invokeJobLambda(String functionName) {

    }

    public String getFirebaseToken() {
        return this.firebaseToken;
    }

    public Object getLambdaVariables(String function) {
        return null;
    }

    public void adjustLambdaVariables(String functionName, Object lambdaConfiguration) {

    }

    protected void getFirebaseMessages() throws InterruptedException {

    }
}
