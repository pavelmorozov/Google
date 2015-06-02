package googlescraper.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * This class scrapes Google search results and put them to queue. Queue stores
 * DataItem object
 * @author Pavlo Morozov
 */
public class GoogleScraper implements Runnable {

	BlockingQueue<DataItem> sitesToScrapeQue;
	private String searchString = null;
	private Integer scrapeSites = null;

	public GoogleScraper(BlockingQueue<DataItem> sitesToScrapeQue,
			String searchString, Integer scrapeSites) {
		this.sitesToScrapeQue = sitesToScrapeQue;
		this.searchString = searchString;
		this.scrapeSites = scrapeSites;
	}

	/**
	 * This method search google and generates structure that contains
	 * ScrapedDataItems. ScrapedDataItem contains task and runnable method to
	 * scrape data from particular domain.
	 */
	@Override
	public void run() {
		System.out.println("=== Google scraper thread start ===");

		// WebDriver driver = new HtmlUnitDriver(true);
		WebDriver driver = new FirefoxDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
				Level.OFF);

		String startUrl = "http://www.google.com";
		// System.out.println("Start with: " + startUrl);
		driver.get(startUrl);

		driver.findElement(By.id("lst-ib")).sendKeys(searchString);
		driver.findElement(By.name("btnG")).click();

		// Collect links
		// Map<String, ScrapedDataItem> tasksToScrape = new HashMap<String,
		// ScrapedDataItem>();

		Integer pageIndex = 1;
		Integer processed = 0;
		while (processed < scrapeSites) {

			/*
			 * Check page number before scrape. Sometimes after click next page
			 * old page not replaced with new one yet, so I check current page
			 * index with pageIndex variable
			 */
			String currentIndexXPATH = "//*[contains(concat(' ', normalize-space(@class), ' '), ' cur ')]";
			// Wait page number filled
			final Integer i = new Integer(pageIndex);
			(new WebDriverWait(driver, 10))
					.until(new ExpectedCondition<Boolean>() {
						public Boolean apply(WebDriver d) {
							boolean processFlag = true;
							Integer currentPageNumber=0;
							String currentIndexString = "";
							while (processFlag) {
								processFlag = false;
								try {
									currentIndexString = d.findElement(
											By.xpath(currentIndexXPATH))
											.getText();
								} catch (StaleElementReferenceException e) {
									/*
									 * Page changed, process it once more
									 */
									processFlag = true;
								}
							}
							if (currentIndexString.length() == 0) {
								return false;
							}
							currentPageNumber = Integer
									.parseInt(currentIndexString);							
							return currentPageNumber.equals(i);
						}
					});

			/*
			 * Search results xPath
			 */
			String searchResultsXPATH = "//*[contains(concat(' ', normalize-space(@class), ' '), ' srg ')]";

			/*
			 * Particular link xPath
			 */
			String linksXPATH = searchResultsXPATH + "//h3/a";

			List<WebElement> linksList = driver.findElements(By
					.xpath(linksXPATH));

			/*
			 * Collect links from page to structure tasksToScrape
			 */
			for (WebElement link : linksList) {

				String descriptionAdd = link
						.findElement(
								By.xpath("../..//*[contains(concat(' ', normalize-space(@class), ' '), ' st ')]"))
						.getText();

				DataItem scrapedDataItem = new DataItem(
						link.getAttribute("href"), link.getText() + " "
								+ descriptionAdd);

				if (processed < scrapeSites) {
					try {
						sitesToScrapeQue.put(scrapedDataItem);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					processed++;
				} else {
					break;
				}

			}

			// Go to next search results page
			if (processed < scrapeSites) {
				try {
					driver.findElement(By.id("pnnext")).click();
					pageIndex++;
				} catch (NoSuchElementException e) {
					// if next button not found - stop search
					break;
				}
			} else {
				break;
			}

		}

		// last item with exit flag
		DataItem scrapedDataItem = new DataItem("exit", "exit");
		try {
			sitesToScrapeQue.put(scrapedDataItem);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		driver.quit();
	}
}
