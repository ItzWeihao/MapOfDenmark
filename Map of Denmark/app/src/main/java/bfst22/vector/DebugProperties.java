package bfst22.vector;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DebugProperties {
	private final Map<String, Boolean> debugValMap;

	public DebugProperties(){
		this.debugValMap = new HashMap<>();
		this.init();
	}

	private void init() {
		try {
			String propFileName = "debugconfig.properties";
			InputStream inputStream = this.getClass().getResourceAsStream(propFileName);

			if(inputStream != null) {
				Properties prop = new Properties();
				prop.load(inputStream);
				inputStream.close();

				((Map<String, String>) (Map) prop).forEach((key, value) ->
						this.debugValMap.put(key, Boolean.parseBoolean(value)));
			} else throw new FileNotFoundException("Could not find " + propFileName + "!");
		} catch (Exception e) {
			System.out.println("EXCEPTION: " + e.getMessage());
		}
	}

	public void toggle(String property) {
		this.debugValMap.replace(property, !this.debugValMap.get(property));
	}

	public boolean get(String property){
		return this.debugValMap.get(property);
	}

	public void set(String property, boolean value){
		this.debugValMap.replace(property,value);
	}
}
