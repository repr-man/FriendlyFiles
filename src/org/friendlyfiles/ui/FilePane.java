package org.friendlyfiles.ui;

import java.io.File;

import org.friendlyfiles.models.FileModel;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

public class FilePane extends Pane {
	
	// Color properties
	private Color selectedColor = new Color(3/255., 189/255., 245/255., 1);
	
	// Data components
	private String file;
	
	private int height;
	private int width;
	private int border;
	
	private Image fileIco;
	
	// FX Components
	private VBox fileDisplay;
	private Label fileLabel;
	private ImageView imageView;
	
	// Selected property
	private boolean isSelected;
	
	public FilePane(String file, int height, int width, int border, Image image) {
		super();
		
		this.file = file;
		
		this.height = height;
		this.width = width;
		this.border = border;
		
		this.fileIco = image;
		
		setup();
	}
	
	public void setup() {
		
		// Set size properties
		this.setMinHeight(height);
		this.setPrefHeight(height);
		this.setMaxHeight(height);
		
		this.setMinWidth(width);
		this.setPrefWidth(width);
		this.setMaxWidth(width);
		
		// Create file display area and set its properties
		fileDisplay = new VBox();
		
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
		fileLabel = new Label(file);
		
		fileLabel.setMinWidth(width - border);
		fileLabel.setPrefWidth(width - border);
		fileLabel.setMaxWidth(width - border);
		
		fileLabel.setFont(new Font(12));
		
		fileLabel.setStyle("-fx-alignment: center");
		
		// Create an image view for the file and set up display properties
		imageView = new ImageView();
		imageView.setImage(fileIco);
		imageView.setFitHeight(height - border - 32);
		
		imageView.setPreserveRatio(true);
		imageView.setSmooth(false);
		imageView.setCache(true);
		
		// Add content to the file display area
		fileDisplay.getChildren().add(imageView);
		fileDisplay.getChildren().add(fileLabel);
		
		// Set default border of the file display area
		fileDisplay.setBorder(new Border(new BorderStroke(new Color(0, 0, 0, 0), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3))));
		
		// Add all content to this file pane
		this.getChildren().add(fileDisplay);
		
		// Style the components to center the content
		//fileLabel.setStyle("-fx-alignment: center");
		
		// Add a mouse hover enter event to the file pane
		fileDisplay.addEventHandler(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				fileDisplay.setBorder(new Border(new BorderStroke(selectedColor, BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3))));
			}
		});
		
		// Add a mouse hover exit event to the file pane
		fileDisplay.addEventHandler(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				
				fileDisplay.setBorder(new Border(new BorderStroke(new Color(0, 0, 0, 0), BorderStrokeStyle.SOLID, new CornerRadii(6), new BorderWidths(3))));
			}
		});
		
		// Temporary colored background
		//fileDisplay.setBackground(new Background(new BackgroundFill(Paint.valueOf("LightGray"), new CornerRadii(2), new Insets(0))));
	}
	
	// Get the file stored within the filePane
	// Instead of storing a file, this object could just store an index/key that points to a file in a "master collection"
	// Something like a hashset could work as long as we have a separate array to hold the keys sorted as needed
	public String getFile() {
	
		return file;
	}
	
	public VBox getSelectionArea() {
		
		return fileDisplay;
	}
	
	public boolean isSelected() {
		
		return isSelected;
	}
	
	public void setSelected(boolean selected) {
		
		isSelected = selected;
	}
}
