package googlescraper.engine;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.BrowserVersionFeatures;

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
		if ((searchString.length()==0)||(searchString==null)){
			return;
		}
		System.out.println("=== Google scraper thread start ===");
		//Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0
	      String applicationName = "FireFox";
	      String applicationVersion = "6.1 (Windows)";
	      String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0";
	      int browserVersionNumeric = 38;
	      BrowserVersion browser = new BrowserVersion(applicationName, applicationVersion, userAgent, browserVersionNumeric) {
	    	  @Override
	          public boolean hasFeature(BrowserVersionFeatures property) {
	              // change features here
	              return BrowserVersion.FIREFOX_24.hasFeature(property);
	          }
	      };
		WebDriver driver = new HtmlUnitDriver(browser);
		//WebDriver driver = new FirefoxDriver();
		
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
				Level.OFF);
		
		String startUrl = "http://www.google.com";
		driver.get(startUrl);
		
		driver.findElement(By.id("lst-ib")).sendKeys(searchString);
		driver.findElement(By.name("btnG")).click();

		Integer pageIndex = 1;
		Integer processed = 0;

		String currentIndexXPATH = "//*[@id='foot']//td[not(contains(@class, 'b'))]/b";
		while (processed < scrapeSites) {

			/*
			 * Check page number before scrape. Sometimes after click next page
			 * old page not replaced with new one yet, so I check current page
			 * index with pageIndex variable
			 */
			
			final Integer i = new Integer(pageIndex);
			try{
				(new WebDriverWait(driver, 30))
				.until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						boolean processFlag = true;
						Integer currentPageNumber=0;
						String currentIndexString = "";
						while (processFlag) {
							processFlag = false;
							try {
								
								List<WebElement> indexCanidatesList = driver.findElements(By.xpath(currentIndexXPATH));
								for (WebElement indexCandidate : indexCanidatesList){
									if (indexCandidate.getText()!=""){
										currentIndexString = indexCandidate.getText();
										break;
									}
								}
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
			}
			catch(TimeoutException temeoutException){
				exitDataItem("Google block!");
				driver.quit();
				return;
			}

			/*
			 * Search results xPath
			 */
			String searchResultsXPATH = "//div[@id='search']//h3/a";
			List<WebElement> linksList = driver.findElements(By
					.xpath(searchResultsXPATH));
			//System.out.println("Links found " + linksList.size());
			
			/*
			 * Collect links from page to structure tasksToScrape
			 */
			for (WebElement link : linksList) {
				String linkString = link.getAttribute("href");
				/*
				 * repair links like 
				 * /url?q=http://www.performancebike.com/&amp;sa=U&amp;ei=3wFvVcKXLoTlywOhgYHQAQ&amp;ved=0CBMQFjAA&amp;usg=AFQjCNHrNVhKpviusmc2Nh2TwwXCJiZrYw
				 * and take off first signs 
				 */
				String[] stringArray = linkString.split("/url\\?q=");
				if (stringArray.length>1){
					linkString = linkString.split("/url\\?q=")[1];
				}else{
					continue;
				}

				DataItem scrapedDataItem = new DataItem(
						linkString, link.getText());

				if (processed < scrapeSites) {
					try {
						sitesToScrapeQue.put(scrapedDataItem);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					processed++;
				} else {
					break;
				}
			}

			/*
			 *  Go to next search results page
			 */
			if (processed < scrapeSites) {
				
				/*
				 * wait to make activity slower
				 */
//				synchronized(this){
//					try {
//						wait(30000);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//				}
				
				try {
					String nextLinkXPATH = "//*[@id='foot']//td[contains(@class, 'b')]/a";			
					List<WebElement> nextCanidatesList = driver.findElements(By.xpath(nextLinkXPATH));
					
					for (WebElement nextCandidate : nextCanidatesList){
						if (nextCandidate.getText()!=""){
							//System.out.println("next " +nextCandidate.getText());
							break;
						}
					}
					nextCanidatesList.get(nextCanidatesList.size()-1).click();
					pageIndex++;
				} catch (NoSuchElementException e) {
					// if next button not found - stop search
					break;
				}
			} else {
				break;
			}
		}

		exitDataItem("Done!");
		driver.quit();
	}
	
	private void exitDataItem(String message){
		// last item with exit flag
		DataItem scrapedDataItem = new DataItem("exit", message);
		try {
			sitesToScrapeQue.put(scrapedDataItem);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
