package files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class ReadDataFromJsonFile {

	public static void main(String[] args) throws IOException, ParseException {

		JSONParser jp = new JSONParser();
		FileReader reader = new FileReader(
				"A:\\github\\DSS\\src\\test\\java\\files\\jsonFile.json");
		Object obj = jp.parse(reader);
		JSONObject partnerJsonObj = (JSONObject) obj;
		String name = (String) partnerJsonObj.get("name");

		System.out.println("my partner name is: " + name);

	}

}
