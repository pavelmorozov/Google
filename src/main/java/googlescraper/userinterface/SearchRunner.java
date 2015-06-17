package googlescraper.userinterface;

import googlescraper.engine.DataItem;
import googlescraper.engine.GoogleScraper;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
//import com.sun.javafx.tk.Toolkit.Task;import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

/**
 * This class consumes data from GoogleScrapper class and
 * put results to ObservableList
 * @author P
 *
 */
public class SearchRunner extends Task {
	
	private ObservableList<SiteFound> siteFoundList;
	private String searchString; 
	private Integer sitesNumber;
	private SimpleDoubleProperty progressProperty;
	private SimpleStringProperty progressStringProperty;
	
	public SearchRunner(
			ObservableList<SiteFound> siteFoundList,
			String searchString, 
			Integer sitesNumber, 
			SimpleDoubleProperty progressProperty,
			SimpleStringProperty progressStringProperty){
		
		this.siteFoundList = siteFoundList;
		this.searchString = searchString;
		this.sitesNumber = sitesNumber;
		this.progressProperty = progressProperty;
		this.progressStringProperty = progressStringProperty;
	}

	//@Override
	public void run() {
		
		BlockingQueue<DataItem> sitesToScrapeQue = new LinkedBlockingQueue<DataItem>();
		GoogleScraper googleScrapper = new GoogleScraper(sitesToScrapeQue, searchString, sitesNumber);
		Thread googleScraperThread = new Thread(googleScrapper,"googleScrapper");
		googleScraperThread.start();
		
		boolean stopProcess = false;
		while (!stopProcess) {
			
            if (isCancelled()) {
                break;
            }
			
			try {
				DataItem task = sitesToScrapeQue.take();
				/*
				 * ScrapedDataItem with "exit" stop queue process 
				 */
				if (task.getUrl() == "exit") {
					stopProcess = true;
			        Platform.runLater(new Runnable() {
			            @Override public void run() {
			           	 progressStringProperty.set(task.getDescription());
			            }
			        });
				} else {
					/*
					 * add task to gsmasPrimary TableView
					 */
					SiteFound siteFound = new SiteFound(
							true,
							task.getDomainUrl(),
							task.getDescription(),
							task.getUrl());
					siteFoundList.add(siteFound);
					
					progressProperty.set((float)siteFoundList.size()/sitesNumber);
					String progressString = String.valueOf(siteFoundList.size()) + " of " + String.valueOf(sitesNumber) + " - " + String.valueOf(100*siteFoundList.size()/sitesNumber) + "%";
					
	                 Platform.runLater(new Runnable() {
	                     @Override public void run() {
	                    	 progressStringProperty.set(progressString);
	                     }
	                 });
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected Object call() throws Exception {
		run();
		return null;
	}
}