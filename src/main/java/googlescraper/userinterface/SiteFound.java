package googlescraper.userinterface;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SiteFound {
	private final BooleanProperty process;
	private final StringProperty domain;
	private final StringProperty description;  
	private final StringProperty startFromLink;

	public SiteFound(boolean process,
			String domain, String description,
			String startFromLink) {
		//super();
		this.process = new SimpleBooleanProperty(process);
		this.domain = new SimpleStringProperty(domain);
		this.description = new SimpleStringProperty(description);
		this.startFromLink = new SimpleStringProperty(startFromLink);
	}

	public void setProcess(Boolean process){
		this.process.set(process); 
	}	

	public void setDomain(String domain){
		this.domain.set(domain); 
	}	

	public void setDescription(String description){
		this.description.set(description); 
	}	

	public void setStartFromLink(String startFromLink){
		this.startFromLink.set(startFromLink); 
	}	
	
	public Boolean getProcess() {
		if (process!=null){
			return process.get();
		}else{
			return null;
		}		
	}

	public String getDomain() {
		if (domain!=null){
			return domain.get();
		}else{
			return null;
		}
	}

	public String getDescription() {
		if (description!=null){
			return description.get();
		}else{
			return null;
		}
	}

	public String getStartFromLink() {
		if (startFromLink!=null){
			return startFromLink.get();
		}else{
			return null;
		}		
	}

	public BooleanProperty processProperty() {
		return process;
	}

	public StringProperty domainProperty() {
		return domain;
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public StringProperty startFromLinkProperty() {
		return startFromLink;
	}
	
}
