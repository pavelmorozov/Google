package googlescraper.userinterface;

import googlescraper.engine.DataItem;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.BlockingQueue;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/**
 * Main form controller
 * @author Pavlo Morozov
 *
 */
public class gsmasPrimaryController implements Initializable {

	@FXML private TextField searchRequest;
	@FXML private TextField sitesNumber;
	@FXML private TableView<SiteFound> sitesFoundTable;
	
    @FXML private TableColumn<SiteFound, Boolean> process;
    @FXML private TableColumn<SiteFound, String> domain;
    @FXML private TableColumn<SiteFound, String> description;
    @FXML private TableColumn<SiteFound, String> startFromLink;
    
	@FXML private TableView<AccountFound> accountsFoundTable;
    
    @FXML private TableColumn<AccountFound, Boolean> saveAccountsColumn;
    @FXML private TableColumn<AccountFound, String> domainAccountsColumn;
    @FXML private TableColumn<AccountFound, String> accountAccountsColumn;
    
    @FXML private ProgressBar googleSearchProgress;
    @FXML private Label googleSearchProgressLabel;
    @FXML private ProgressBar findAccountsProgress;
    @FXML private Label findAccountsProgressLabel;
	
	private ObservableList<SiteFound> sitesFoundList = FXCollections.observableArrayList();
	private ObservableList<AccountFound> accountsFoundList = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		/*
		 * Listener to sitesNumber for integer input check.
		 */
		sitesNumber.textProperty().addListener(
					new ChangeListener<String>() {
					    @Override
					    public void changed(ObservableValue<? extends String> observable,
					            String oldValue, String newValue) {
					        if (!newValue.matches("\\d*")) {
					        	sitesNumber.setText(oldValue);
					        }else if (!newValue.isEmpty()){
						        Integer sitesNumberInteger = Integer.parseInt(newValue); 
						        if (sitesNumberInteger>1000){
						        	sitesNumber.setText(oldValue);
						        }
					        };
					    }
					}
				);
		
		/*
		 * wire up columns with properties
		 */
		process.setCellValueFactory(cellData -> cellData.getValue().processProperty());		
        domain.setCellValueFactory(cellData -> cellData.getValue().domainProperty());
        description.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        startFromLink.setCellValueFactory(cellData -> cellData.getValue().startFromLinkProperty());
		
        /*
         * Set column editable
         */
		sitesFoundTable.setEditable(true);
		process.setEditable(true);
		sitesFoundTable.setItems(sitesFoundList);

		/*
		 * wire up columns with properties
		 */
		saveAccountsColumn.setCellValueFactory(cellData -> cellData.getValue().saveProperty());
		domainAccountsColumn.setCellValueFactory(cellData -> cellData.getValue().domainProperty());
	    accountAccountsColumn.setCellValueFactory(cellData -> cellData.getValue().accountProperty());
		
        /*
         * Set column editable
         */
		accountsFoundTable.setEditable(true);
		saveAccountsColumn.setEditable(true);
		accountsFoundTable.setItems(accountsFoundList);
		
		//sitesFoundList.add(new SiteFound(true, "domain", "description", "startFromLink"));
		//sitesFoundList.add(new SiteFound(false, "a", "b", "c"));
	}
	
	/**
	 * Search button pressed. Start search from google.
	 * @param event
	 */
	@FXML
	private void handleSearch(ActionEvent event){
		String searchRequestString = searchRequest.getText();
		String sitesNumberString = sitesNumber.getText(); 
		System.out.println("Search call: " + searchRequestString+ " " + sitesNumberString);
		
		final SimpleDoubleProperty progressProperty = new SimpleDoubleProperty(0);
		googleSearchProgress.progressProperty().bind(progressProperty);
		
		final SimpleStringProperty progressStringProperty = new SimpleStringProperty();
		googleSearchProgressLabel.textProperty().bind(progressStringProperty);
		
		sitesFoundList.clear();
		
		Integer scrapeSitesInteger=0;
		if (sitesNumberString.length()!=0){
			scrapeSitesInteger = Integer.parseInt(sitesNumberString);
		}else{ 
			scrapeSitesInteger = 25;
			sitesNumber.setText(scrapeSitesInteger.toString());
		}
		
		SearchRunner searchRunner = new SearchRunner(
				sitesFoundList, 
				searchRequestString, 
				scrapeSitesInteger, 
				progressProperty,
				progressStringProperty);
		
		Thread t = new Thread(searchRunner, "searchRunner thread");
		t.start();
	}

	/**
	 * Start search for accounts from sites found
	 * @param event
	 */
	@FXML
	private void handleFindAccounts(ActionEvent event){
		final SimpleDoubleProperty findAccountsProgressProperty = new SimpleDoubleProperty(0);
		findAccountsProgress.progressProperty().bind(findAccountsProgressProperty);
		
		final SimpleStringProperty findAccountsProgressStringProperty = new SimpleStringProperty();
		findAccountsProgressLabel.textProperty().bind(findAccountsProgressStringProperty);
		
		accountsFoundList.clear();

		FindAccountsRunner findAccountsRunner = new FindAccountsRunner(
				accountsFoundList,
				sitesFoundList,
				findAccountsProgressProperty,
				findAccountsProgressStringProperty);
		
		Thread t = new Thread(findAccountsRunner, "findAccountsRunner thread");
		t.start();
	}
	
	/**
	 * Saves results
	 * @param event
	 */
	@FXML
	private void handleSaveTo(ActionEvent event){
		
	}
}
