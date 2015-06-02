package googlescraper.engine;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

/**
 * Prints out from Queue.
 * @author Pavlo Morozov
 *
 */
public class OutputProcessor implements Runnable {
	private BlockingQueue<DataItem> processQueue;
	private BlockingQueue<DataItem> inputQueue;
	boolean stopProcess = false;
	
	public OutputProcessor(BlockingQueue<DataItem> processQueue, BlockingQueue<DataItem> inputQueue){
		this.processQueue = processQueue;
		this.inputQueue = inputQueue;
	}
	
	public void stopProcess(){
		this.stopProcess = true;
	}
	
	@Override
	public void run() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date dateStart = new Date();

		System.out.println("=== Output processor thread start ===");
		
		while (!stopProcess) {
			try {
				DataItem taskDone = processQueue.take();
				/*
				 * to stop queue process pass ScrapedDataItem with "exit"
				 */
				if (taskDone.getDescription() == "exit") {
					stopProcess = true;
					System.out.println("================= Scrape complete =================");
					break;
				/*
				 *  create new thread
				 */
				} else {
					System.out.println("=================== Domain data ===================");
					System.out.println("Domain: "+taskDone.getDomainUrl());
					System.out.println("Description: "+taskDone.getDescription());
					taskDone.getAccounts().stream().forEach(p->System.out.println("Account: " + p));
					System.out.println("Input que size : "+inputQueue.size());
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Date dateStop = new Date();
		System.out.println("Start: "+ dateFormat.format(dateStart));
		System.out.println("Stop: "+ dateFormat.format(dateStop));
		Long delta = (dateStop.getTime() - dateStart.getTime())/1000;
		System.out.println("Delta: " + delta + " sec");
	}
}
