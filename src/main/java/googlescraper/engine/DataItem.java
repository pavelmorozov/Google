package googlescraper.engine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * This class stores URL to scrape from, initial description and social
 * media(email) accounts found.
 * @author Pavlo Morozov
 */
public class DataItem {
	private String url;
	private String description;
	private List<String> accounts;

	/**
	 * Constructor
	 */
	public DataItem(String url, String description) {
		this.url = url;
		this.description = description;
		accounts = new LinkedList<String>();
	}

	/*
	 * Getters and setters
	 */
	String getDomainUrl() {
		try {
			return (new URL(url)).getHost();
		} catch (MalformedURLException e) {
			return url;
		}
	}

	public String getUrl() {
		return url;
	}

	public String getDescription() {
		return description;
	}

	public void addAccount(String account) {
		accounts.add(account);
	}

	public List<String> getAccounts() {
		return accounts;
	}
}