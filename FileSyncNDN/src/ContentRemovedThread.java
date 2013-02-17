
/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class ContentRemovedThread implements Runnable {
	private String fileNameString;
	private Parameters parameters;

	public ContentRemovedThread(String fileNameString, Parameters parameters) {
		this.fileNameString = fileNameString;
		this.parameters = parameters;
	}

	public void run() {
		/** Is Deleted File Known? */
		if (parameters.containsKeyInSharedFiles(fileNameString)) {
			FileInformation fileInfo = parameters.getFromSharedFiles(fileNameString);

			/** If it exists, make it not exist */
			if (fileInfo.getExistence()) {
				fileInfo.setExistence(false);
				System.out.println(fileNameString + " existence set to false.");
			}
		}
		else {
			/** BugFix: Ubuntu 12.04 */
			if (fileNameString.endsWith("/")) {
				fileNameString = fileNameString.substring(0, fileNameString.length() - 1);
				System.out.println("Removed Slash: " + fileNameString);

				if (parameters.containsKeyInSharedFiles(fileNameString)) {
					FileInformation fileInfo = parameters.getFromSharedFiles(fileNameString);

					/** If it exists, make it not exist */
					if (fileInfo.getExistence()) {
						fileInfo.setExistence(false);
						System.out.println(fileNameString + " existence set to false.");
					}
				}
			}
		}
	}
}