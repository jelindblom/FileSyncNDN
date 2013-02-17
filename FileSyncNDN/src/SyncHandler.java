import org.ccnx.ccn.CCNSyncHandler;
import org.ccnx.ccn.io.content.ConfigSlice;
import org.ccnx.ccn.profiles.VersionMissingException;
import org.ccnx.ccn.profiles.VersioningProfile;
import org.ccnx.ccn.protocol.ContentName;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class SyncHandler implements CCNSyncHandler {
	Parameters parameters;

	public SyncHandler(Parameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public void handleContentName(ConfigSlice arg0, ContentName arg1) {
		ContentName syncedContent = arg1;
		
		/** Strip Version information from ContentName */
		ContentName contentName = VersioningProfile.cutLastVersion(syncedContent);

		/** If this is a snapshot */
		if (contentName.compareTo(parameters.getSnapshot()) == 0) {
			try {
				/** If we have a new snapshot */
				if (parameters.getSnapshotVersion() == null || VersioningProfile.isLaterVersionOf(syncedContent, parameters.getSnapshotVersion())) {
					/** Update our version */
					parameters.setSnapshotVersion(syncedContent);

					/** Handle this snapshot */
					Runnable runnable = new GlobalSnapshotUpdateListener(syncedContent, parameters);
					parameters.threadPool.submit(runnable);
				}
			}
			catch (VersionMissingException e) {
				e.printStackTrace();
			}
		}
	}
}