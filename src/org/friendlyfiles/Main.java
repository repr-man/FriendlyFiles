package org.friendlyfiles;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import org.friendlyfiles.ui.UIController;

public class Main extends Application {

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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/org/friendlyfiles/ui/friendlyfilesui.fxml"));
        Parent root = fxmlLoader.load();

        // Get the UI controller from the fxmloader object
        controller = fxmlLoader.getController();

        // Continue with the standard UI setup, primarily initializing/presenting the stage
        primaryStage.setTitle("FriendlyFiles File Utility");

        // Store a reference to the scene for further customization
        Scene scene = new Scene(root);

        // Add the "style.css" css stylesheet to the scene
        scene.getStylesheets().add(this.getClass().getResource("/org/friendlyfiles/ui/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Shuts down the application.
     * @throws Exception not that it really matters at this point
     */
    @Override
    public void stop() throws Exception {
        super.stop();
        controller.shutDown();
    }
}
