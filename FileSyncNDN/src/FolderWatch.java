import java.io.File;
import java.io.IOException;

import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyException;
import net.contentobjects.jnotify.JNotifyListener;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class FolderWatch {
	private int watchID;
	private Parameters parameters;

	public FolderWatch(Parameters parameters) {
		this.watchID = 0;
		this.parameters = parameters;
	}

	public void startJNotify() throws JNotifyException {
		int mask =  JNotify.FILE_ANY;

		boolean watchSubtree = true;  // Recursive

		watchID = JNotify.addWatch(parameters.getSharedDirectoryPath(), mask, watchSubtree, new JNotifyListener() {
			public void fileRenamed(int wd, String rootPath, String oldName, String newName) {
				System.out.println("Jnotify: File " + oldName + " renamed to " + newName + ".");

				contentRemoved(wd, rootPath, oldName);

				contentAddedOrUpdated(wd, rootPath, newName);
			}

			public void fileModified(int wd, String rootPath, String name) {
				System.out.println("Jnotify: File " + name + " modified.");
				contentAddedOrUpdated(wd, rootPath, name);
			}

			public void fileDeleted(int wd, String rootPath, String name) {
				System.out.println("Jnotify: File " + name + " deleted.");
				contentRemoved(wd, rootPath, name);
			}

			public void fileCreated(int wd, String rootPath, String name) {
				System.out.println("Jnotify: File " + name + " created.");
				contentAddedOrUpdated(wd, rootPath, name);
			}
		});
	}


	public void contentRemoved(int wd, String rootPath, String name) {
		/** Build File Name String */
		String fileNameString = getFileNameString(name);

		/** Has Criteria been met? */
		if (checkRemovedCriteria(fileNameString)) {
			Runnable runnable = new ContentRemovedThread(fileNameString, parameters);
			parameters.taskProgress.add(parameters.threadPool.submit(runnable));
		}
	}

	public void contentAddedOrUpdated(int wd, String rootPath, String name) {
		/** Build File Name String */
		String fileNameString = getFileNameString(name);

		try {
			/** Create File Based on Event */
			File file = new File(rootPath + fileNameString);

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
		name = "/" + name;

		return name;
	}

	public boolean checkRemovedCriteria(String fileNameString) {
		boolean passCriteria = false;

		if (parameters.containsKeyInSharedFiles(fileNameString)) {
			FileInformation fileInfo = parameters.getFromSharedFiles(fileNameString);

			if (fileInfo.getExistence()) {
				System.out.println(fileNameString + " passed criteria.");
				passCriteria = true;
			}
			else {
				System.out.println(fileNameString + " should not exist. Ignoring.");
			}
		}
		else {
			/** BugFix: Ubuntu 12.04 */
			if (fileNameString.endsWith("/")) {
				fileNameString = fileNameString.substring(0, fileNameString.length() - 1);
				System.out.println("Removed Slash: " + fileNameString);
				
				if (parameters.containsKeyInSharedFiles(fileNameString)) {
					FileInformation fileInfo = parameters.getFromSharedFiles(fileNameString);

					if (fileInfo.getExistence()) {
						System.out.println(fileNameString + " passed criteria.");
						passCriteria = true;
					}
					else {
						System.out.println(fileNameString + " should not exist. Ignoring.");
					}
				}
				else {
					System.out.println(fileNameString + " is not a shared file. Ignoring.");
				}
			}
			else {
				System.out.println(fileNameString + " is not a shared file. Ignoring.");
			}
		}

		return passCriteria;
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

	public void stopJNotify() throws JNotifyException {
		boolean res = false;

		res = JNotify.removeWatch(watchID);

		if (!res) {
			// invalid watch ID specified.
		}
	}

	public int getWatchID() {
		return watchID;
	}
}