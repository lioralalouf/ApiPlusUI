package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class VendorToolPage extends BasePage {
	@FindBy(css = "#partner-connect")
	private WebElement partnerConnectBtn;
	@FindBy(css = "#digihaler-connect")
	private WebElement digihalerConnectBtn;
	@FindBy(css = "[name='patient-id']")
	private WebElement patientIdField;
	@FindBy(css = "[name='api-key']")
	private WebElement apiKeyField;
	@FindBy(css = "app-vendor-lp > div > div > h1")
	private WebElement title;

	public VendorToolPage(WebDriver driver) {
		super(driver);
	}

	// fill in patient id and api key and press connect
	public void login(String patientId, String apiKey) {
		fillText(patientIdField, patientId);
		sleep(2000);
		fillText(apiKeyField, apiKey);
		click(partnerConnectBtn);
	}

	public void clearLogin() {

	}

	public void clickDigihalerConnectButton() {
		click(digihalerConnectBtn);
	}
	
	public String getTitle() {
		@SuppressWarnings("deprecation")
		WebDriverWait wait = new WebDriverWait(driver, 10);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-vendor-lp > div > div > h1")));
		return getText(title);
		}
	}


