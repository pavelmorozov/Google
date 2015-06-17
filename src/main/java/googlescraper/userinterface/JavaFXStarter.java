package googlescraper.userinterface;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXStarter extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {
		System.out.println("Start GSMAS application");
		URL gsmasPrimary=null;
		gsmasPrimary=getClass().getResource("/gsmasPrimary.fxml");
		if (gsmasPrimary==null){
			gsmasPrimary=getClass().getResource("/resources/gsmasPrimary.fxml");
		}
		Scene myScene = new Scene(FXMLLoader.load(gsmasPrimary));
		primaryStage.setScene(myScene);
		primaryStage.setTitle("GSMAS");
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
