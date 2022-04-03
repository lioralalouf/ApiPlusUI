package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

	static Properties properties = new Properties();



	public static String readProperty(String key) {
		String value = "";
		String propertyFile = "";
		String env = System.getProperty("env");

		if (properties.containsKey(key)) {
			return properties.getProperty(key);
		}

		if (env == null) {
			propertyFile = "./src/test/resources/configuration.int.properties";
		} else {
			propertyFile = "./src/test/resources/configuration."  + env + ".properties";
		}



		try (InputStream input = new FileInputStream(propertyFile)) {

			// load a properties file
			properties.load(input);
			// get the property value and print it out
			value = properties.getProperty(key);

		} catch (Exception e) {

		}
		return value;
	}

	public static File readRequest(String folder, String requestName) {
		return new File("./src/test/resources/requests/" + folder + "/" + requestName + ".json");
	}


}
