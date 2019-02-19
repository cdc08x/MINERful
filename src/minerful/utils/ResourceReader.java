package minerful.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import minerful.io.encdec.declaremap.DeclareMapEncoderDecoder;

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
	
	public static final InputStream loadResource(String libraryUrl, String resourcePath, Class<?> classLoaderProvider) {
		URL url = null;
		try {
			url = new URL(libraryUrl);
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		URL[] urls = {url};
		URLClassLoader classLoader = new URLClassLoader(urls, classLoaderProvider.getClassLoader());
		Thread.currentThread().setContextClassLoader(classLoader);
		
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
	}
}