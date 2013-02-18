import java.io.IOException;

import org.ccnx.ccn.protocol.Interest;


/**
	 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
	 *  
	 * @category Distributed File Sharing
	 * @author Jared Lindblom
	 * @author Huang (John) Ming-Chun
	 * @version 1.0
	 */
public class Heartbeats implements Runnable {

	private Parameters parameters;
	private Interest interest;

	public Heartbeats (Parameters parameters) {
		this.parameters = parameters;
		this.interest = new Interest(parameters.getHeartBeatName());
	}
	
	@Override
	public void run() {
		try {			
			while(true) {
					Thread.sleep(5000);
					
					/** Issue Interest, Timeout Instantly */
					parameters.getHandle().get(interest, 0);
			}
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
	}

}
