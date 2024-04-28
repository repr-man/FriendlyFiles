package org.friendlyfiles.ui;

import java.util.ArrayList;
import java.util.Arrays;

import org.friendlyfiles.models.SortStep;
import org.friendlyfiles.models.SortStep.OrderType;
import org.friendlyfiles.models.SortStep.SortType;

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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SortDialog extends Stage {
	
	private final int width = 500;
	private final int height = 350;
	
	UIController parent;
	
	public SortDialog(UIController parent) {
		
		this.parent = parent;
	}
	
	public void displayCreateDialog(ObservableList<SortStep> stepList) {
		
		setTitle("Sort Builder");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
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
    	
    	StackPane dynamicContentPane = new StackPane();
    	dynamicContentPane.setAlignment(Pos.TOP_CENTER);
    	
    	sortScreen.getChildren().add(dynamicContentPane);
    	
    	// Add buttons
    	Button createButton = new Button("Add");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(createButton, exitButton);
    	sortScreen.getChildren().add(buttons);
    	
    	cbx_sortTypes.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
    		
    		dynamicContentPane.getChildren().clear();
    		
    		boolean sortExists = false;
    		SortStep existingSort = null;
    		
    		// Check if the selected sort already exists
    		for (SortStep s : stepList) {
    			
    			if (s.getType().equals(SortStep.SortType.values()[cbx_sortTypes.getSelectionModel().getSelectedIndex()])) {
    				
    				sortExists = true;
    				existingSort = s;
    				break;
    			}
    		}
    		
    		// If the selected type of sort doesn't already exist, the user can continue
    		if (!sortExists) {
    			
    			dynamicContentPane.getChildren().add(new Text("Sort Order"));
	        	ArrayList<String> orderOptions = new ArrayList<String>(Arrays.asList(SortStep.getOrderNames()));
	        	ComboBox<String> cbx_order = new ComboBox<String>((FXCollections.observableArrayList(orderOptions)));
	        	cbx_order.getSelectionModel().clearAndSelect(0);
	        	dynamicContentPane.getChildren().add(cbx_order);
	        	
	        	// Set up button actions if the sort is good to go
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
    		}
    		// Set up button actions if the sort is not good to go
    		else {
    			
    			Text info = new Text("A sort of this type already exists: " + existingSort.getName());
				info.setTextAlignment(TextAlignment.CENTER);
				dynamicContentPane.getChildren().add(info);
    			
    			createButton.setOnAction(new EventHandler<ActionEvent>() {
    				
    				@Override
    				public void handle(ActionEvent event) {
    					
    					Alert alert = new Alert(AlertType.INFORMATION);
    					alert.setHeaderText("The provided sort could not be added.");
    					alert.setContentText("Please ensure all sorting information is set correctly, or press cancel");
    					
    					alert.showAndWait();
    				}
    			});
    		}
    		
    	});
    	
    	// Set up button actions on initial loading of the screen
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setHeaderText("The provided sort could not be added.");
				alert.setContentText("Please ensure all sorting information is set correctly, or press cancel");
				
				alert.showAndWait();
			}
		});
    	
    	exitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				close();
			}
		});
    	
    	
    	// Create a scene using the VBox and set it as the root element
    	Scene sortDialogScene = new Scene(sortScreen, width, height);
    	setScene(sortDialogScene);
    	show();
	}
	
	public void displayEditDialog(ObservableList<SortStep> stepList, int selectedIndex) {
		
		SortStep oldStep = stepList.get(selectedIndex);
		
    	setTitle("Sort Editor");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox sortScreen = new VBox(8);
    	sortScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	sortScreen.getChildren().add(new Text("Name"));
    	TextField txt_sortName = new TextField(oldStep.getName());
    	txt_sortName.setMaxWidth(150);;
    	sortScreen.getChildren().add(txt_sortName);
    	
    	// Add selection drop downs
    	sortScreen.getChildren().add(new Text("Sorting Type"));
    	ComboBox<String> cbx_sortTypes = new ComboBox<String>(
    			FXCollections.observableArrayList(SortStep.getTypeNames()[oldStep.getType().ordinal()]));
    	cbx_sortTypes.getSelectionModel().clearAndSelect(0);
    	sortScreen.getChildren().add(cbx_sortTypes);
    	
    	sortScreen.getChildren().add(new Text("Sort Order"));
    	ArrayList<String> orderOptions = new ArrayList<String>(Arrays.asList(SortStep.getOrderNames()));
    	ComboBox<String> cbx_order = new ComboBox<String>((FXCollections.observableArrayList(orderOptions)));
    	cbx_order.getSelectionModel().clearAndSelect(oldStep.getOrder().ordinal());
    	sortScreen.getChildren().add(cbx_order);
    	
    	// Add buttons
    	Button editButton = new Button("Apply Changes");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(editButton, exitButton);
    	sortScreen.getChildren().add(buttons);
    	
    	// Set up button actions
    	editButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String name = txt_sortName.getText();
				int typeIndex = cbx_sortTypes.getSelectionModel().getSelectedIndex();
				int orderIndex = cbx_order.getSelectionModel().getSelectedIndex();
				
				if (!name.trim().equals("") && typeIndex >= 0 && orderIndex >= 0) {
					
					SortStep step = new SortStep(name, oldStep.getType(), OrderType.values()[orderIndex]);
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
    	Scene sortDialogScene = new Scene(sortScreen, width, height);
    	setScene(sortDialogScene);
    	show();
	}
}
