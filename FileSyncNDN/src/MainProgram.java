import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Future;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class MainProgram {
	private Parameters parameters;
	
	public MainProgram(Parameters parameters) {
		this.parameters = parameters;
	}
	
	public void RunProgram() {
		/** Build Local Shared Folder Hashtable */
		Startup startup = new Startup(parameters);
		startup.Populate(parameters.getSharedDirectoryPath(), parameters);
		
		/**  Start Jnotify */
		try {	
			/** Create Instance of JNotify */
			FolderWatch folderWatch = new FolderWatch(parameters);
			
			/** Start JNotify */
			folderWatch.startJNotify();
			
			/** Loop Forever */
			while(true) {
				/** Snapshot update needed? */
				if (!parameters.taskProgress.isEmpty()) {
					synchronized(parameters.taskProgress) {
						@SuppressWarnings("rawtypes")
						Runnable runnable = new GlobalSnapshotThread(parameters, new ArrayList<Future>(parameters.taskProgress));
						
						parameters.threadPool.submit(runnable);
						parameters.taskProgress.removeAll(parameters.taskProgress);
					}
				}
				Thread.sleep(2000);
			}
		} 
		catch (IOException e3) {
			System.out.println("JNotify could not start.");
			return;
		}
		catch (InterruptedException e4) {
			e4.printStackTrace();
		}
	}
}
	
