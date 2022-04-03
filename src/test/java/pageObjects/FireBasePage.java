package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class FireBasePage extends BasePage {

    @FindBy(id = "token")
    private WebElement token;

    public FireBasePage(WebDriver driver) {
        super(driver);
    }

    public String getToken() {
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("token")));
        return getText(token);
    }
}
