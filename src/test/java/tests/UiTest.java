package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;
import pageObjects.PartnerLoginPage;
import pageObjects.VendorToolPage;

public class UiTest extends UiBaseTest {
	@Severity(SeverityLevel.CRITICAL)
	@Story("As A partner, I supposed to be able fill patient id and valid api key and redirected to digiheler login page")
	@Test(description = "check that after pressing connect button, im redirected to digiheler login page")
	@Description("insert patient id + valid api key and press on connect button")
	public void tc01_validLogin() {
		VendorToolPage vt = new VendorToolPage(driver);
		vt.login("123456", "TtQWT5bvdh9UFTJtxvQUy34D2jFqUfVC4CO7tHZN");
		PartnerLoginPage dl = new PartnerLoginPage(driver);
		String actual = dl.getDigihelerPageTitle();
		System.out.println(dl.getDigihelerPageTitle());
		//Assert.assertEquals(actual, dl.getDigihelerPageTitle());
	}
}
