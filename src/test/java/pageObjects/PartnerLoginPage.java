package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class PartnerLoginPage extends BasePage {
	@FindBy(css = "#logo")
	private WebElement logo;
	@FindBy(css = "#partner-text")
	private WebElement partnerText;
	@FindBy(css = "#idh-username")
	private WebElement usernameField;
	@FindBy(css = "#idh-password")
	private WebElement passwordField;
	@FindBy(css = "#idh-login-button")
	private WebElement loginBtn;
	@FindBy(css = "[alt='Show Password']")
	private WebElement showPassword;
	@FindBy(css = ".idh-var-username-invalid-error")
	private WebElement errorMsg;
	@FindBy(css = "#login-text")
	private WebElement loginTxt;
    @FindBy(css = "#idh-errors")
    private WebElement emptyFieldError;
    @FindBy(css = "#idh-errors")
    private WebElement invalidCredError;

	public PartnerLoginPage(WebDriver driver) {
		super(driver);
		// TODO Auto-generated constructor stub
	}
	
    public String getEmptyFieldErrorMsg() {
    	sleep(2000);
		return getText(emptyFieldError);
    }
    
    public String getInvalidCredErrorMsg() {
    	sleep(2000);
		return getText(invalidCredError);
    }

    public void clickLoginEmptyPassword(String username) {
    	fillText(usernameField, username);
    	click(loginBtn);
    	sleep(1500);
    }
    
    public void clickLoginEmptyUserName(String userName, String password) {
    	fillText(passwordField, password);
    	click(loginBtn);
    	sleep(1500);
    }

	// fill in email and password and press login
	public void login(String username, String password) {
		fillText(usernameField, username);
		fillText(passwordField, password);
		click(loginBtn);
		sleep(2500);
	}
	
	// fill in email and password and press login
	public void login2(String username, String password) {
		fillText(usernameField, username);
		fillText(passwordField, password);
		sleep(2500);
	}

	public void clickLogin2() {
		click(loginBtn);
	}

	public String getErrorMsg() {
		return getText(errorMsg);
	}

	public String gettextValue() {
		return driver.findElement(By.id("idh-password")).getCssValue("type");
	}

	public void clickShowPassword() {
		click(showPassword);
	}

	public String getDigihelerPageTitle() {
		@SuppressWarnings("deprecation")
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login-text")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#login-text")));
		System.out.println(getTitle());
		return getTitle();
	}

	public boolean checkTitle() {
		@SuppressWarnings("deprecation")
		WebDriverWait wait = new WebDriverWait(driver, 30);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#login-text")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#login-text")));
		if (loginTxt.isDisplayed()) {
			return true;
		}
		return false;

	}
}
