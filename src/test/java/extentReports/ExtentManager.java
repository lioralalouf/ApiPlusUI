package extentReports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.Test;
import annotations.Traceability;
import utils.ReflectionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ExtentManager {

	private static ExtentReports extentReports;
	private static Map<String, ExtentTest> testMap = new HashMap<>();
	private static Map<String, Boolean> reportHydrated = new HashMap<>();
	private static WebDriver driver;

	public static ExtentReports createInstance() {

		String fileName = getReportName();
		String directory = System.getProperty("user.dir") + "/reports/";
		new File(directory).mkdirs();
		String path = directory + fileName;
		ExtentHtmlReporter htmlReporter = new ExtentHtmlReporter(path);

		htmlReporter.config().setEncoding("utf-8");
		htmlReporter.config().setDocumentTitle("Automation Reports");
		htmlReporter.config().setReportName("Automation Test Results");
		htmlReporter.config().setTheme(Theme.STANDARD);
		htmlReporter.config().hashCode();

		extentReports = new ExtentReports();
		extentReports.setSystemInfo("Organization", "Teva Pharmaceuticals");
		extentReports.setSystemInfo("Browser", "Chrome");

		extentReports.attachReporter(htmlReporter);

		return extentReports;
	}

	public static String getReportName() {
		Date d = new Date();
		String fileName = "AutomationReport" + "_" + d.toString().replace(":", "_").replace(" ", " ") + ".html";
		return fileName;
	}

	public static void registerTest(String testName, ExtentTest extentTest) {
		testMap.put(testName, extentTest);
	}

	public static ExtentTest getTest(Class clazz, Object obj) throws IOException {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String methodName = stackTraceElements[2].getMethodName();
		Method m = ReflectionUtils.getMethodByName(clazz, methodName);

		assert m != null;
		Test testAnnotation = m.getAnnotation(Test.class);
		String testName = testAnnotation.testName();

		if (obj != null) {
			testName += "[" + obj + "]";
		}



		ExtentTest extentTest = testMap.get(testName);

		Traceability traceability = m.getAnnotation(Traceability.class);

		if (traceability != null && traceability.URS() != null && traceability.URS().length > 0 && traceability.FS().length > 0) {
			extentTest.info("Filename: " + stackTraceElements[2].getFileName() + " URS: " + Arrays.toString(traceability.URS()) + " FS: " + Arrays.toString(traceability.FS()));
		}

		if (traceability != null && traceability.URS() != null && traceability.FS().length == 0 && traceability.URS().length > 0) {
			extentTest.info("Filename: " + stackTraceElements[2].getFileName() + " URS: " + Arrays.toString(traceability.URS()));
		}

		if (traceability != null && traceability.URS() != null && traceability.FS().length > 0 && traceability.URS().length == 0) {
			extentTest.info("Filename: " + stackTraceElements[2].getFileName() + " FS: " +  Arrays.toString(traceability.FS()));
		}

		associateHashWithTest(testName, extentTest, m);

		return extentTest;
	}

	public static ExtentTest getTest(Class clazz) throws IOException {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		String methodName = stackTraceElements[2].getMethodName();
		Method m = ReflectionUtils.getMethodByName(clazz, methodName);

		Test testAnnotation = m.getAnnotation(Test.class);
		String testName = testAnnotation.testName();

		ExtentTest extentTest = testMap.get(testName);
		associateHashWithTest(testName, extentTest, m);

		Traceability traceability = m.getAnnotation(Traceability.class);

		if (traceability != null && traceability.URS() != null && traceability.URS().length > 0 && traceability.FS().length > 0) {
			extentTest.info("Filename: " + stackTraceElements[2].getFileName() + " URS: " + Arrays.toString(traceability.URS()) + " FS: " + Arrays.toString(traceability.FS()));
		}

		if (traceability != null && traceability.URS() != null && traceability.FS().length == 0 && traceability.URS().length > 0) {
			extentTest.info("Filename: " + stackTraceElements[2].getFileName() + " URS: " + Arrays.toString(traceability.URS()));
		}

		if (traceability != null && traceability.URS() != null && traceability.FS().length > 0 && traceability.URS().length == 0) {
			extentTest.info("Filename: " + stackTraceElements[2].getFileName() + " FS: " +  Arrays.toString(traceability.FS()));
		}

		return extentTest;
	}
	
	private static void associateHashWithTest(String testName, ExtentTest extentTest, Method method)
			throws IOException {

		Class clazz = method.getDeclaringClass();
		String path = clazz.getPackage().getName().replaceAll("\\.", "/");

		String currentDirectory = System.getProperty("user.dir");
		String filename = currentDirectory + "/src/test/java/" + path + "/" + clazz.getSimpleName() + ".java";

		ProcessBuilder processBuilder = new ProcessBuilder();
		StringBuilder output = new StringBuilder();

		processBuilder.command("git", "hash-object", filename);

		Process process = null;
		process = processBuilder.start();

		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

		String line;

		while ((line = reader.readLine()) != null) {
			output.append(line);
			output.append(System.getProperty("line.separator"));
		}

		filename = filename.replaceAll("\\\\", "/");

		if (!reportHydrated.containsKey(filename)) {
			extentTest.getExtent().setSystemInfo("Hash", output.toString());
			extentTest.getExtent().setSystemInfo("Filename", filename);
			reportHydrated.put(filename, true);
		}

	}

}
