package org.friendlyfiles.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.friendlyfiles.SortStep;
import org.friendlyfiles.SortStep.OrderType;
import org.friendlyfiles.SortStep.SortType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SortDialog extends Stage {
	
	private final int width = 500;
	private final int height = 350;
	
	UIController parent;
	
	public SortDialog(UIController parent) {
		
		this.parent = parent;
	}
	
	public void displayCreateDialog() {
		
		setTitle("Sort Builder");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	this.setWidth(width);
    	this.setHeight(height);
    	
    	// Create VBox for the root element of the scene
    	VBox sortScreen = new VBox(8);
    	sortScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	sortScreen.getChildren().add(new Text("Name"));
    	TextField txt_sortName = new TextField();
    	txt_sortName.setMaxWidth(150);;
    	sortScreen.getChildren().add(txt_sortName);
    	
    	// Add selection drop downs
    	sortScreen.getChildren().add(new Text("Sorting Type"));
    	ArrayList<String> sortOptions = new ArrayList<String>(Arrays.asList(SortStep.getTypeNames()));
    	ComboBox<String> cbx_sortTypes = new ComboBox<String>((FXCollections.observableArrayList(sortOptions)));
    	sortScreen.getChildren().add(cbx_sortTypes);
    	
    	sortScreen.getChildren().add(new Text("Sort Order"));
    	ArrayList<String> orderOptions = new ArrayList<String>(Arrays.asList(SortStep.getOrderNames()));
    	ComboBox<String> cbx_order = new ComboBox<String>((FXCollections.observableArrayList(orderOptions)));
    	sortScreen.getChildren().add(cbx_order);
    	
    	// Add buttons
    	Button createButton = new Button("Add");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	sortScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_sortName.getText();
				int typeIndex = cbx_sortTypes.getSelectionModel().getSelectedIndex();
				int orderIndex = cbx_order.getSelectionModel().getSelectedIndex();
				
				if (!name.trim().equals("") && typeIndex >= 0 && orderIndex >= 0) {
					
					SortStep step = new SortStep(name, SortType.values()[typeIndex], OrderType.values()[orderIndex]);
					parent.onSortAdd(step);
					
					close();
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The provided sort could not be added.");
					alert.setContentText("Please ensure all sorting information is set correctly, or press cancel");
					
					alert.showAndWait();
				}
			}
		});
    	
    	exitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				close();
			}
		});
    	
    	
    	// Create a scene using the VBox and set it as the root element
    	Scene sortDialogScene = new Scene(sortScreen, 300, 260);
    	setScene(sortDialogScene);
    	show();
	}
	
	public void displayEditDialog(ObservableList<SortStep> stepList, int selectedIndex) {
		
		SortStep step = stepList.get(selectedIndex);
		
    	setTitle("Sort Editor");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox sortScreen = new VBox(8);
    	sortScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	sortScreen.getChildren().add(new Text("Name"));
    	TextField txt_sortName = new TextField(step.getName());
    	txt_sortName.setMaxWidth(150);;
    	sortScreen.getChildren().add(txt_sortName);
    	
    	// Add selection drop downs
    	sortScreen.getChildren().add(new Text("Sorting Type"));
    	ArrayList<String> sortOptions = new ArrayList<String>(Arrays.asList(SortStep.getTypeNames()));
    	ComboBox<String> cbx_sortTypes = new ComboBox<String>((FXCollections.observableArrayList(sortOptions)));
    	cbx_sortTypes.getSelectionModel().clearAndSelect(step.getType().ordinal());
    	sortScreen.getChildren().add(cbx_sortTypes);
    	
    	sortScreen.getChildren().add(new Text("Sort Order"));
    	ArrayList<String> orderOptions = new ArrayList<String>(Arrays.asList(SortStep.getOrderNames()));
    	ComboBox<String> cbx_order = new ComboBox<String>((FXCollections.observableArrayList(orderOptions)));
    	cbx_order.getSelectionModel().clearAndSelect(step.getOrder().ordinal());
    	sortScreen.getChildren().add(cbx_order);
    	
    	// Add buttons
    	Button createButton = new Button("Apply Changes");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	sortScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_sortName.getText();
				int typeIndex = cbx_sortTypes.getSelectionModel().getSelectedIndex();
				int orderIndex = cbx_order.getSelectionModel().getSelectedIndex();
				
				if (!name.trim().equals("") && typeIndex >= 0 && orderIndex >= 0) {
					
					SortStep step = new SortStep(name, SortType.values()[typeIndex], OrderType.values()[orderIndex]);
					parent.onSortEdit(selectedIndex, step);
					
					close();
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The edits could not be applied.");
					alert.setContentText("Please ensure all sorting information is set correctly, or press cancel");
					
					alert.showAndWait();
				}
			}
		});
    	
    	exitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				close();
			}
		});
    	
    	
    	// Create a scene using the VBox and set it as the root element
    	Scene sortDialogScene = new Scene(sortScreen, 300, 260);
    	setScene(sortDialogScene);
    	show();
	}
}
