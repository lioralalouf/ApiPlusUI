package pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

import java.util.List;

public class VendorMockTool extends BasePage {
	// Buttons
	@FindBy(css = ".flex.justify-between.text-lg>button")
	private WebElement addWorkSPaceBtn;
	@FindBy(css = "[placeholder='Workspace name']")
	private WebElement workSpaceIModifyName;
	@FindBy(css = "#confirm-create-button")
	private WebElement confirmDeleteBtn;
	@FindBy(css = ".h-12.w-full")
	private WebElement modifyWorkSpaceInput;
	@FindBy(css = "#confirm-create-button")
	private WebElement createBtn;
	@FindBy(css = ".flex.flex-col.justify-center.items-center a")
	private WebElement firstProjectBtn;
	@FindBy(css = ".hidden:nth-child(2)")
	private WebElement createMoreProjectsBtn;
	@FindBy(css = "[data-icon='chevron-down']")
	private WebElement modifyWorkSpaceBtn;
	@FindBy(css = ".ml-auto button:nth-child(1)")
	private WebElement cancelButton;
	@FindBy(css = ".fas.fa-caret-down")
	private WebElement accountDropBtn;
	@FindBy(css = "[data-icon='search']")
	private WebElement searchInput;
	@FindBy(css = "div>input")
	private WebElement searchField;
	@FindBy(css = ".items-center h1")
	private WebElement workspaceTitle;
	@FindBy(css = ".dropdown-menu>li:nth-child(1) > button")
	private WebElement renameButton;
	@FindBy(css = ".dropdown-menu>li:nth-child(2) > button")
	private WebElement deleteButton;

	// dropdown
	@FindBy(css = ".form-select.ml-auto")
	private WebElement sortContainer;

	// Lists
	@FindBy(css = ".leading-tight.text-lg.font-medium a")
	private List<WebElement> listOfProjects;
	@FindBy(css = ".mr-3.truncate")
	private List<WebElement> listOfWorkSpaces;
	@FindBy(css = ".dropdown-menu.w-auto.whitespace-no-wrap.min-w-full button")
	private List<WebElement> listOfWorkSpacesOptions;
	@FindBy(css = ".dropdown-item")
	private List<WebElement> accountDropList;

	public VendorMockTool(WebDriver driver) {
		super(driver);
	}
	// add new workSpace
	public void addNewWorkSpace(String text) {
		sleep(1000);
		click(addWorkSPaceBtn);
		fillText(workSpaceIModifyName, text);
		click(createBtn);
	}
	public boolean checkIfWorkspaceExist(String name) {
		sleep(1500);
		for (WebElement el : listOfWorkSpaces) {
			if (getText(el).equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
	}
	// method 1 for choosing from list of workSpaces - Not Working
	public void chooseWorkSpace(String name) {
		for (WebElement el : listOfWorkSpaces) {
			String workSpaceName = getText(el);
			if (workSpaceName.equalsIgnoreCase(name)) {
				sleep(2000);
				click(el);
				break;
			}
		}
	}
	// choosing from list of workSpaces
	public void clickOnChosenWorkSpace(String text) {
		sleep(1500);
		for (WebElement el : listOfWorkSpaces) {
			String workspaceName = getText(el);
			if (workspaceName.equalsIgnoreCase(text)) {
				sleep(1500);
				click(el);
				break;
			}
		}
	}
	public String getWorkspaceTitle() {
		return getText(workspaceTitle);
	}
	// rename WorkSpace
	public void renameWorkSpace(String newName) {
		click(modifyWorkSpaceBtn);
		click(renameButton);
		sleep(1500);
		modifyWorkSpaceInput.clear();
		fillText(modifyWorkSpaceInput, newName);
		click(createBtn);
	}
	// delete WorkSpace phase1 - fill name
	public void fillDeleteWorkSpace(String name) {
		click(modifyWorkSpaceBtn);
		click(deleteButton);
		fillText(modifyWorkSpaceInput, name);
	}
	// delete workspace phase2 - confirm delete
	public void clickDeleteWorkSpace() {
		click(confirmDeleteBtn);
	}
	// click cancel
	public void cancelDelete() {
		click(cancelButton);
	}
	// check if confirm delete button is clickable
	public boolean checkDeleteButtonClickable() {
		Boolean clickable = createBtn.isEnabled();
		return clickable;
	}
	// get list of buttons for delete and rename work Space -Not Working
	public void modifyWorkPlace(String input, String option) {
		for (WebElement el : listOfWorkSpacesOptions) {
			click(modifyWorkSpaceBtn);
			switch (option) {
			case "rename":
				click(renameButton);
				alertOK(input);
				break;
			case "delete":
				click(deleteButton);
				alertOK(input);
				break;
			default:
				break;
			}
		}
	}
	// sort by - workSpace projects
	public void tasksSortBy(String sort) {
		Select select = new Select(sortContainer);
		select.selectByValue((sort));
		sleep(1000);
	}
	// click on create first project button
	public void creatMyFirstProject() {
		sleep(1000);
		click(firstProjectBtn);
	}
	// click on create more projects button
	public void createMoreProjects() {
		sleep(1000);
		click(createMoreProjectsBtn);
	}
	// return Number of projects in workSpace
	public int numberOfProjects() {
		return listOfProjects.size();
	}
	// print projects name
	public void printProjectName() {
		for (WebElement el : listOfProjects) {
			String projectName = getText(el);
			System.out.println(listOfProjects);
			System.out.println(projectName);
		}
	}
	public String getFirstProject(int location) {
		List<WebElement> list = driver.findElements(By.cssSelector(".leading-tight.text-lg.font-medium a"));
        //using regular for, to iterate over the list and print all elements
		for (int i = 0; i < list.size(); i++) {
		}
		return list.get(location).getText();
	}
	// clicking create project button based of number of projects
	public void createNewProjectCondition() {
		if (numberOfProjects() == 0) {
			click(createMoreProjectsBtn);
		} else {
			click(firstProjectBtn);
		}
	}
	public String getProjectsPageTitle() {
		return getTitle();
	}
	public void ChooseAccountOption(String name) {
		click(accountDropBtn);
		sleep(1500);
		for (WebElement el : accountDropList) {
			String option = getText(el);
			if (option.equalsIgnoreCase(name)) {
				click(el);
				break;
			}
		}
	}
	public boolean searchingProject(String textInput, String textFound) {
		click(searchInput);
		sleep(1500);
		fillText(searchField, textInput);
		sleep(1500);
		for (WebElement el : listOfProjects) {
			if (el.getText().contains(textFound))  {
				break;
			}
			System.out.println(textFound);
			return true;
		}
		System.out.println(textFound);
		return false;
	}
	public boolean checkIfSearchDisplayed(String textInput) {
		click(searchInput);
		fillText(searchInput, textInput);
		sleep(1500);
		for (WebElement el : listOfProjects) {
			if (el.isDisplayed()) {
			}
			return true;
		}
		return false;
	}
	public void clearSearchInput() {
		searchInput.clear();
	}
}
