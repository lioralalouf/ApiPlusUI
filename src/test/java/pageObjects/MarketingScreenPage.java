package pageObjects;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MarketingScreenPage extends BasePage {
	@FindBy(css = "section > div > h1")
	private WebElement title;
    @FindBy(css = ".doc-trigger.signature-doc")
    private WebElement signaturePolicyLink;
	@FindBy(css = ".checkbox-outer")
	private WebElement signatureCheckbox;
	@FindBy(css = ".authorization-doc.marketing-authorization-doc")
	private WebElement signatureTxt;
	@FindBy(css = ".close-modal")
	private WebElement closeSignatureDoc;
	@FindBy(css = "[name='signature']")
	private WebElement signatureField;
	@FindBy(css = ".name-and-date.clearfix span:nth-child(1)")
	private WebElement signatureName;
	@FindBy(css = ".name-and-date.clearfix span:nth-child(2)")
	private WebElement signatureDate;
	@FindBy(css = ".error-field")
	private WebElement errorMsg;
	@FindBy(css = ".button")
	private WebElement acceptBtn;
	@FindBy(css = ".no-bg-button")
	private WebElement skip;
	@FindBy(css = "app-data-consent > div > section > div > p")
	private WebElement marketingConsent;
	@FindBy(css = ".doc-container")
	private WebElement signaturePolicyDoc;


	public MarketingScreenPage(WebDriver driver) {
		super(driver);
	}
	
	public String checkTitle() {
		sleep(4000);
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("section > div > h1")));
		return getText(title);
	}
	
	public void getSignatueName() {
		getText(signatureName);
	}
	
    public void getSignatueDate() {
    	getText(signatureDate);
	}
    
    public String clickSignaturePolicy() {
    	click(signaturePolicyLink);
    	sleep(2000);
		return getText(signaturePolicyDoc);
    }
    
    public void closeSignatureDoc() {
    	click(closeSignatureDoc);	
    }
	
	public void sign(String text) {
		fillText(signatureField, text);
	}
	
	public void clearSignature() {
		sleep(2500);
		driver.findElement(By.cssSelector("[name='signature']")).clear();
	}
	
    public void clickSignatureCheckbox() {
		click(signatureCheckbox);
	}
    
    public String getSignatureName() {
		return getText(signatureName);
    	
    }
    
    public String getSignatureDate() {
		return getText(signatureDate);
    	
    }
    
    public void clickAccept() {
    	click(acceptBtn);
    }
    
    public void clickSkip() {
    	click(skip);
    }
    
    public String getErrorMsg() {
    	return getText(errorMsg);
    }
    
    public boolean CheckAcceptBtnIsClickable() {
		if (acceptBtn.isEnabled()) {
			return true;
		}
		return false;
	}
    
    public String getMarketingText() {
    	sleep(3500);
    	WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-data-consent > div > section > div > p")));
    	return getText(marketingConsent);
    }

}
