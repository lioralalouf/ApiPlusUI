package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ConsentPage extends BasePage {
	@FindBy(css = "app-data-transfer-icons > div > div > img:nth-child(1)")
	private WebElement icon;
	@FindBy(css = "body > app-root > app-data-consent > div > section > h1")
	private WebElement title;
	@FindBy(css = ".user-name")
	private List<WebElement> usersNamesList;
	@FindBy(css = ".user-dob")
	private List<WebElement> usersDobList;
	@FindBy(css = ".no-bg-button")
	private WebElement cancelBtn;
	@FindBy(css = "div > section > button.button")
	private WebElement continueBtn;
	
	
	

	public ConsentPage(WebDriver driver) {
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
	public String getTitleText() {
		sleep(5000);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("body > app-root > app-data-consent > div > section > h1")));
		return getText(title);
	}
	public void choosePerson(String text) {
		sleep(4000);
		for (WebElement el : usersNamesList) {
			String userName = getText(el);
			if (userName.equalsIgnoreCase(text)) {
				sleep(2000);
				click(el);
				break;

			}
		}
	}
	public boolean checkPersonExist(String text) {
		sleep(3000);
		for (WebElement el : usersNamesList) {
			String personName = getText(el);
			if (personName.contains(text)) {
				sleep(2000);
				return true;
				

			}
		}
		return false;
	}
	public boolean checkPersonExist(String text, String text2) {
		sleep(3000);
		for (WebElement el : usersNamesList) {
			String userName = getText(el);
			for (WebElement el2 : usersDobList) {
				String userDob = getText(el2);
				if (userName.equalsIgnoreCase(text)) {
					if (userDob.equalsIgnoreCase(text2)) {
						sleep(2000);
						return true;
					}
				}
			}
		}
		return false;

	}
	
	public boolean checkPersonExistByName(String text) {
		sleep(3000);
		for (WebElement el : usersNamesList) {
			String userName = getText(el);
			
				if (userName.equalsIgnoreCase(text)) {
				
						return true;
					}
				}
		return false;
	}
		
	public void clickCancel() {
		click(cancelBtn);
	}
	
	public void clickContinue() {
		sleep(1500);
		click(continueBtn);
	}
	
	public boolean checkGardianIsFirst(String text) {
		sleep(3000);
		for (WebElement el : usersNamesList) {
			if (usersNamesList.get(0).getText().contains(text)) {
				return  true;
			}
		}
		return false;
			
		}
	public boolean checkUserIsNotDisplayed(String text) {
		for (WebElement el : usersNamesList) {
			String userName = getText(el);
			if (userName.contains(text)) {
				return true;
			}
		}
		return false;
	}
}
