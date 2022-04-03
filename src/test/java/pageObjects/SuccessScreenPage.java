package pageObjects;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import utils.Utils;

public class SuccessScreenPage extends BasePage{
	@FindBy(css = "app-data-transfer-icons > div > div > img:nth-child(1)")
	private WebElement icon;
	@FindBy(css = "app-data-consent > div > section > p")
	private WebElement title;
	@FindBy(css = "app-data-consent > div > section > h1")
	private WebElement header;
	@FindBy(css = "app-data-consent > div > section > button")
	private WebElement doneBtn;
	
	public SuccessScreenPage(WebDriver driver) {
		super(driver);
	}
	
	public String getIconPath() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-data-transfer-icons > div > div > img:nth-child(1)")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("app-data-transfer-icons > div > div > img:nth-child(1)")));
		String path = icon.getAttribute("src");
		System.out.println(path);
		return path;
	}
	
	public String ReplaceTitleString(String replace1, String replace2) {
		String s1 = Utils.readProperty("succesText")
        .replace("$", replace1)
        .replace("@", replace2);
		return s1;
	}
	
	public String getHeader() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-data-consent > div > section > h1")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("app-data-consent > div > section > h1")));
		return getText(header);
	}
	
	public String getTitle() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-data-consent > div > section > p")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("app-data-consent > div > section > p")));
		return getText(title);
	}
	
	public void clickDone() {
		sleep(3000);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-data-consent > div > section > button")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("app-data-consent > div > section > button")));
		click(doneBtn);
	}
}
