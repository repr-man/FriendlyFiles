package org.friendlyfiles.ui;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class FilePane extends VBox {

    // Color properties
    private static final Color selectedColor = new Color(3 / 255., 189 / 255., 245 / 255., 1);
    private static final Border hoverBorder = new Border(new BorderStroke(selectedColor, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3)));
    private static final Border normalBorder = new Border(new BorderStroke(new Color(0, 0, 0, 0), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3)));

    // Data components
    private static int height = 112;
    private static int width = 80;
    private static int border = 12;

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

        //fileDisplay.setAlignment(Pos.CENTER);
        setFillWidth(true);


        setAlignment(Pos.CENTER);

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
        getChildren().add(imageView);
        getChildren().add(fileLabel);

        // Set default border of the file display area
        setBorder(normalBorder);

        // Style the components to center the content
        //fileLabel.setStyle("-fx-alignment: center");

        // Add a mouse hover enter event to the file pane
        addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                setBorder(hoverBorder);
            }
        });

        // Add a mouse hover exit event to the file pane
        addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                setBorder(normalBorder);
            }
        });

        addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            System.out.println(getFile());
        });
        // Temporary colored background
        //fileDisplay.setBackground(new Background(new BackgroundFill(Paint.valueOf("LightGray"), new CornerRadii(2), new Insets(0))));
    }

    // Get the file stored within the filePane
    public String getFile() {

        return filePath;
    }

    public boolean isSelected() {

        return isSelected;
    }

    public void setSelected(boolean selected) {

        isSelected = selected;
    }
}
