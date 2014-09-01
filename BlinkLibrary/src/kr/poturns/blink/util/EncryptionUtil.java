package kr.poturns.blink.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionUtil {

	/**
	 * 
	 * @param plain
	 * @return
	 */
	public static String grantHashMessage(final String plain) {
		if (plain == null)
			return null;
		
		StringBuilder builder = new StringBuilder();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(plain.getBytes());
			
			byte[] byteData = md.digest();
			for(int i = 0 ; i < byteData.length ; i++){
				builder.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			
		} catch (NoSuchAlgorithmException e) { ; }
		
		return builder.reverse().toString();
	}
	
	public static boolean isSame(String message, String hashed) {
		return grantHashMessage(message).equals(hashed);
	}
}
