package org.friendlyfiles;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UITest extends Application {
	
	private UIController controller;

	public static void main(String[] args) {
		
		Application.launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		/* In class, we were shown "Parent root = FXMLLoader.load([fxml resource]);" to load the fxml file with a static method
	 	 * However, we want to use an FXMLLoader __object__ instead so we can then get a reference to the controller object
		 * the .load([fxml file here]) method is static, so...
		 * We can pass the fxml file into the object's constructor to "prime" it, and call the object's .load() method instead (not static) 
		 */
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("friendlyfilesui.fxml"));
		Parent root = fxmlLoader.load();
		
		// Get the UI controller from the fxmloader object
		controller = (UIController)fxmlLoader.getController();
		
		// Continue with the standard UI setup, primarily initializing/presenting the stage
		primaryStage.setTitle("FriendlyFiles File Utility");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}

}
