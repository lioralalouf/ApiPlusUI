package extentReports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DemoTest {
	public ExtentHtmlReporter htmlReporter;
	public ExtentReports extent;
	public ExtentTest extentTest;
	public WebDriver driver;

	@BeforeClass
	public void beforeClass() {
		/*htmlReporter = new ExtentHtmlReporter("./reports/extent.html");
		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setDocumentTitle("Automation Reports");
		htmlReporter.config().setReportName("Automation Test Results");
		htmlReporter.config().setTheme(Theme.STANDARD);

		extent = new ExtentReports();
		extent.setSystemInfo("Organization", "Lets cose it");
		extent.setSystemInfo("Browser", "Chrome");
		extent.attachReporter(htmlReporter);}*/
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
	}
	
	

	@Test
	public void testSuccessful() {
		driver.get("https://www.letskodeit.com");
		System.out.println("this is succsessful test");
		//extentTest = extent.createTest("Successful Test");
		//extentTest.log(Status.PASS, "test method successful");
	}

	@Test
	public void testFailed() {
		driver.get("https://www.letskodeit.com");
		System.out.println("this is failed test");
		//extentTest = extent.createTest("Failed Test");
		//extentTest.log(Status.FAIL, "test method successful");
		Assert.fail("Executing Failed Test Method");
	}

	@Test
	public void testSkipped() {
		driver.get("https://www.letskodeit.com");
		System.out.println("this is skipped test");
		//extentTest = extent.createTest("Skipped Test");
		//extentTest.log(Status.SKIP, "test method successful");
		throw new SkipException("Executing Skipped Test Method");
	}

	@AfterClass
	public void afterClass() {
		driver.quit();
		//extent.flush();
	}

}
