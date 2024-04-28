package org.friendlyfiles.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import javafx.scene.control.DatePicker;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
public class FilterDialog extends Stage {
	
	private final int width = 500;
	private final int height = 350;
	
	private UIController parent;
	
	private TextField tbx_filterName;
	private Button createButton;
	
	public FilterDialog(UIController parent) {
		
		this.parent = parent;
	}
	
	public void displayCreateDialog() {
		
		setTitle("Filter Builder");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	this.setWidth(width);
    	this.setHeight(height);
    	
    	// Create VBox for the root element of the scene
    	VBox filterScreen = new VBox(8);
    	filterScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	filterScreen.getChildren().add(new Text("Name"));
    	tbx_filterName = new TextField();
    	tbx_filterName.setMaxWidth(150);
    	filterScreen.getChildren().add(tbx_filterName);
    	
    	// Add selection drop downs
    	filterScreen.getChildren().add(new Text("Filter Type"));
    	ArrayList<String> filterOptions = new ArrayList<String>(Arrays.asList(FilterStep.getTypeNames()));
    	ComboBox<String> cbx_filterTypes = new ComboBox<String>((FXCollections.observableArrayList(filterOptions)));
    	filterScreen.getChildren().add(cbx_filterTypes);
    	
    	StackPane dynamicContentPane = new StackPane();
    	dynamicContentPane.setAlignment(Pos.TOP_CENTER);
    	
    	filterScreen.getChildren().add(dynamicContentPane);
    	
    	cbx_filterTypes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
    		
    		dynamicContentPane.getChildren().clear();
    		
    		switch (cbx_filterTypes.getSelectionModel().getSelectedIndex()) {
    			
    		case 0:
    			
    			dynamicContentPane.getChildren().add(option_fileName());
    			break;
    			
    		case 1:
    			
    			dynamicContentPane.getChildren().add(option_fileExtension());
    			break;
    			
    		case 2:
    			
    			dynamicContentPane.getChildren().add(option_lastAccess());
    			break;
    			
    		case 3:
    			
    			dynamicContentPane.getChildren().add(option_fileSize());
    			break;
    		}
    	});
    	
    	
    	
    	// Add buttons
    	createButton = new Button("Add");
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
				
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setHeaderText("The filter could not be added.");
				alert.setContentText("Please select a filter type and provide the necessary information.");
				
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
    	Scene filterDialogScene = new Scene(filterScreen, 300, 260);
    	setScene(filterDialogScene);
    	show();
    	
	}
	
	public void displayEditDialog(ObservableList<FilterStep> stepList, int selectedIndex) {
		
		FilterStep step = stepList.get(selectedIndex);
		
		setTitle("Filter Editor");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
    	// Create VBox for the root element of the scene
    	VBox filterScreen = new VBox(8);
    	filterScreen.setAlignment(Pos.TOP_CENTER);
    	
    	// Add name field
    	filterScreen.getChildren().add(new Text("Name"));
    	TextField tbx_filterName = new TextField(step.getName());
    	tbx_filterName.setMaxWidth(150);
    	filterScreen.getChildren().add(tbx_filterName);
    	
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
				
				String name = tbx_filterName.getText();
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
	
	
	
	
	
	
	
	
	// UI for adding a filename filter
	private VBox option_fileName() {
		
		VBox content = new VBox(8);
		content.setAlignment(Pos.TOP_CENTER);
		
		content.getChildren().add(new Text("Filter by Text"));
		
		TextField tbx_input = new TextField();
		tbx_input.setMaxWidth(200);
		tbx_input.setPromptText("Search for a File");
		content.getChildren().add(tbx_input);
		
		
		// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				String fileName = tbx_input.getText();
				
				// If the filter name is given and the file name is valid, accept the input
				if (!filterName.trim().equals("") && !fileName.trim().equals("")) {
					
					FilterStep filter = new FilterStep(filterName, FilterType.EXTENSION);
					
					parent.onFilterAdd(filter);
					
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
		
		return content;
	}
	
	
	// UI for adding a file extension filter
	private VBox option_fileExtension() {
		
		VBox content = new VBox(8);
		content.setAlignment(Pos.TOP_CENTER);
		
		content.getChildren().add(new Text("Filter by File Extension"));
		
		TextField tbx_input = new TextField();
		tbx_input.setMaxWidth(120);
		tbx_input.setPromptText("File extension (.*)");
		content.getChildren().add(tbx_input);
		
		
		// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				String extension = tbx_input.getText();
				
				// If the filter name and extension are given, accept the input
				if (!filterName.trim().equals("") && !extension.replace(".", "").trim().equals("")) {
					
					// If the extension doesn't start with a ".", add it in before the entered text
					if (!extension.startsWith(".")) {
						
						extension = "." + extension;
					}
					
					FilterStep filter = new FilterStep(filterName, FilterType.EXTENSION);
					
					parent.onFilterAdd(filter);
					
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
		
		return content;
	}
	
	// UI for adding a last accessed filter
	private VBox option_lastAccess() {
		
		VBox content = new VBox(8);
		content.setAlignment(Pos.TOP_CENTER);
		
		HBox options = new HBox(20);
		options.setAlignment(Pos.TOP_CENTER);
		content.getChildren().add(options);
		
		VBox optionMinDate = new VBox(8);
		options.getChildren().add(optionMinDate);
		optionMinDate.getChildren().add(new Text("Date From:"));
		
		DatePicker dp_dateFrom = new DatePicker();
		dp_dateFrom.setMaxWidth(100);
		optionMinDate.getChildren().add(dp_dateFrom);
		
		HBox hbx_timeFrom = new HBox(8);
		optionMinDate.getChildren().add(hbx_timeFrom);
		
		Spinner<Integer> spn_hourFrom = new Spinner<Integer>(1, 12, 12);
		spn_hourFrom.setMaxWidth(60);
		Spinner<Integer> spn_minuteFrom = new Spinner<Integer>(0, 59, 0);
		spn_minuteFrom.setMaxWidth(60);
		ComboBox<String> cbx_periodFrom = new ComboBox<String>(FXCollections.observableArrayList("AM", "PM"));
		cbx_periodFrom.getSelectionModel().clearAndSelect(0);
		
		hbx_timeFrom.getChildren().addAll(spn_hourFrom, spn_minuteFrom, cbx_periodFrom);
		
		VBox optionMaxDate = new VBox(8);
		options.getChildren().add(optionMaxDate);
		optionMaxDate.getChildren().add(new Text("Date To:"));
		
		DatePicker dp_dateTo = new DatePicker();
		dp_dateTo.setMaxWidth(100);
		optionMaxDate.getChildren().add(dp_dateTo);
		
		HBox hbx_timeTo = new HBox(8);
		optionMaxDate.getChildren().add(hbx_timeTo);
		
		Spinner<Integer> spn_hourTo = new Spinner<Integer>(1, 12, 11);
		spn_hourTo.setMaxWidth(60);
		Spinner<Integer> spn_minuteTo = new Spinner<Integer>(0, 59, 59);
		spn_minuteTo.setMaxWidth(60);
		ComboBox<String> cbx_periodTo = new ComboBox<String>(FXCollections.observableArrayList("AM", "PM"));
		cbx_periodTo.getSelectionModel().clearAndSelect(1);
		
		hbx_timeTo.getChildren().addAll(spn_hourTo, spn_minuteTo, cbx_periodTo);
		
		
		// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				LocalDate startDate = dp_dateFrom.getValue();
				LocalTime startTime = LocalTime.of(spn_hourFrom.getValue(), spn_minuteFrom.getValue());
				
				LocalDate endDate = dp_dateTo.getValue();
				LocalTime endTime = LocalTime.of(spn_hourTo.getValue(), spn_minuteTo.getValue());
				
				LocalDateTime startDateTime;
				LocalDateTime endDateTime;
				
				// If the filter name is provided, we can validate the dates
				if (!filterName.trim().equals("")) {
					
					boolean datesValid = false;
					
					// This case only allows for two non-null dates, with the start occuring before the end
					if (startDate != null && endDate != null && startDate.isBefore(endDate)) {
						
						datesValid = true;
						startDateTime = LocalDateTime.of(startDate, startTime);
						endDateTime = LocalDateTime.of(endDate, endTime);
					}
					// This case only allows for a non-null start date and a null end date
					else if (startDate != null && endDate == null) {
						
						datesValid = true;
						startDateTime = LocalDateTime.of(startDate, startTime);
						endDateTime = LocalDateTime.MAX;
						
					}
					// This case only allows for a null start date and a non-null end date
					else if (startDate == null && endDate != null) {
						
						datesValid = true;
						startDateTime = LocalDateTime.MIN;
						endDateTime = LocalDateTime.of(endDate, endTime);
						
					}
					// If both dates were null, or the start was later than the end, we will warn the user
					else {
						
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setHeaderText("The provided filter could not be added.");
						alert.setContentText("Please ensure the date to and from fields are filled out correctly, or leave one field empty to remove the limit in that direction ");
						
						alert.showAndWait();
					}
					
					// If the dates were valid, we can create the filter with the data and close
					if (datesValid) {
						
						FilterStep filter = new FilterStep(filterName, FilterType.DATE_EDITED);
						
						parent.onFilterAdd(filter);
						
						close();
					}
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The provided filter could not be added.");
					alert.setContentText("Please ensure all filter information is set correctly, or press cancel");
					
					alert.showAndWait();
				}
			}
		});
		
		return content;
	}
	
	// UI for adding a file size filter
	private VBox option_fileSize() {
		
		VBox content = new VBox(8);
		content.setAlignment(Pos.TOP_CENTER);
		
		HBox options = new HBox(20);
		options.setAlignment(Pos.TOP_CENTER);
		content.getChildren().add(options);
		
		VBox optionMinSize = new VBox(8);
		options.getChildren().add(optionMinSize);
		optionMinSize.getChildren().add(new Text("Size From:"));
		HBox optionMinSize_controls = new HBox(4);
		optionMinSize.getChildren().add(optionMinSize_controls);
		
		TextField tbx_fileSizeMin = new TextField();
		tbx_fileSizeMin.setMaxWidth(70);
		tbx_fileSizeMin.setPromptText("Max: 1000");
		optionMinSize_controls.getChildren().add(tbx_fileSizeMin);
		
		ComboBox<String> cbx_fileSizeMin = new ComboBox<String>(FXCollections.observableArrayList("B", "KB", "MB", "GB", "TB"));
		cbx_fileSizeMin.getSelectionModel().clearAndSelect(1);
		cbx_fileSizeMin.setMaxWidth(60);
		optionMinSize_controls.getChildren().add(cbx_fileSizeMin);
		
		VBox optionMaxSize = new VBox(8);
		options.getChildren().add(optionMaxSize);
		optionMaxSize.getChildren().add(new Text("Size To:"));
		HBox optionMaxSize_controls = new HBox(4);
		optionMaxSize.getChildren().add(optionMaxSize_controls);
		
		TextField tbx_fileSizeMax = new TextField();
		tbx_fileSizeMax.setMaxWidth(70);
		tbx_fileSizeMax.setPromptText("Max: 1000");
		optionMaxSize_controls.getChildren().add(tbx_fileSizeMax);
		
		ComboBox<String> cbx_fileSizeMax = new ComboBox<String>(FXCollections.observableArrayList("B", "KB", "MB", "GB", "TB"));
		cbx_fileSizeMax.getSelectionModel().clearAndSelect(1);
		cbx_fileSizeMax.setMaxWidth(60);
		optionMaxSize_controls.getChildren().add(cbx_fileSizeMax);
		
		
		tbx_fileSizeMin.textProperty().addListener((obs, oldVal, newVal) -> {
		    
			if (!newVal.matches("\\d*")) {
				tbx_fileSizeMin.setText(newVal.replaceAll("[^\\d]", ""));
	        }
		});
		
		tbx_fileSizeMax.textProperty().addListener((obs, oldVal, newVal) -> {
		    
			if (!newVal.matches("\\d*")) {
				tbx_fileSizeMax.setText(newVal.replaceAll("[^\\d]", ""));
	        }
		});
		
		
		// Set up button actions
    	createButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				
				int baseSizeMin = -1;
				if (!tbx_fileSizeMin.getText().trim().equals("")) {
					
					baseSizeMin = (Integer.valueOf(tbx_fileSizeMin.getText()));
				}
				
				int baseSizeMax = -1;
				if (!tbx_fileSizeMax.getText().trim().equals("")) {
					
					baseSizeMax = (Integer.valueOf(tbx_fileSizeMax.getText()));
				}
				
				// If the filter name is given and either of the size textboxes are filled in, we will accept the input
				if (!filterName.trim().equals("") && (baseSizeMin >= 0 || baseSizeMax >= 0) && baseSizeMax > baseSizeMin) {
					
					int minSizeType = cbx_fileSizeMin.getSelectionModel().getSelectedIndex() + 1;
					int maxSizeType = cbx_fileSizeMax.getSelectionModel().getSelectedIndex() + 1;
					
					long sizeMin = -1;
					long sizeMax = -1;
					
					if (baseSizeMin <= 1000 && baseSizeMax <= 1000) {
						
						if (baseSizeMin >= 0) {
							
							sizeMin = (long) (baseSizeMin * Math.pow(10, minSizeType * 3));
						}
						
						if (baseSizeMax >= 0) {
							
							sizeMax = (long) (baseSizeMax * Math.pow(10, maxSizeType * 3));
						}
						
						
						
						FilterStep filter = new FilterStep(filterName, FilterType.FILESIZE);
						parent.onFilterAdd(filter);
						close();
					}
					else {
						
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setHeaderText("Invalid size value(s) provided.");
						alert.setContentText("Please enter numbers between 0 to 1000, or leave one field empty to remove the limit in that direction");
						
						alert.showAndWait();
					}
				}
				else {
					
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setHeaderText("The provided filter could not be added.");
					alert.setContentText("Please ensure all filter information is set correctly, or press cancel");
					
					alert.showAndWait();
				}
			}
		});
    	
    	return content;
	}
}
