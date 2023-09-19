package adadsmonitor;

import java.time.Duration;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverService;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ADADSLink {

	private WebDriver driver;
	Boolean headless;
	Boolean hardcopy;

	ADADSLink(Boolean headless) {
		this.headless = headless;
	}

	void open() {
		FirefoxOptions options = new FirefoxOptions();
		if (headless)
			options.addArguments("-headless");

		FirefoxDriverService service = new GeckoDriverService.Builder().withLogOutput(System.out).build();

		driver = new FirefoxDriver(service, options);
		System.out.println("Browser started.");
		driver.get("https://adads.web.boeing.com/webapp/ADADS/Project/redirector.jsp");

		new WebDriverWait(driver, Duration.ofSeconds(90)).until(ExpectedConditions.elementToBeClickable(By.xpath(
				"/html/body/table/tbody/tr[2]/td/table/tbody/tr[1]/td[5]/table/tbody/tr/td/table/tbody/tr/td/center/div/a[3]")));

		System.out.println("Passed authentication.");

		driver.findElement(By.xpath(
				"/html/body/table/tbody/tr[2]/td/table/tbody/tr[1]/td[5]/table/tbody/tr/td/table/tbody/tr/td/center/div/a[3]"))
				.click();
		driver.findElement(By.xpath(
				"/html/body/table/tbody/tr[2]/td/table/tbody/tr[1]/td[5]/table/tbody/tr/td/table[1]/tbody/tr[6]/td/span/a"))
				.click();
		hardcopy = true;
	}

	void close() {
		driver.quit();
		System.out.println("Browser closed.");
	}

	ArrayList<String> switchAndQuery() {
		if (hardcopy) {
			driver.findElement(
					By.xpath("/html/body/table/tbody/tr[2]/td/table/tbody/tr[1]/td[1]/table[1]/tbody/tr[1]/td/span/a"))
					.click();
		} else {
			driver.findElement(
					By.xpath("/html/body/table/tbody/tr[2]/td/table/tbody/tr[1]/td[1]/table[1]/tbody/tr[2]/td/span/a"))
					.click();
		}
		hardcopy = !hardcopy;

		Document orderDoc = Jsoup.parseBodyFragment(driver.findElement(By.id("bodyTable")).getAttribute("outerHTML"));
		Element orderTable = orderDoc.getElementById("bodyTable");
		Elements orders = orderTable.getElementsByClass("data");

		ArrayList<String> array = new ArrayList<String>();

		for (Element order : orders) {
			if (hardcopy)
				array.add(
						order.select("table > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(11)").first().text());
			else
				array.add(
						order.select("table > tbody:nth-child(1) > tr:nth-child(2) > td:nth-child(7)").first().text());
		}

		return array;
	}

}
