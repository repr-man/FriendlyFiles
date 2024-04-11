package org.friendlyfiles.ui;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class FilePane extends Pane {

    // Color properties
    private static final Color selectedColor = new Color(3 / 255., 189 / 255., 245 / 255., 1);
    private static final Border hoverBorder = new Border(new BorderStroke(selectedColor, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3)));
    private static final Border normalBorder = new Border(new BorderStroke(new Color(0, 0, 0, 0), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3)));

    // Data components
    private static int height;
    private static int width;
    private static int border;

    private final String filePath;

    // Selected property
    private boolean isSelected;

    public FilePane(String file, Image image) {
        super();

        // Set size properties
        this.setMinHeight(height);
        this.setPrefHeight(height);
        this.setMaxHeight(height);

        this.setMinWidth(width);
        this.setPrefWidth(width);
        this.setMaxWidth(width);

        this.filePath = file;

        setup(file, image);
    }

    public void setup(String file, Image image) {

        // Create file display area and set its properties
        VBox fileDisplay = new VBox();

        fileDisplay.setMinHeight(height - border);
        fileDisplay.setPrefHeight(height - border);
        fileDisplay.setMaxHeight(height - border);

        fileDisplay.setMinWidth(width - border);
        fileDisplay.setPrefWidth(width - border);
        fileDisplay.setMaxHeight(width - border);

        //fileDisplay.setAlignment(Pos.CENTER);
        fileDisplay.setFillWidth(true);


        fileDisplay.setAlignment(Pos.CENTER);

        // Create a label for the file and set its properties
        Label fileLabel = new Label(file.substring(file.lastIndexOf('/') + 1));

        fileLabel.setMinWidth(width - border);
        fileLabel.setPrefWidth(width - border);
        fileLabel.setMaxWidth(width - border);

        fileLabel.setFont(new Font(12));

        fileLabel.setStyle("-fx-alignment: center");

        // Create an image view for the file and set up display properties
        ImageView imageView = new ImageView();
        imageView.setImage(image);
        imageView.setFitHeight(height - border - 32);

        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);
        imageView.setCache(true);

        // Add content to the file display area
        fileDisplay.getChildren().add(imageView);
        fileDisplay.getChildren().add(fileLabel);

        // Set default border of the file display area
        fileDisplay.setBorder(normalBorder);

        // Add all content to this file pane
        this.getChildren().add(fileDisplay);

        // Style the components to center the content
        //fileLabel.setStyle("-fx-alignment: center");

        // Add a mouse hover enter event to the file pane
        fileDisplay.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                fileDisplay.setBorder(hoverBorder);
            }
        });

        // Add a mouse hover exit event to the file pane
        fileDisplay.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                fileDisplay.setBorder(normalBorder);
            }
        });

        // Temporary colored background
        //fileDisplay.setBackground(new Background(new BackgroundFill(Paint.valueOf("LightGray"), new CornerRadii(2), new Insets(0))));
    }

    public static void setBorder(int border) {
        FilePane.border = border;
    }

    public static void setWidth(int width) {
        FilePane.width = width;
    }

    public static void setHeight(int height) {
        FilePane.height = height;
    }

    // Get the file stored within the filePane
    public String getFile() {

        return filePath;
    }

    public VBox getSelectionArea() {

        return (VBox) getChildren().get(0);
    }

    public boolean isSelected() {

        return isSelected;
    }

    public void setSelected(boolean selected) {

        isSelected = selected;
    }
}
