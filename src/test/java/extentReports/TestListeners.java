package extentReports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import tests.UiBaseTest;
import utils.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;


public class TestListeners implements ITestListener {

    private static ExtentReports extent = ExtentManager.createInstance();
    private static ThreadLocal<ExtentTest> extentTest = new ThreadLocal<ExtentTest>();


    public void onTestStart(ITestResult result) {

        StringBuilder testName = new StringBuilder();
        Class c = result.getTestClass().getRealClass();

        String methodName = result.getName();
        Method m = ReflectionUtils.getMethodByName(c, methodName);
        Test testAnnotation = m.getAnnotation(Test.class);
        testName = new StringBuilder(testAnnotation.testName());

        if (result.getParameters() != null) {
            for (int i = 0; i < result.getParameters().length; i++) {
                testName.append("[").append(result.getParameters()[i]).append("]");
            }
        }

        ExtentTest test = extent.createTest(testName.toString());
        extentTest.set(test);
        ExtentManager.registerTest(testName.toString(), test);
    }

    public void onTestSuccess(ITestResult result) {

    }

    public void onTestFailure(ITestResult result) {

        String methodName = result.getMethod().getMethodName();

        if (result.getInstance() instanceof UiBaseTest) {
            WebDriver driver = ((UiBaseTest) result.getInstance()).driver;
            String path = takeScreenShot(driver, result.getMethod().getMethodName());

            try {
                extentTest.get().fail("<b><font color=red>" + "Screenshot Of Failure:" + "</font></b>",
                        MediaEntityBuilder.createScreenCaptureFromPath(path).build());

            } catch (IOException e) {
                extentTest.get().fail("Test Failed, cannot attach screenshot");
            }
        }

        String logText = "<b>Test Method " + methodName + " Failed</b>";
        Markup m = MarkupHelper.createLabel(logText, ExtentColor.RED);
        extentTest.get().log(Status.FAIL, m);
    }


    public void onTestSkipped(ITestResult result) {
        String logText = "<b>Test Method " + result.getMethod().getMethodName() + "Skipped</b>";
        Markup m = MarkupHelper.createLabel(logText, ExtentColor.YELLOW);
        extentTest.get().log(Status.SKIP, m);

    }

    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }

    public String takeScreenShot(WebDriver driver, String methodName) {
        String fileName = getScreenshotName(methodName);
        String directory = System.getProperty("user.dir") + "/screenshots/";
        new File(directory).mkdirs();
        String path = directory + fileName;

        try {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshot, new File(path));
            System.out.println("********************************");
            System.out.println("Screenshot stored at: " + path);
            System.out.println("********************************");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    public static String getScreenshotName(String methodName) {
        Date d = new Date();
        String fileName = methodName + "_" + d.toString().replace(":", "_").replace(" ", " ") + ".png";
        return fileName;

    }

}
