import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.ccnx.ccn.io.CCNVersionedInputStream;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class GlobalSnapshotUpdateListener implements Runnable {
	private Parameters parameters;
	private ContentName snapshotVersion;

	public GlobalSnapshotUpdateListener (ContentName snapshotVersion, Parameters parameters) {
		this.snapshotVersion = snapshotVersion;
		this.parameters = parameters;
	}

	@Override
	public void run() {
		try {
			/** De-bounce */
			Thread.sleep(250);
			
			/** Create Input Stream	 */
			Interest interest = VersioningProfile.latestVersionInterest(snapshotVersion, null, null);
			CCNVersionedInputStream inputStream = new CCNVersionedInputStream(interest.name(), parameters.getHandle());

			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

			String line = bufferedReader.readLine();

			while (line != null) {
				String[] values = line.split(",");

				if (values.length == 4) {
					String fileNameString = values[0], versionedContentNameURI = values[2], latestDigest = values[3];

					/** Build Versioned Content Name */
					ContentName versionedContentName = ContentName.fromURI(versionedContentNameURI);

					boolean existence = false;

					if (values[1].equalsIgnoreCase("true")) {
						existence = true;
					}

					FileInformation fileInfo;

					if (parameters.containsKeyInSharedFiles(fileNameString)) {  /** Hashtable Hit (We know about this file) */
						fileInfo = parameters.getFromSharedFiles(fileNameString);

						if (existence) { /** Local File Should Exist */
							if (fileInfo.getExistence()) { /** Hashtable says file exists */
								if(fileInfo.getVersionedContentName().toURIString().equalsIgnoreCase(versionedContentNameURI)) {
									/** No Update Needed */
								}
								else {
									/** Update Needed */
									fileInfo.setVersionedContentName(versionedContentName);
									fileInfo.setLatestDigest(latestDigest);

									Runnable runnable = new UpdateContentThread(fileNameString, versionedContentName, fileInfo, parameters);
									parameters.threadPool.submit(runnable);
								}
							}
							else { /** Hashtable says file does not exist */
								/** File Now Exists */
								fileInfo.setExistence(true);

								/** Update Needed */
								fileInfo.setVersionedContentName(versionedContentName);
								fileInfo.setLatestDigest(latestDigest);

								Runnable runnable = new UpdateContentThread(fileNameString, versionedContentName, fileInfo, parameters);
								parameters.threadPool.submit(runnable);
							}
						}
						else { /** Local File Should not Exist */
							if (fileInfo.getExistence()) { /** Hashtable says file exists */
								/** Delete Local File */
								File file = new File(parameters.getSharedDirectoryPath() + fileNameString);

								if (file.exists()) {
									file.delete();
								}
							}
							else { /** Hashtable says file does not exist */
								/** No Update Needed */
							}
						}
					}
					else { /** Hashtable Miss (We don't know about this file) */
						/** Get Content Name */
						ContentName contentName = parameters.getContentNameFromFileName(fileNameString);

						fileInfo = new FileInformation(fileNameString, contentName, existence, versionedContentName, latestDigest);

						if (existence) { /** Local File Should Exist */
							/** Update Needed */
							Runnable runnable = new UpdateContentThread(fileNameString, versionedContentName, fileInfo, parameters);
							parameters.threadPool.submit(runnable);
						}
						else { /** Local File Should not Exist */
							/** No Update Needed */
						}

						parameters.addToSharedFiles(fileNameString, fileInfo);
					}

					line = bufferedReader.readLine();
				}
				else {
					System.out.println("Snapshot Format Issue on line: " + line);

					line = bufferedReader.readLine();
				}
			}
			
			bufferedReader.close();
		} 
		catch (IOException e) {
		
		}
		catch (MalformedContentNameStringException e) {
		
		} 
		catch (InterruptedException e) {
		
		}
	}
}