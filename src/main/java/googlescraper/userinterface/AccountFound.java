package googlescraper.userinterface;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AccountFound {
	private final BooleanProperty save;
	private final StringProperty domain;
	private final StringProperty account;

	public AccountFound(
			boolean save,
			String domain,
			String account){
		this.save = new SimpleBooleanProperty(save);
		this.domain = new SimpleStringProperty(domain);
		this.account = new SimpleStringProperty(account);
	}

	public Boolean getSave() {
		if (save!=null){
			return save.get();
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

	public String getAccount() {
		if (account!=null){
			return account.get();
		}else{
			return null;
		}
	}
	
	public void setSave(Boolean save){
		this.save.set(save); 
	}
	
	public void setSave(String domain){
		this.domain.set(domain); 
	}

	public void setAccount(String account){
		this.account.set(account); 
	}
	
	public BooleanProperty saveProperty() {
		return save;
	}

	public StringProperty domainProperty() {
		return domain;
	}

	public StringProperty accountProperty() {
		return account;
	}
}
