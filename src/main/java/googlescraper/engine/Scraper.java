package googlescraper.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Scraper {

	private String searchString = null;
	private Integer scrapeSites = null;

	/**
	 * This is the main class and it starts
	 * three primary threads: GoogleScrapper,
	 * ScrapeThreadsStarter and OutputThread.
	 * To info exchange BlockingQueues used.
	 * It takes two parameter as input. First - 
	 * search string, second sites to process 
	 * number.
	 * @param args
	 * @author Pavlo Morozov
	 */
	public static void main(String[] args) {
		String searchString = args[0];
		Integer sitesNumber = Integer.parseInt(args[1]);

		BlockingQueue<DataItem> sitesToScrapeQue = new LinkedBlockingQueue<DataItem>();
		BlockingQueue<DataItem> scrapedQueue = new  LinkedBlockingQueue<DataItem>();

		GoogleScraper googleScrapper = new GoogleScraper(sitesToScrapeQue, searchString, sitesNumber);
		
		Thread googleScraperThread = new Thread(googleScrapper);
		googleScraperThread.start();
		
		ScrapeThreadsStarter scrapeThreadsStarter = new ScrapeThreadsStarter(sitesToScrapeQue,scrapedQueue);
		Thread scrapeThreadsStarterThread = new Thread(scrapeThreadsStarter);
		scrapeThreadsStarterThread.start();
		
		OutputProcessor outputProcessor = 
				new OutputProcessor(scrapedQueue, sitesToScrapeQue);
		
		Thread outputThread = new Thread(outputProcessor);
		outputThread.start();
	}
}
