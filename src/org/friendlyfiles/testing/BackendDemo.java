//package org.friendlyfiles.testing;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import org.friendlyfiles.models.*;
//import org.friendlyfiles.ui.*;
//import org.friendlyfiles.utils.*;
//
//public class BackendDemo {
//
//	// Reference to the UI controller
//	UIController controller;
//
//	// Root directories
//	ArrayList<String> rootDirs;
//
//	// All files loaded by the program
//	HashMap<String, FileModel> mFiles;
//
//	// Files selected after search, sort, filter, etc
//	ArrayList<String> selectedFiles;
//
//	public BackendDemo() {
//
//		loadRootDirs();
//		mFiles = loadFiles(rootDirs);
//	}
//
//	// Set the controller reference and perform some additional setup
//	public void setController(UIController controller) {
//
//		this.controller = controller;
//
//		// Update the directory tree of the UI
//		controller.updateDirTree(rootDirs);
//
//		// Fill the UI with all files in the selected directories
//		selectedFiles = searchFiles(new FileSearchModel("", false));
//		controller.updateFiles(selectedFiles);
//	}
//
//	public void loadRootDirs() {
//
//		rootDirs = new ArrayList<String>();
//
//		// TODO: Load root directories into the arraylist
//	}
//
//	public HashMap<String, FileModel> loadFiles(ArrayList<String> rootDirs) {
//
//		FileReader reader = new FileReader();
//
//		return reader.getFilesFromDirs(rootDirs);
//	}
//
//	public HashMap<String, FileModel> getMasterFiles() {
//
//		return mFiles;
//	}
//
//	public ArrayList<String> searchFiles(FileSearchModel search) {
//
//		ArrayList<String> result = new ArrayList<String>();
//
//		if (search.isExtensionIncluded()) {
//
//			// Search with file extension
//
//			for (FileModel file : mFiles.values()) {
//
//				if (file.getName().toLowerCase().contains(search.getQuery().toLowerCase())) {
//
//					result.add(file.getPath());
//				}
//			}
//		}
//		else {
//
//			// Search without file extension
//			for (FileModel file : mFiles.values()) {
//
//				if (file.getName().substring(0, file.getName().lastIndexOf('.')).toLowerCase().contains(search.getQuery().toLowerCase())) {
//
//					result.add(file.getPath());
//				}
//			}
//		}
//
//
//		return result;
//	}
//}
//