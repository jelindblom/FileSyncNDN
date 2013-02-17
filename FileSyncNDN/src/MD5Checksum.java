import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class MD5Checksum {

   public static String getMD5Digest(File file) throws IOException{
	   /** Open Input Stream */
	   FileInputStream inputStream = new FileInputStream(file);
	   
	   /** Calcuate Digest */
	   String digest = DigestUtils.md5Hex(inputStream);
	   
	   /** Close Input Stream */
	   inputStream.close();
	   
	   return digest;
   }
}