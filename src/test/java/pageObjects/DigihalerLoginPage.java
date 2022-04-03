package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class DigihalerLoginPage extends BasePage {

    @FindBy(name = "page:form:username")
    private WebElement usernameField;
    @FindBy(name = "page:form:password")
    private WebElement passwordField;
    @FindBy(xpath = "//*[@id=\"page:form:loginButton\"]")
    private WebElement loginBtn;
    @FindBy(css = "#login-text")
    private WebElement loginTxt;
    @FindBy(css = "#idh-errors > div:nth-child(1)")
    private WebElement emptyFieldError;
    @FindBy(css = "#idh-errors > div:nth-child(2)")
    private WebElement invalidCredError;
 

    public DigihalerLoginPage(WebDriver driver) {
        super(driver);

    }
    
    public String getEmptyFieldErrorMsg() {
		return getText(emptyFieldError);
    }
    
    public String getInvalidCredErrorMsg() {
		return getText(invalidCredError);
    }

    public String login(String username, String password) {
        fillText(usernameField, username);
        fillText(passwordField, password);
        click(loginBtn);
    //    WebDriverWait wait = new WebDriverWait(driver, 30);
    //    wait.until(ExpectedConditions.titleIs("localhost"));
        return driver.getCurrentUrl();
    }

    public void clickLoginNoCred() {
    	click(loginBtn);
    }

    public boolean checkTitle() {
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("page:form:loginButton")));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("page:form:loginButton")));
        if (loginTxt.isDisplayed()) {
            return true;
        }
        return false;
    }

    public void checkLoginButton() {
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"page:form:loginButton\"]")));
    }
}
