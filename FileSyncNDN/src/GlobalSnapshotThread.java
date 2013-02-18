import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.Future;

import org.ccnx.ccn.io.RepositoryVersionedOutputStream;
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
public class GlobalSnapshotThread implements Runnable {
	private Parameters parameters;
	
	@SuppressWarnings("rawtypes")
	private ArrayList<Future> taskProgress;
	
	public GlobalSnapshotThread(Parameters parameters, @SuppressWarnings("rawtypes") ArrayList<Future> taskProgress) {
		this.parameters = parameters;
		this.taskProgress = taskProgress;
	}

	public void run() {
		/** Create Snapshot */
		try {
			@SuppressWarnings("rawtypes")
			Future future;
			
			while(!taskProgress.isEmpty()) {
				@SuppressWarnings("rawtypes")
				Iterator<Future> itr = taskProgress.iterator();
				
				while (itr.hasNext()) {
					future = itr.next();
					if(future.isDone() || future.isCancelled()) {
						itr.remove();
					}
				}
			}
			
			/** Add Version to Content Name */
			ContentName snapshotVersion = VersioningProfile.updateVersion(parameters.getSnapshot());

			/** Update FileInformation with Current Version */
			parameters.setSnapshotVersion(snapshotVersion);
			
			/** Create Versioned Output Stream */
			RepositoryVersionedOutputStream outputStream = new RepositoryVersionedOutputStream(snapshotVersion, parameters.getHandle());
			
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));

			Enumeration<String> keys = parameters.sharedFiles.keys();

			synchronized(parameters.sharedFiles) {
				while(keys.hasMoreElements()) {
					String key = keys.nextElement(), result;
					
					FileInformation info = parameters.sharedFiles.get(key);
					
					if (info.getExistence()) {
						result = key + ",true," + info.getVersionedContentName().toURIString() + "," + info.getLatestDigest();
					}
					else {
						result = key + ",false," + info.getVersionedContentName().toURIString() + "," + info.getLatestDigest();
					}
					
					bufferedWriter.write(result);
					bufferedWriter.newLine();
				}
			}
			/** Close Writer, Flush */
			bufferedWriter.close();
			
			outputStream.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}