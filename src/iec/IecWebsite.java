package iec;

import java.io.File;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.WebDriverRunner;

public class IecWebsite {
	
    private static IecWebsite instance;

    private IecWebsite() {

    }

    static {
        instance = new IecWebsite();
        instance.username = "";
        instance.password = "";
        Configuration.startMaximized = true;
		Selenide.open(IecWebsite.LOGIN_URL);
    }

    public static IecWebsite getInstance() {
        return instance;
    }
	
    public static final String LOGIN_URL = "https://5b.mgipu.hr/mgipuiec";
    
    private String username;
    
    private String password;
    
    public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
	public boolean login(String username, String password) {
		Selenide.open(LOGIN_URL);
		SelenideElement usernameInput = Selenide.$(By.id("username"));
		SelenideElement passwordInput = Selenide.$(By.id("password"));
		SelenideElement submitButton = Selenide.$(By.xpath("/html/body/div/div/div/form/button"));
		if (!username.isEmpty() && !password.isEmpty() && !usernameInput.isDisplayed() && !passwordInput.isDisplayed()) {
			return true;
		}
		
    	usernameInput.setValue(username);
    	passwordInput.setValue(password);
    	submitButton.click();
    	try {
    		Thread.sleep(1000);
    	} catch (Exception e) {}
    	if (WebDriverRunner.source().contains("Neispravno korisničko ime ili lozinka")) {
    		return false;
    	}
    	
    	SelenideElement span = Selenide.$(By.xpath("//*[@id=\"content-wrapper\"]/div/div/div/div/div/div[1]/div[1]/idom-include/div/div[1]/div[3]/span"));
    	span.waitUntil(Condition.visible, Utility.LONG_PAGE_WAIT);
    	span.waitWhile(Condition.matchesText("Nema stavki za prikaz"), Utility.LONG_PAGE_WAIT);
    	this.username = username;
    	this.password = password;
    	return true;
    }

	public File downloadExcelValueFile(String oznakaCertifikata) throws Exception {
		SelenideElement sortInput = Selenide.$(By.xpath("//*[@id=\"content-wrapper\"]/div/div/div/div/div/div[1]/div[1]/idom-include/div/div[1]/div[1]/div/table/thead/tr[2]/th[2]/span/span/span/input"));
		sortInput.setValue(oznakaCertifikata).pressEnter();
		Thread.sleep(3000);
		SelenideElement spanElement = Selenide.$(By.xpath("//*[@id=\"content-wrapper\"]/div/div/div/div/div/div[1]/div[1]/idom-include/div/div[1]/div[3]/span"));
		String spanElementValue = spanElement.getText();
		if (spanElementValue.equals("Nema stavki za prikaz")) {
			return null;
		}
		
		File currentLatestFile = Utility.getLatestFilefromDir(Configuration.downloadsFolder);
		SelenideElement excelDownloadButton = Selenide.$(By.xpath("//*[@id=\"content-wrapper\"]/div/div/div/div/div/div[1]/div[1]/idom-include/div/div[1]/div[4]/idom-button/button"));
		excelDownloadButton.click();
		File certificateExcel = Utility.awaitForNewFile(currentLatestFile, Configuration.downloadsFolder);
		return certificateExcel;
	}
    
}
