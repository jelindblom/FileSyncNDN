import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.ccnx.ccn.io.CCNVersionedInputStream;
import org.ccnx.ccn.protocol.ContentName;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class UpdateContentThread implements Runnable {
	private String fileNameString;
	private ContentName versionedContentName;
	private Parameters parameters;
	private FileInformation fileInfo;

	public UpdateContentThread(String fileNameString, ContentName versionedContentName, FileInformation fileInfo, Parameters parameters) {
		this.fileNameString = fileNameString;
		this.versionedContentName = versionedContentName;
		this.fileInfo = fileInfo;
		this.parameters = parameters;
	}

	@Override
	public void run() {
		/** Build Full Path */
		String filePath = parameters.getSharedDirectoryPath() + fileNameString;

		/** Request Content From Network */
		try {
			/** About to modify File */
			fileInfo.setModifyState(true);
			
			/** Create Input Stream	 */
			CCNVersionedInputStream inputStream = new CCNVersionedInputStream(versionedContentName);
		
			/** Open File */
			File file = new File(filePath);
			
			/** Copy to File */
			FileUtils.copyInputStreamToFile(inputStream, file);
			
			/** Close Input Stream */
			inputStream.close();

			/** End of File Modification */
			fileInfo.setModifyState(false);
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
