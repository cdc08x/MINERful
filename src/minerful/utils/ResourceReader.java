package minerful.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceReader {
	public static final String readResource(String resourcePath) {
		StringBuilder sBuilder = new StringBuilder();
		
		BufferedReader buReader = new BufferedReader(
			new InputStreamReader(
				loadResource(resourcePath)
			)
		);
		String inLine;
		try {
			inLine = buReader.readLine();
			while (inLine != null) {
				sBuilder.append(inLine);
				sBuilder.append("\n");
				inLine = buReader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sBuilder.toString();
	}
	
	public static final InputStream loadResource(String resourcePath) {
		return ResourceReader.class.getClassLoader().getResourceAsStream(resourcePath);
	}
}