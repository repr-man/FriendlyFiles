package org.friendlyfiles.ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.friendlyfiles.models.FileDateFilter;
import org.friendlyfiles.models.FileExtensionFilter;
import org.friendlyfiles.models.FileSizeFilter;
import org.friendlyfiles.models.FileSizeFilter.SizeUnit;
import org.friendlyfiles.models.FilterStep;
import org.friendlyfiles.models.FilterStep.FilterType;
import org.friendlyfiles.models.FileTextFilter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FilterDialog extends Stage {
	
	private final int width = 500;
	private final int height = 350;
	
	private final UIController parent;
	
	private TextField tbx_filterName;
	private Button editButton;
	
	public FilterDialog(UIController parent) {
		
		this.parent = parent;
	}
	
	public void displayCreateDialog() {
		
		setTitle("Filter Builder");
    	initModality(Modality.APPLICATION_MODAL);
    	initOwner(parent.getRoot().getScene().getWindow());
    	
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
    	
    	// Add buttons
    	editButton = new Button("Add");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(editButton, exitButton);
    	filterScreen.getChildren().add(buttons);
    	
    	cbx_filterTypes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
    		
    		dynamicContentPane.getChildren().clear();
    		
    		switch (cbx_filterTypes.getSelectionModel().getSelectedIndex()) {
    			
	    		case 0:
	    			
	    			dynamicContentPane.getChildren().add(option_fileName(null, null));
	    			break;
	    			
	    		case 1:
	    			
	    			dynamicContentPane.getChildren().add(option_fileExtension(null, null));
	    			break;
	    			
	    		case 2:
	    			
	    			dynamicContentPane.getChildren().add(option_lastAccess(null, null));
	    			break;
	    			
	    		case 3:
	    			
	    			dynamicContentPane.getChildren().add(option_fileSize(null, null));
	    			break;
	    		}
    	});
    	
    	// Set up button actions
    	editButton.setOnAction(new EventHandler<ActionEvent>() {
			
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
    	Scene filterDialogScene = new Scene(filterScreen, width, height);
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
    	tbx_filterName = new TextField();
    	tbx_filterName.setMaxWidth(150);
    	tbx_filterName.setText(step.getName());
    	
    	filterScreen.getChildren().add(tbx_filterName);
    	
    	// Add selection drop down
    	filterScreen.getChildren().add(new Text("Filter Type"));
    	ComboBox<String> cbx_filterType = new ComboBox<String>((FXCollections.observableArrayList(FilterStep.getTypeNames()[step.getType().ordinal()])));
    	cbx_filterType.getSelectionModel().clearAndSelect(0);
    	filterScreen.getChildren().add(cbx_filterType);
    	
    	StackPane dynamicContentPane = new StackPane();
    	dynamicContentPane.setAlignment(Pos.TOP_CENTER);
    	
    	filterScreen.getChildren().add(dynamicContentPane);
    	
    	// Add buttons
    	editButton = new Button("Apply Changes");
    	Button exitButton = new Button("Cancel");
    	HBox buttons = new HBox(20);
    	VBox.setMargin(buttons, new Insets(50, 0, 0, 0));
    	buttons.setAlignment(Pos.TOP_CENTER);
    	buttons.getChildren().addAll(editButton, exitButton);
    	filterScreen.getChildren().add(buttons);
    	
    	switch (step.getType()) {
		
			case TEXT:
				dynamicContentPane.getChildren().add(option_fileName((FileTextFilter)step, selectedIndex));
				break;
				
			case EXTENSION:
				dynamicContentPane.getChildren().add(option_fileExtension((FileExtensionFilter)step, selectedIndex));
				break;
				
			case DATE_EDITED:
				dynamicContentPane.getChildren().add(option_lastAccess((FileDateFilter)step, selectedIndex));
				break;
				
			case FILESIZE:
				dynamicContentPane.getChildren().add(option_fileSize((FileSizeFilter)step, selectedIndex));
				break;
		}
    	
    	exitButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				close();
			}
		});
    	
    	// Create a scene using the VBox and set it as the root element
    	Scene filterDialogScene = new Scene(filterScreen, width, height);
    	setScene(filterDialogScene);
    	show();
	}
	
	// UI for adding a filename filter
	private VBox option_fileName(FileTextFilter oldFilter, Integer indexAt) {
		
		VBox content = new VBox(8);
		content.setAlignment(Pos.TOP_CENTER);
		
		content.getChildren().add(new Text("Filter by Text"));
		
		TextField tbx_input = new TextField();
		tbx_input.setMaxWidth(200);
		tbx_input.setPromptText("Search for a File");
		content.getChildren().add(tbx_input);
		
		// Fill data if an existing filter was provided
		if (oldFilter != null) {
			
			tbx_input.setText(oldFilter.getText());
		}
		
		// Set up button actions
    	editButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				String searchText = tbx_input.getText();
				
				// If the filter name is given and the file name is valid, accept the input
				if (!filterName.trim().isEmpty() && !searchText.trim().isEmpty()) {
					
					FileTextFilter filter = new FileTextFilter(filterName, FilterType.TEXT, searchText);
					
					if (oldFilter == null)
						parent.onFilterAdd(filter);
					else
						parent.onFilterEdit(indexAt, filter);
					
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
	private VBox option_fileExtension(FileExtensionFilter oldFilter, Integer indexAt) {
		
		VBox content = new VBox(8);
		content.setAlignment(Pos.TOP_CENTER);
		
		content.getChildren().add(new Text("Filter by File Extension"));
		
		TextField tbx_input = new TextField();
		tbx_input.setMaxWidth(120);
		tbx_input.setPromptText("File extension (.*)");
		content.getChildren().add(tbx_input);
		
		// Fill data if an existing filter was provided
		if (oldFilter != null) {
			
			tbx_input.setText(oldFilter.getExtension());
		}
		
		// Set up button actions
    	editButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				String extension = tbx_input.getText();
				
				// If the filter name and extension are given, accept the input
				if (!filterName.trim().isEmpty() && !extension.replace(".", "").trim().isEmpty()) {
					
					// If the extension doesn't start with a ".", add it in before the entered text
					if (!extension.startsWith(".")) {
						
						extension = "." + extension;
					}
					
					FileExtensionFilter filter = new FileExtensionFilter(filterName, FilterType.EXTENSION, extension);
					
					if (oldFilter == null)
						parent.onFilterAdd(filter);
					else
						parent.onFilterEdit(indexAt, filter);
					
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
	private VBox option_lastAccess(FileDateFilter oldFilter, Integer indexAt) {
		
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
		
		// Fill data if an existing filter was provided
		if (oldFilter != null) {
			
			LocalDateTime startDateTime = oldFilter.getStartDate();
			LocalDateTime endDateTime = oldFilter.getEndDate();
			
			dp_dateFrom.setValue(startDateTime.toLocalDate());
			
			int startHour = startDateTime.getHour() + 1;
			
			if (startHour > 12) {
				
				spn_hourFrom.getValueFactory().setValue(startHour - 12);
				cbx_periodFrom.getSelectionModel().clearAndSelect(1);
			}
			else {
				
				spn_hourFrom.getValueFactory().setValue(startHour);
				cbx_periodFrom.getSelectionModel().clearAndSelect(0);
			}
			
			
			spn_minuteFrom.getValueFactory().setValue(startDateTime.getMinute());
			
			dp_dateTo.setValue(endDateTime.toLocalDate());
			
			int endHour = endDateTime.getHour() + 1;
			
			if (endHour > 12) {
				
				spn_hourTo.getValueFactory().setValue(endHour - 12);
				cbx_periodFrom.getSelectionModel().clearAndSelect(1);
			}
			else {
				
				spn_hourTo.getValueFactory().setValue(endHour);
				cbx_periodFrom.getSelectionModel().clearAndSelect(0);
			}
			
			spn_minuteTo.getValueFactory().setValue(endDateTime.getMinute());
		}
		
		// Set up button actions
    	editButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				LocalDate startDate = dp_dateFrom.getValue();
				LocalTime startTime = LocalTime.of(spn_hourFrom.getValue() - 1, spn_minuteFrom.getValue());
				
				LocalDate endDate = dp_dateTo.getValue();
				LocalTime endTime = LocalTime.of(spn_hourTo.getValue() - 1, spn_minuteTo.getValue());
				
				// Default the time values to their minimum and maximum
				LocalDateTime startDateTime = LocalDateTime.MIN;
				LocalDateTime endDateTime = LocalDateTime.MAX;
				
				// If the filter name is provided, we can validate the dates
				if (!filterName.trim().isEmpty()) {
					
					boolean datesValid = false;
					
					// This case only allows for two non-null dates, with the start occuring before or equal to the end
					if (startDate != null && endDate != null && (startDate.isBefore(endDate) || startDate.isEqual(endDate))) {
						
						datesValid = true;
						startDateTime = LocalDateTime.of(startDate, startTime);
						endDateTime = LocalDateTime.of(endDate, endTime);
					}
					// This case only allows for a non-null start date and a null end date
					else if (startDate != null && endDate == null) {
						
						datesValid = true;
						startDateTime = LocalDateTime.of(startDate, startTime);
						
					}
					// This case only allows for a null start date and a non-null end date
					else if (startDate == null && endDate != null) {
						
						datesValid = true;
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
						
						FileDateFilter filter = new FileDateFilter(filterName, FilterType.DATE_EDITED, startDateTime, endDateTime);
						
						if (oldFilter == null)
							parent.onFilterAdd(filter);
						else
							parent.onFilterEdit(indexAt, filter);
						
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
	private VBox option_fileSize(FileSizeFilter oldFilter, Integer indexAt) {
		
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
		tbx_fileSizeMin.setPromptText("Max: 9999");
		optionMinSize_controls.getChildren().add(tbx_fileSizeMin);
		
		ComboBox<String> cbx_fileSizeMin = new ComboBox<String>(FXCollections.observableArrayList(FileSizeFilter.getUnitNames()));
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
		tbx_fileSizeMax.setPromptText("Max: 9999");
		optionMaxSize_controls.getChildren().add(tbx_fileSizeMax);
		
		ComboBox<String> cbx_fileSizeMax = new ComboBox<String>(FXCollections.observableArrayList(FileSizeFilter.getUnitNames()));
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
		
		
		// Fill data if an existing filter was provided
		if (oldFilter != null) {
			
			tbx_fileSizeMin.setText(String.valueOf(oldFilter.getMinSize() / (long) Math.pow(10, oldFilter.getMinSizeUnit().ordinal() * 3)));
			cbx_fileSizeMin.getSelectionModel().clearAndSelect(oldFilter.getMinSizeUnit().ordinal());
			
			
			tbx_fileSizeMax.setText(String.valueOf(oldFilter.getMaxSize() / (long) Math.pow(10, oldFilter.getMaxSizeUnit().ordinal() * 3)));
			cbx_fileSizeMax.getSelectionModel().clearAndSelect(oldFilter.getMaxSizeUnit().ordinal());
		}
		
		// Set up button actions
    	editButton.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				
				String filterName = tbx_filterName.getText();
				
				int baseSizeMin = -1;
				if (!tbx_fileSizeMin.getText().trim().isEmpty()) {
					
					baseSizeMin = (Integer.parseInt(tbx_fileSizeMin.getText()));
				}
				
				int baseSizeMax = -1;
				if (!tbx_fileSizeMax.getText().trim().isEmpty()) {
					
					baseSizeMax = (Integer.parseInt(tbx_fileSizeMax.getText()));
				}
				
				// If the filter name is given, either of the size textboxes are filled in, we will further process the input
				if (!filterName.trim().isEmpty() && (baseSizeMin >= 0 || baseSizeMax >= 0)) {
					
					int minSizeUnit = cbx_fileSizeMin.getSelectionModel().getSelectedIndex();
					int maxSizeUnit = cbx_fileSizeMax.getSelectionModel().getSelectedIndex();
					
					// Set the base sizes to 0 or the max value if they were not already assigned
					// This will also set the min/max size type to Byte/Terabyte
					if (baseSizeMin == -1) {
						
						baseSizeMin = 0;
						minSizeUnit = 0;
					}
					
					if (baseSizeMax == -1) {
						
						baseSizeMax = 9999;
						maxSizeUnit = 4;
					}
					
					long minSize = -1;
					long maxSize = -1;
					
					if (baseSizeMin <= 9999 && baseSizeMax <= 9999) {
						
						// Calculate the total number of bytes for the min and max size
						minSize = (baseSizeMin * (long) Math.pow(10, minSizeUnit * 3));
						maxSize = (baseSizeMax * (long) Math.pow(10, maxSizeUnit * 3));
						
						if (maxSize >= minSize) {
							
							FileSizeFilter filter = new FileSizeFilter(filterName, FilterType.FILESIZE, 
									SizeUnit.values()[maxSizeUnit], maxSize, 
									SizeUnit.values()[minSizeUnit], minSize);
							
							
							if (oldFilter == null)
								parent.onFilterAdd(filter);
							else
								parent.onFilterEdit(indexAt, filter);
							
							close();
						}
						else {
							
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setHeaderText("Invalid size value(s) provided.");
							alert.setContentText("Please ensure the maximum size is larger than the minimum size");
							
							alert.showAndWait();
						}
						
					}
					else {
						
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setHeaderText("Invalid size value(s) provided.");
						alert.setContentText("Please enter numbers between 0 to 9999, or leave one field empty to remove the limit in that direction");
						
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
