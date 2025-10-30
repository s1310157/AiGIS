package aigis.model.loader;

import java.util.Properties;

@SuppressWarnings("serial")
public class PropLoader extends Properties {
	
	public String getProperty(String key) {
		String val = super.getProperty(key);
		if (val != null) {
			val = val.split("#")[0].trim();
		}
		return val;
	}

}
