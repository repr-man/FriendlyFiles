package org.friendlyfiles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// TODO: Am I abstracting too early?  Do we really need this?

// Handles all coordination between the ui, backend, and file system.
class Switchboard extends Application {
	
    private Backend backend;
    // TODO: Change this to an array so we can have multiple file sources at once?
    private FileSource fileSource;
    
    // UI Controller
    private UIController controller;
    
    

    public Switchboard(Backend backend, FileSource fileSource, String[] args) {
    	
        this.backend = backend;
        this.fileSource = fileSource;
        
        // Launch the UI
        Application.launch(args);
    }


    
    // Start up UI-related components
	@Override
	public void start(Stage primaryStage) throws Exception {
		
		/* In class, we were shown "Parent root = FXMLLoader.load([fxml resource]);" to load the fxml file with a static method
	 	 * However, we want to use an FXMLLoader object instead so we can then get a reference to the controller object
		 * .load([fxml file here]) is static, so...
		 * We can pass the fxml file into the object's constructor to prime the fxml file, and call the object's .load() method instead (not static) 
		 */
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("friendlyfilesui.fxml"));
		Parent root = fxmlLoader.load();
		
		// Get the UI's controller from the fxmloader
		controller = (UIController)fxmlLoader.getController();
		
		// Continue with the standard UI setup, setting and showing the stage
		primaryStage.setTitle("FriendlyFiles File Utility");
		primaryStage.setScene(new Scene(root));
		primaryStage.show();
	}
}
