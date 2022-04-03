package pageObjects;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class BasePage {
	WebDriver driver;
	Actions actions;
	JavascriptExecutor js; 

	public BasePage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
		actions = new Actions(driver);
		js = (JavascriptExecutor) driver;
	}

	protected void fillText(WebElement el, String text) {
		highlightElement(el, "black", "blue");
		sleep(500);
		el.clear();
		el.sendKeys(text);
	}

	protected void scrollMouse(WebElement el) {
		//to perform Scroll on application using Selenium
		js.executeScript("arguments[0].scrollIntoView();", el);
	}

	protected void moveTo(WebElement el) {
		actions.moveToElement(el).build().perform();
		sleep(2000);
	}

	protected void sleep(long mills) {
		try {
			Thread.sleep(mills);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void click(WebElement el) {
		highlightElement(el, "black", "yellow");
		sleep(1000);
		el.click();
	}

	protected String getText(WebElement el) {
		highlightElement(el, "black", "orange");
		return el.getText();

	}

	protected void alertOK(String text) {
		driver.switchTo().alert().sendKeys(text);
		driver.switchTo().alert().accept();

	}
    public void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

	protected void alertOK() {
		driver.switchTo().alert().accept();
	}

	protected String getAttribute(WebElement el, String key) {
		return el.getAttribute(key);

	}

	protected String getTitle() {
		String actualTitle = driver.getTitle();
		return actualTitle;

	}
	
	/*
	 * Call this method with your element and a color like (red,green,orange etc...)
	 */
	protected void highlightElement(WebElement element, String color, String backgroundColor) {
		// keep the old style to change it back
		String originalStyle = element.getAttribute("style");
		String newStyle = "background-color:" + backgroundColor + "; border: 2px solid " + color + ";" + originalStyle;
		JavascriptExecutor js = (JavascriptExecutor) driver;

		// Change the style
		js.executeScript("var tmpArguments = arguments;setTimeout(function () {tmpArguments[0].setAttribute('style', '"
				+ newStyle + "');},0);", element);

		// Change the style back after few miliseconds
		js.executeScript("var tmpArguments = arguments;setTimeout(function () {tmpArguments[0].setAttribute('style', '"
				+ originalStyle + "');},400);", element);
	}

}
 
