package pageObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import utils.Utils;

public class AuthorizationPage extends BasePage {
	@FindBy(css = "section > div > h1")
	private WebElement title;
	@FindBy(css = "app-data-consent > div > section > div > p")
	private WebElement authorizeTxt;
	@FindBy(css = "span:nth-child(1)")
	private WebElement termsOfUse;
	@FindBy(css = "span:nth-child(2)")
	private WebElement privecyNotice;
	@FindBy(css = "	app-data-consent > div > section > div > div.doc-container")
	private WebElement hippa;
	@FindBy(css = ".checkbox-outer img")
	private WebElement AgreeCheckbox;
	@FindBy(css = "div > button.button")
	private WebElement acceptBtn;
	//@FindBy(css = ".docs-viewer.animated")
	//private WebElement documentText;
	@FindBy(css = "div.docs-viewer-outer > div > div")
	private WebElement documentText;
	@FindBy(css = ".docs-viewer-outer > div > button")
	private WebElement docCloseBtn;
	@FindBy(css = "app-data-consent span:nth-child(1)")
	private WebElement termsLink;
	@FindBy(css = "app-data-consent span:nth-child(2)")
	private WebElement privacyLink;
	@FindBy(css = ".no-bg-button")
	private WebElement declineBtn;
	
	public AuthorizationPage(WebDriver driver) {
		super(driver);
	}
	
	public String getTermsOfUseText() {
		sleep(2000);
		click(termsLink);
		String text = getText(documentText);
		click(docCloseBtn);
		return text;
	}
	
	public String getHipaaTxt() {
		return getText(hippa);
	}
	
	public String getPrivacyNoticeText() {
		sleep(2000);
		click(privacyLink);
		String text = getText(documentText);
		click(docCloseBtn);
		return text;
	}
	
	public String ReplaceTitleString(String replace1, String replace2) {
		String s1 = Utils.readProperty("authorizationTitle")
        .replace("$", replace1)
        .replace("@", replace2);
		return s1;
	}
	
	public String getTitle() {
		return getText(authorizeTxt);
	}
	
	public void clickCheckbox() {
		click(AgreeCheckbox);
		sleep(1500);
	}
	
	public void clickAccept() {
		click(acceptBtn);;
	}
	
	public boolean CheckAcceptBtnIsClickable() {
		if (acceptBtn.isEnabled()) {
			return true;
		}
		return false;
	}
	
	public void clickDecline() {
		click(declineBtn);
	}
}
