package googlescraper.userinterface;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import googlescraper.engine.DataItem;
import googlescraper.engine.ScrapeThreadsStarter;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * This class takes all sites found from google and runs scrape threads starter
 * 
 * @author P
 *
 */
public class FindAccountsRunner extends Task {

	private ObservableList<AccountFound> accountsFoundList;
	private ObservableList<SiteFound> sitesFoundList;
	private SimpleDoubleProperty findAccountsProgressProperty;
	private SimpleStringProperty findAccountsProgressStringProperty;
	private boolean stopProcess;

	public FindAccountsRunner(ObservableList<AccountFound> accountsFoundList,
			ObservableList<SiteFound> sitesFoundList,
			SimpleDoubleProperty findAccountsProgressProperty,
			SimpleStringProperty findAccountsProgressStringProperty) {
		this.accountsFoundList = accountsFoundList;
		this.sitesFoundList = sitesFoundList;
		this.findAccountsProgressProperty = findAccountsProgressProperty;
		this.findAccountsProgressStringProperty = findAccountsProgressStringProperty;
		this.stopProcess = false;
	}

	public void stopProcess() {
		this.stopProcess = true;
	}

	@Override
	protected Object call() throws Exception {

		BlockingQueue<DataItem> sitesToScrapeQue = new LinkedBlockingQueue<DataItem>();
		BlockingQueue<DataItem> scrapedQueue = new LinkedBlockingQueue<DataItem>();

		Integer tasksNumber = 0;
		for (SiteFound siteFound : sitesFoundList) {
			if (siteFound.getProcess() == true) {
				sitesToScrapeQue.add(new DataItem(siteFound.getStartFromLink(),
						siteFound.getDescription()));
				tasksNumber++;
			}
		}

		sitesToScrapeQue.add(new DataItem("exit", "exit"));

		ScrapeThreadsStarter scrapeThreadsStarter = new ScrapeThreadsStarter(
				sitesToScrapeQue, scrapedQueue);
		Thread scrapeThreadsStarterThread = new Thread(scrapeThreadsStarter,
				"scrapeThreadsStarter");
		scrapeThreadsStarterThread.start();

		/*
		 * consumes data from scrapedQueue
		 */
		Integer tasksProcessedNumber = 0;
		while (!stopProcess) {
			try {
				DataItem taskDone = scrapedQueue.take();
				/*
				 * to stop queue process pass ScrapedDataItem with "exit"
				 */
				if (taskDone.getDescription() == "exit") {
					stopProcess = true;
					System.out
							.println("================= Scrape complete =================");
					break;
					/*
					 * Pass new result to JavaFX form
					 */
				} else {
					System.out
							.println("=================== Domain data ===================");
					System.out.println("Domain: " + taskDone.getDomainUrl());
					System.out.println("Description: "
							+ taskDone.getDescription());
					taskDone.getAccounts().stream()
							.forEach(p -> System.out.println("Account: " + p));
					System.out.println("Input que size : "
							+ scrapedQueue.size());
					
					for (String account : taskDone.getAccounts()) {
						AccountFound accountfound = new AccountFound(true,
								taskDone.getDomainUrl(), account);
						accountsFoundList.add(accountfound);
					}

					tasksProcessedNumber++;
					
					findAccountsProgressProperty
							.set((float) tasksProcessedNumber / tasksNumber);

					String progressString = String
							.valueOf(tasksProcessedNumber)
							+ " of "
							+ tasksNumber
							+ " - "
							+ String.valueOf(100 * tasksProcessedNumber
									/ tasksNumber) + "%";

					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							findAccountsProgressStringProperty
									.set(progressString);
						}
					});
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				findAccountsProgressStringProperty.set("Done!");
			}
		});
		return null;
	}
}
