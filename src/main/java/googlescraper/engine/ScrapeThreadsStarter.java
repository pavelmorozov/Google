package googlescraper.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * This class starts parallel scrape threads, waits for 
 * complete and puts exit signal to outputQueue  
 * @author Pavlo Morozov
 */
public class ScrapeThreadsStarter implements Runnable {

	BlockingQueue<DataItem> inputQueue;
	BlockingQueue<DataItem> outputQueue;
	private static Integer THREADS_NUMBER=8;
	
	public ScrapeThreadsStarter(BlockingQueue<DataItem> sitesToScrapeQueue, 
			BlockingQueue<DataItem> scrapedQueue){
		inputQueue = sitesToScrapeQueue;
		outputQueue = scrapedQueue;
	}
	
	@Override
	public void run() {
		
		/*
		 * Run threads and put them to List
		 */
		List<Thread> threadsList = new LinkedList<Thread>();
		while (threadsList.size()<THREADS_NUMBER){
			//create new thread
			ScrapeQueueProcessor processor = new ScrapeQueueProcessor(
					inputQueue, outputQueue);
			Thread t = new Thread(processor, "ScrapeQueueProcessor_"+String.valueOf(threadsList.size()));
			t.start();
			threadsList.add(t);
		}
		
		/*
		 * Wait for all threads to finish
		 */
		for (Thread t:threadsList){
			try {
				t.join();
			} catch (InterruptedException e) {
				System.out.println("Problem occures while wait scrape threads to finish");
				e.printStackTrace();
			}
		} 
		
		/*
		 * after all threads complete, put exit
		 * in output queue
		 */
		try {
			outputQueue.put(new DataItem("exit", "exit"));
		} catch (InterruptedException e) {
			System.out.println("Problem occures while put exit to outputQueue");
			e.printStackTrace();
		}
	}
}
