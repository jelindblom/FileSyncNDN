import java.io.IOException;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.CCNInterestHandler;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.impl.CCNNetworkManager;
import org.ccnx.ccn.impl.CCNNetworkManager.NetworkProtocol;
import org.ccnx.ccn.profiles.ccnd.CCNDaemonException;
import org.ccnx.ccn.profiles.ccnd.FaceManager;
import org.ccnx.ccn.profiles.ccnd.PrefixRegistrationManager;
import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.Interest;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class BroadcastHandler implements CCNInterestHandler {

	private Parameters parameters;
	
	public BroadcastHandler (Parameters parameters) {
		this.parameters = parameters;
	}
	
	@Override
	public boolean handleInterest(Interest arg0) {
		ContentName ipAnnouncement = arg0.name();
		
		String ipAddress = ipAnnouncement.stringComponent(ipAnnouncement.count() - 1);
	
		System.out.println("Overheard this ip address: " + ipAddress);
		
		if(!parameters.faces.contains(ipAddress) && !ipAddress.equalsIgnoreCase(parameters.myIpAddress)) {			
			try {
				CCNHandle handle = CCNHandle.open();
				
				FaceManager faceManager = new FaceManager(handle);
				int faceID = faceManager.createFace(NetworkProtocol.UDP, ipAddress, new Integer(CCNNetworkManager.DEFAULT_AGENT_PORT));
				
				PrefixRegistrationManager prefixManager = new PrefixRegistrationManager(handle);
				prefixManager.registerPrefix("ccnx:/ccnx.org/Users/", faceID, 3);
				prefixManager.registerPrefix(parameters.getTopology(), faceID, 3);
				prefixManager.registerPrefix(parameters.getNamespace(), faceID, 3);
				
				parameters.faces.add(ipAddress);
				
				handle.close();
			} 
			catch (CCNDaemonException e) {
				System.out.println("Face already exists?");
			} 
			catch (ConfigurationException e) {
				e.printStackTrace();
			} 
			catch (IOException e) {
				e.printStackTrace();
			} 
		}
		
		return true;
	}

}
