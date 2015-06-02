package googlescraper.userinterface;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class JavaFXStarter extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		System.out.println("start");
		
		primaryStage.setTitle("GSMAS");
		Pane primaryPane = (Pane)FXMLLoader.load(getClass().getResource
	    		    ("gsmasPrimary.fxml"));
       Scene myScene = new Scene(primaryPane);
       primaryStage.setScene(myScene);
       primaryStage.show();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
