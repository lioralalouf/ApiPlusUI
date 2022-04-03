package pageObjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class TermsPage extends BasePage {
	@FindBy(css = ".consenting-user-data.selected")
	private WebElement person;
	@FindBy(css = "section > button.button")
	private WebElement continuetBtn;
	@FindBy(css = ".no-bg-button")
	private WebElement cancelBtn;

	public TermsPage(WebDriver driver) {
		super(driver);
	}

	public void choosePerson(String text) {
		if (person.getText().contains(text)) {
			click(person);
		}
	}

	public void clickContinue() {
		click(continuetBtn);

	}

	public void clickCancel() {
		click(cancelBtn);
	}

}
