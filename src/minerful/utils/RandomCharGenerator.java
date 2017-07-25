package minerful.utils;

import java.security.SecureRandom;

import com.thoughtworks.xstream.core.util.Base64Encoder;

public class RandomCharGenerator {

	public static String generateChar(int bytearraysize){
		SecureRandom sr = new SecureRandom();
		byte[] randomBytes = new byte[bytearraysize];
		String randChar = "+";
		while (!randChar.matches("\\w+")) {
			sr.nextBytes(randomBytes);
			randChar = new Base64Encoder().encode(randomBytes);
		}
		return randChar;
	}
}