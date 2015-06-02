package googlescraper.engine;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * This class runs one particular site scrape process thread
 * @author Pavlo Morozov
 */
public class ScrapeQueueProcessor implements Runnable {
	
	private static final String[] SOCIAL_MEDIA_DOMAINS = { "facebook.com",
			"plus.google.com", "twitter.com", "linkedin.com", "youtube.com" };

	private static final String MAILTO_PATTERN_STRING = "(?i)(mailto:.*@.*)";
	/*
	 * Max links to process on one page. Useful if page animated, to break
	 * infinitive operation
	 */
	private static Integer MAX_LINKS_NUMBER = 5000;

	private BlockingQueue<DataItem> inputQueue;
	private BlockingQueue<DataItem> outputQueue;
	WebDriver driver;

	public ScrapeQueueProcessor(BlockingQueue<DataItem> inputQueue,
		BlockingQueue<DataItem> outputQueue){
		this.inputQueue = inputQueue;
		this.outputQueue = outputQueue;
		//driver = new FirefoxDriver();
		driver = new HtmlUnitDriver();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
				Level.OFF);
	}
	
	/**
	 * Method checks links to be social media
	 * account or email and adds it to accounts list
	 * @param str
	 */
	private void checkAccounts(DataItem task, String str){
		if (str==null){
			return;
		}

		if (str.matches(MAILTO_PATTERN_STRING)){
			task.addAccount(str);
			return;
		}
		
		/*
		 * Check start url not social media 
		 */
		for (String socialMediaDomain: SOCIAL_MEDIA_DOMAINS){
			if (task.getDomainUrl().matches("(?i).*"+socialMediaDomain+".*")){
				//System.out.println("!!!!! Social media site " + socialMediaDomain + " skip!");
				return;
			}
		}
		
		List<String> domainsList = Arrays.asList(SOCIAL_MEDIA_DOMAINS);
		
		for (String socialMediaDomain:domainsList){
			if (str.matches("(?i)^http.?://[a-z0-9\\-_\\.]*"+socialMediaDomain+".*")) {
				task.addAccount(str);
				break;
			}
		}		
	}
	
	/**
	 * Scrapes data for given DataItem and put 
	 * results back to it. Pass result to outputQueue 
	 */
	private void scrape(DataItem task){
		String startUrl = task.getUrl();
		// System.out.println("===============================");
		// System.out.println("Start scrape with: " + startUrl);
		
		driver.get(startUrl);
		Integer linksProcessed = 0;
		boolean processFlag = true; //if page changed this flag will indicate
		while (processFlag) {
			/*
			 * If no Exceptions catch, do not repeat process
			 */
			processFlag = false;
			List<WebElement> linksList = driver.findElements(By.tagName("a"));
			linksList.addAll(driver.findElements(By.tagName("area")));
			for (WebElement link : linksList) {
				String str = null;
				try {
					str = link.getAttribute("href");
					checkAccounts(task, str);
					linksProcessed++;
					if (linksProcessed>=MAX_LINKS_NUMBER){
						break;
					}
				} catch (StaleElementReferenceException e) {
					//System.out.println(startUrl + " - Page changed!");
					/*
					 * Page changed, process it once more
					 */
					processFlag = true;
					break;
				}
			}
		}

		try {
			outputQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method consumes task from inputQueue, calls scrape and 
	 * put results in outputQueue
	 */
	private void process(){
		boolean stopProcess = false;
		while (!stopProcess) {
			try {
				DataItem task = inputQueue.take();
				/*
				 * to stop queue process pass ScrapedDataItem with "exit"
				 */
				if (task.getDescription() == "exit") {
					stopProcess = true;
					inputQueue.put(new DataItem("exit", "exit"));//to notify next processor to stop
				} else {
					scrape(task);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		driver.quit();
	}
	
	@Override
	public void run() {
		System.out.println("=== Scrape queue processor thread start ===");
		process();
	}

}
