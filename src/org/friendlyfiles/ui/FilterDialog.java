package org.friendlyfiles.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.friendlyfiles.FilterStep;
import org.friendlyfiles.FilterStep.FilterType;

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

public class FilterDialog extends Stage {
	
	public void displayCreateDialog(UIController parent) {
		
		setTitle("Filter Builder");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox filterScreen = new VBox(8);
    	filterScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	filterScreen.getChildren().add(new Text("Name"));
    	TextField txt_filterName = new TextField();
    	txt_filterName.setMaxWidth(150);;
    	filterScreen.getChildren().add(txt_filterName);
    	
    	// Add selection drop downs
    	filterScreen.getChildren().add(new Text("Filter Type"));
    	ArrayList<String> filterOptions = new ArrayList<String>(Arrays.asList(FilterStep.getTypeNames()));
    	ComboBox<String> cbx_filterTypes = new ComboBox<String>((FXCollections.observableArrayList(filterOptions)));
    	filterScreen.getChildren().add(cbx_filterTypes);
    	
    	
    	
    	// Add buttons
    	Button createButton = new Button("Add");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	filterScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_filterName.getText();
				int typeIndex = cbx_filterTypes.getSelectionModel().getSelectedIndex();
				
				if (name.trim() != "" && typeIndex >= 0) {
					
					FilterStep step = new FilterStep(name, FilterType.values()[typeIndex]);
					parent.onFilterAdd(step);
					
					close();
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The provided filter could not be added.");
					alert.setContentText("Please ensure all filter information is set correctly, or press cancel");
					
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
    	Scene filterDialogScene = new Scene(filterScreen, 300, 260);
    	setScene(filterDialogScene);
    	show();
    	
	}
	
	public void displayEditDialog(UIController parent, ObservableList<FilterStep> stepList, int selectedIndex) {
		
		FilterStep step = stepList.get(selectedIndex);
		
		setTitle("Filter Editor");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox filterScreen = new VBox(8);
    	filterScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	filterScreen.getChildren().add(new Text("Name"));
    	TextField txt_filterName = new TextField(step.getName());
    	txt_filterName.setMaxWidth(150);;
    	filterScreen.getChildren().add(txt_filterName);
    	
    	// Add selection drop downs
    	filterScreen.getChildren().add(new Text("Filter Type"));
    	ArrayList<String> filterOptions = new ArrayList<String>(Arrays.asList(FilterStep.getTypeNames()));
    	ComboBox<String> cbx_filterTypes = new ComboBox<String>((FXCollections.observableArrayList(filterOptions)));
    	cbx_filterTypes.getSelectionModel().clearAndSelect(step.getType().ordinal());
    	filterScreen.getChildren().add(cbx_filterTypes);
    	
    	// Add buttons
    	Button createButton = new Button("Apply Changes");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	filterScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_filterName.getText();
				int typeIndex = cbx_filterTypes.getSelectionModel().getSelectedIndex();
				
				if (name.trim() != "" && typeIndex >= 0) {
					
					FilterStep step = new FilterStep(name, FilterType.values()[typeIndex]);
					parent.onFilterEdit(selectedIndex, step);
					
					close();
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The edits could not be applied.");
					alert.setContentText("Please ensure all filter information is set correctly, or press cancel");
					
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
    	Scene filterDialogScene = new Scene(filterScreen, 300, 260);
    	setScene(filterDialogScene);
    	show();
	}
}
