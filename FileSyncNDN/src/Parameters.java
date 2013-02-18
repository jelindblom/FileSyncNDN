import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNSync;
import org.ccnx.ccn.config.ConfigurationException;
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
public class Parameters {
	/** Define Number of Threads */
	final int numThreads = 20;

	/** Global Knowledge of Local Shared Folder */
	Hashtable<String, FileInformation> sharedFiles;
	
	/** Set of Faces (for keys) */
	HashSet<String> faces;

	/** Global Snapshot Object */
	//CCNSnapshotObject globalSnapshotObject;

	/** Shared Directory Path */	
	private String sharedDirectoryPath;
	
	String myIpAddress;

	/** Topology and Namespace */
	private ContentName topology, namespace, snapshot, snapshotVersion, ipBroadcastPrefix, myheartBeatName;

	/** Sync */
	CCNSync sync;

	/** Communication Handle */
	private CCNHandle handle;

	/** ThreadPool */ 
	ExecutorService threadPool;

	/** Thread Futures */
	@SuppressWarnings("rawtypes")
	ArrayList<Future> taskProgress;

	/** Secure Random Generator */
	SecureRandom randomGenerator;

	@SuppressWarnings("rawtypes")
	public Parameters (String sharedDirectoryPath, ContentName topology, ContentName namespace, ContentName snapshot) {
		sharedFiles = new Hashtable<String, FileInformation>();
		faces = new HashSet<String>();

		this.sharedDirectoryPath = sharedDirectoryPath;
		this.topology = topology;
		this.namespace = namespace;
		this.snapshot = snapshot;
		this.snapshotVersion = null;

		threadPool = Executors.newFixedThreadPool(numThreads);

		taskProgress = new ArrayList<Future>();
		
		try {
			/** What's my IP Address? */
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(whatismyip.openStream()));

			myIpAddress = bufferedReader.readLine();
			bufferedReader.close();
			
			/** Build IPBroadcast Prefix */
			ipBroadcastPrefix = ContentName.fromNative("/ndn/broadcast/filesync/ip.address/");
			
			/** Build HeartBeat Name */
			myheartBeatName = ContentName.fromNative("/ndn/broadcast/filesync/ip.address/" + myIpAddress);
			
			/** Create Handle */
			handle = CCNHandle.open();
			
			/** Dispatch to Broadcast Periodic Heartbeats */
			Runnable runnable = new Heartbeats(this);
			threadPool.submit(runnable);
			
			/** Register to Receive Updates from Others */
			handle.registerFilter(ipBroadcastPrefix, new BroadcastHandler(this));
			
			sync = new CCNSync();

			/** Start Sync */
			sync.startSync(handle, topology, namespace, new SyncHandler(this));
		
			Thread.sleep(5000);
		} 
		catch (ConfigurationException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		catch (MalformedContentNameStringException e) {
			e.printStackTrace();
		}
	}

	public String getSharedDirectoryPath() {
		return sharedDirectoryPath;
	}

	public ContentName getTopology() {
		return topology;
	}

	public ContentName getNamespace() {
		return namespace;
	}

	public ContentName getSnapshot() {
		return snapshot;
	}

	public ContentName getSnapshotVersion() {
		return snapshotVersion;
	}
	
	public ContentName getHeartBeatName() {
		return myheartBeatName;
	}
	
	public CCNHandle getHandle() {
		return handle;
	}

	public void addToSharedFiles(String fileName, FileInformation fileInfo) {		
		/** Add to Shared Files */
		sharedFiles.put(fileName, fileInfo);
	}

	public boolean containsKeyInSharedFiles(String fileName) {
		/** Lookup entry in Shared Files */
		return sharedFiles.containsKey(fileName);
	}

	public FileInformation getFromSharedFiles(String fileName) {
		/** Lookup entry in Shared Files */
		return sharedFiles.get(fileName);
	}
	
	public void setSnapshotVersion(ContentName snapshotVersion) {
		this.snapshotVersion = snapshotVersion;
	}

	public ContentName getContentNameFromFileName(String fileName) throws MalformedContentNameStringException {
		/** Build Content Name */		
		ContentName contentName = namespace.append(fileName);

		System.out.println("Created Content Name: " + contentName.toString());

		return contentName;
	}
}