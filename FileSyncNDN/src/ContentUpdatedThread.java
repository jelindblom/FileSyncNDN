import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ccnx.ccn.io.RepositoryVersionedOutputStream;
import org.ccnx.ccn.protocol.ContentName;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class ContentUpdatedThread implements Runnable {
	private ContentName versionedContentName;
	private Parameters parameters;
	private FileInformation fileInfo;
	private String filePath;

	public ContentUpdatedThread(String filePath, FileInformation fileInfo, ContentName versionedContentName, Parameters parameters) {
		this.filePath = filePath;
		this.fileInfo = fileInfo;
		this.versionedContentName = versionedContentName;
		this.parameters = parameters;
	}

	public void run() {
		try {
			/** De-bounce */
			Thread.sleep(250);
			
			/** Mac Bug Fix: */
			if (!fileInfo.getExistence()) {
				fileInfo.setExistence(true);
			}
			
			/** Create Versioned Output Stream */
			RepositoryVersionedOutputStream outputStream = new RepositoryVersionedOutputStream(versionedContentName, parameters.getHandle());
			
			/** Open File */
			File file = new File(filePath);
			
			/** Copy File to Stream */
			FileUtils.copyFile(file, outputStream);
			
			/** Close Versioned Output Stream */
			outputStream.close();
			
			System.out.println("Content UPDATED.");
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
	}
}