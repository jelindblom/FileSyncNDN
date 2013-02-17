import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ccnx.ccn.CCNHandle;
import org.ccnx.ccn.io.ErrorStateException;
import org.ccnx.ccn.io.content.CCNNetworkObject;
import org.ccnx.ccn.io.content.ContentDecodingException;
import org.ccnx.ccn.io.content.ContentEncodingException;
import org.ccnx.ccn.io.content.ContentGoneException;
import org.ccnx.ccn.io.content.ContentNotReadyException;
import org.ccnx.ccn.protocol.ContentName;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class CCNSnapshotObject extends CCNNetworkObject<byte[]> {
	
	public CCNSnapshotObject(ContentName name, CCNHandle handle) throws ContentDecodingException, IOException {
		super(byte[].class, true, name, handle);
	}

	@Override
	protected byte[] readObjectImpl(InputStream arg0) throws ContentDecodingException, IOException {
		/** InputStream to byte[] */	
		byte[] temp = new byte[arg0.available()];
		arg0.read(temp);
		arg0.close();
		
		return temp;
	}

	@Override
	protected void writeObjectImpl(OutputStream arg0) throws ContentEncodingException, IOException {
		/** byte[] to OutputStream */
		arg0.write(data());
		arg0.close();
	}
	
	public byte[] contents() throws ContentNotReadyException, ContentGoneException, ErrorStateException {
		/** Return Data */
		return data();
	}
}
