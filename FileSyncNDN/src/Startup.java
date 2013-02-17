import java.io.File;
import java.io.IOException;

import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class Startup {
	Parameters parameters;

	public Startup (Parameters parameters) {
		this.parameters = parameters;
	}

	public void Populate(String sharedPath, Parameters parameters) {
		File sharedDirectory = new File(sharedPath);
		File[] fileList = sharedDirectory.listFiles();

		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isDirectory()) {
				Populate(fileList[i].getPath(), parameters);
			} 
			else {
				String fileName = fileList[i].getAbsolutePath();

				fileName = fileName.replaceFirst(parameters.getSharedDirectoryPath(), "");

				contentAddedOrUpdated(parameters.getSharedDirectoryPath(), fileName, fileList[i]);	
			}
		}
	}

	public void contentAddedOrUpdated(String rootPath, String name, File file) {
		/** Build File Name String */
		String fileNameString = getFileNameString(name);

		try {
			/** Does this File meet our criteria? */
			if (checkAddedOrUpdatedCriteria(file, fileNameString)) {
				FileInformation fileInfo = null;

				if (parameters.containsKeyInSharedFiles(fileNameString)) {
					fileInfo = parameters.getFromSharedFiles(fileNameString);

					/** Check If file is being modified by our program */
					if (!fileInfo.getModifiedState()) {

						/** If Doesn't Exist, Make it Exist */
						if (!fileInfo.getExistence()) {
							fileInfo.setExistence(true);
						}

						/** Calculate Content Checksum */
						String digest = MD5Checksum.getMD5Digest(file);

						/** Add Digest to File Information */
						if (!digest.equalsIgnoreCase(fileInfo.getLatestDigest())) {
							fileInfo.setLatestDigest(digest);

							/** Add Version to Content Name */
							ContentName versionedContentName = VersioningProfile.updateVersion(fileInfo.getContentName());

							/** Update FileInformation with Current Version */
							fileInfo.setVersionedContentName(versionedContentName);

							/** Dispatch to Update Content */
							Runnable runnable = new ContentUpdatedThread(rootPath + fileNameString, fileInfo, versionedContentName, parameters);
							parameters.taskProgress.add(parameters.threadPool.submit(runnable));
						}
					}
					else {
						System.out.println(fileNameString + " is being modified. Ignoring.");
					}
				}
				else {
					/** Get Content Name */
					ContentName contentName = parameters.getContentNameFromFileName(fileNameString);

					/** Calculate Content Checksum */
					String digest = MD5Checksum.getMD5Digest(file);
					
					/** Add Version to Content Name */
					ContentName versionedContentName = VersioningProfile.updateVersion(contentName);

					/** Create FileInformation */
					fileInfo = new FileInformation(fileNameString, contentName, true, versionedContentName, digest);
					parameters.addToSharedFiles(fileNameString, fileInfo);
					
					/** Dispatch to Update Content */
					Runnable runnable = new ContentUpdatedThread(rootPath + fileNameString, fileInfo, versionedContentName, parameters);
					parameters.taskProgress.add(parameters.threadPool.submit(runnable));
				}
			}
		} 
		catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFileNameString(String name) {
		/** Build File Name String */
		//return ("/" + name);

		if (!name.startsWith("/")) {
			name = "/" + name;
		}

		return name;
	}

	public boolean checkAddedOrUpdatedCriteria(File file, String fileNameString) {
		boolean passCriteria = false;

		if (file.isFile()) {
			if(!file.isHidden()) {
				if (!file.getAbsolutePath().endsWith("~")) {
					if (!fileNameString.endsWith("/")) {
						if (file.canRead()) {
							System.out.println(fileNameString + " passed criteria.");
							passCriteria = true;
						}
						else {
							System.out.println(fileNameString + " cannot be read. Ignoring.");
						}
					}
					else {
						System.out.println(fileNameString + " is incorrectly formatted. Ignoring.");
					}
				}
				else {
					System.out.println(fileNameString + " is a temporary file. Ignoring.");
				}
			}
			else {
				System.out.println(fileNameString + " is hidden. Ignoring.");
			}
		}
		else {
			System.out.println(fileNameString + " is not a file. Ignoring.");
		}

		return passCriteria;
	}

}