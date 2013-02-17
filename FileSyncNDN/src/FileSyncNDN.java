import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import java.util.List;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class FileSyncNDN {	
	static final String NDNSETUP = System.getProperty("user.home") + "/.ccnx/NDNSetup.txt"; 

	/** 1. Shared Folder Path, 2. Topology, 3. Namespace */
	static final int LIMIT_NUM_OF_CONTENT = 3;

	public static void main (String[] args) {		
		/** Declare and Define Variables */
		boolean setupFinished = false;
		String sharedDirectoryPath = "", topologyString = "", namespaceString = "";
		ContentName topology = null, namespace = null, snapshot = null;

		/** Open Setup File */
		File setupFile = new File(NDNSETUP);
		List<String> fileContents;

		/** Define Scanner */
		Scanner input;

		while (!setupFinished) {
			/** File Contents */
			fileContents = new ArrayList<String>();

			/** Does the setup file Exist? */
			if(!setupFile.exists()) {
				try {
					/** Open Scanner for User Input */
					input = new Scanner(System.in);

					/** Create a new Setup file */
					setupFile.createNewFile();

					boolean successful = false;

					/** Shared Directory */
					File sharedDirectory;				
					do {
						System.out.print("Shared Directory Path: ");
						sharedDirectoryPath = input.nextLine();

						/** Open Shared Directory */
						sharedDirectory = new File(sharedDirectoryPath);

						if (sharedDirectory.exists() && sharedDirectory.isDirectory()) {
							successful = true;
						}
						else {
							System.out.println("Shared Directory is created.");
							sharedDirectory.mkdirs();
							successful = true;
						}
					}
					while (!successful);

					/** Topology */ 
					successful = false;				
					do {
						System.out.print("Topology: ");
						topologyString = input.nextLine();

						try {
							topology = ContentName.fromNative(topologyString);
							successful = true;
						} 
						catch (MalformedContentNameStringException e) {
							System.out.println("Topology is Malformed... Please try again.");
							successful = false;
						}
					}
					while (!successful);

					/** Namespace */
					successful = false;
					do {
						System.out.print("Namespace: ");
						namespaceString = input.nextLine();

						try {
							namespace = ContentName.fromNative(namespaceString);
							snapshot = ContentName.fromNative(namespaceString + "/snapshot");				
							successful = true;				
						} 
						catch (MalformedContentNameStringException e) {
							System.out.println("Namespace is Malformed... Please try again.");
						}
					}
					while (!successful);

					/** Close Scanner */ 
					input.close();

					/** Write File */
					BufferedWriter writer = new BufferedWriter(new FileWriter(NDNSETUP));
					writer.write(sharedDirectoryPath+'\n');
					writer.write(topologyString+'\n');
					writer.write(namespaceString);
					writer.close();

					setupFinished = true;

				} 
				catch (IOException e) {
					e.printStackTrace();
				}
			} 
			else {
				System.out.println("NDNSetup.txt exists");

				try {
					BufferedReader reader = new BufferedReader(new FileReader(NDNSETUP));
					String line = null;

					while((line=reader.readLine()) != null){
						fileContents.add(line);
					}

					reader.close();

					/** Read File Contents
					 * Case 1. Three paths are included and valid
					 * Case 2. Three paths are included and but some of them are invalid
					 * Case 3. Some paths are missing
					 */
					System.out.println("Total Number of Contents: " + fileContents.size());			

					if(fileContents.size() != LIMIT_NUM_OF_CONTENT) {
						/** Bad file contents, we should delete it and restart */
						setupFile.delete();
					}
					else {
						setupFinished = true;

						/** Check if everything is valid */
						File SharedDir = new File(fileContents.get(0));

						if(!SharedDir.exists()){						
							System.out.println("No Shared Directory");
							setupFinished = false;
						}					

						if(fileContents.get(1).isEmpty()){
							System.out.println("No Topology");
							setupFinished = false;
						}

						if(fileContents.get(2).isEmpty()){
							System.out.println("No Namespace");
							setupFinished = false;
						}

						if(!setupFinished) {
							setupFile.delete();
						} 
						else {
							sharedDirectoryPath = fileContents.get(0);
							topologyString = fileContents.get(1);
							namespaceString = fileContents.get(2);

							topology = ContentName.fromNative(topologyString);
							namespace = ContentName.fromNative(namespaceString);
							snapshot = ContentName.fromNative(namespaceString + "/snapshot");
						}		
					}
				} 
				catch (IOException e) {
					e.printStackTrace();
				} 
				catch (MalformedContentNameStringException e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("Bootstrap End");

		/** Initialize Parameters */		
		Parameters parameters = new Parameters(sharedDirectoryPath, topology, namespace, snapshot);			

		/** Invoke MainProgram */
		MainProgram mainProgram = new MainProgram(parameters);
		mainProgram.RunProgram();
	}
}
