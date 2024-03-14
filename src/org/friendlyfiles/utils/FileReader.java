package org.friendlyfiles.utils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import org.friendlyfiles.models.FileModel;

public class FileReader {
	
	// File attribute readers
	BasicFileAttributeView fileAttrView;
	BasicFileAttributes fileAttributes;
	
	/**
	 * Get the files from within the provided root directories
	 * @param rootDirs the paths to the root directories
	 */
	public HashMap<RealPath, FileModel> getFilesFromDirs(ArrayList<RealPath> rootDirs) {
		
		HashMap<RealPath, FileModel> files = new HashMap<RealPath, FileModel>();
		
		// Loop through the given root directories to get the files
		for (RealPath rootDir : rootDirs) {
			
			try {
				
				// Walk through the file tree below the current root directory
				Files.walkFileTree(rootDir.toAbsolutePath(), new SimpleFileVisitor<Path>() {
					 
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						
						// Get file attributes from the open file/folder
						fileAttrView = Files.getFileAttributeView(file, BasicFileAttributeView.class);
						
						try {
							
							// Read the attributes
							fileAttributes = fileAttrView.readAttributes();
							
							// If the file is a regular file, we will create a model for it and add the model to the list of files
							if (fileAttributes.isRegularFile()) {
								
								// Create the model based on the path
								FileModel model = new FileModel(RealPath.create(file));
								
								// Fill model data
								model.setSize(fileAttributes.size());
								model.setDateModified(ZonedDateTime.parse(fileAttributes.lastModifiedTime().toString()));
								model.setDateCreated(ZonedDateTime.parse(fileAttributes.creationTime().toString()));
								
								// Add the model to the hashmap of files
								files.put(model.getPath(), model);
							}
							
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						
						return FileVisitResult.CONTINUE;
					}
				});
			}
			catch(Exception e) {
				
				System.out.println("Problem reading file data!");
				e.printStackTrace();
			}
		}
		
		return files;
	}
}
