import org.ccnx.ccn.protocol.ContentName;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class FileInformation {
	private ContentName contentName, versionedContentName;
	private boolean exists, isBeingModified;
	private String fileName, latestDigest;
		
	public FileInformation(String fileName, ContentName contentName, boolean exists, ContentName versionedContentName, String latestDigest) {
		this.fileName = fileName;
		this.contentName = contentName;
		this.exists = exists;
		
		this.isBeingModified = false;
		this.versionedContentName = versionedContentName;
		this.latestDigest = latestDigest;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public ContentName getContentName() {
		return contentName;
	}
	
	public boolean getExistence() {
		return exists;
	}
	
	public ContentName getVersionedContentName() {
		return versionedContentName;
	}
	
	public String getLatestDigest() {
		return latestDigest;
	}
	
	public boolean getModifiedState() {
		return isBeingModified;
	}
	
	public void setExistence(boolean exists) {
		this.exists = exists;
	}
	
	public void setVersionedContentName(ContentName versionedContentName) {
		this.versionedContentName = versionedContentName;
	}
	
	public void setLatestDigest(String latestDigest) {
		this.latestDigest = latestDigest;
	}
	
	public void setModifyState(boolean isBeingModified) {
		this.isBeingModified = isBeingModified;
	}
}