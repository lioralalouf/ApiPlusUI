package pageObjects;

import java.time.Duration;

import javax.swing.text.html.CSS;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FailureScreenPage extends BasePage {
	@FindBy(css = "app-data-consent > div > section > h1")
	private WebElement errorTitle;
	
	public FailureScreenPage(WebDriver driver) {
		super(driver);
	}
	
	public String getError() {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-data-consent > div > section > h1")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("app-data-consent > div > section > h1")));
		return getText(errorTitle);
	}
	
}
