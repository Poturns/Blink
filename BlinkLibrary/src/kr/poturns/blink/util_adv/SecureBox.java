package kr.poturns.blink.util_adv;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 리소스 관련 난독화 및 보안성 향상 모듈.
 * 
 * @author Yeonho.Kim
 * @since 2014.11.16
 *
 */
public class SecureBox {
	
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
			// Using SHA-256 Hash-MD Algorithm.
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(plain.getBytes());

			byte[] byteData = md.digest();
			for (int i = 0; i < byteData.length; i++) {
				builder.append(
						Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}
		} catch (NoSuchAlgorithmException e) { ; }
		return builder.reverse().toString();
	}

	public static boolean isMatchedWith(String message, String hashed) {
		return grantHashMessage(message).equals(hashed);
	}
}
