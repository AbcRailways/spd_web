package views;

import java.util.HashMap;
import java.util.Map;

public class HelperUtils {

	public static Map<String, Object> dfOptions(int width, int height){
		Map<String,Object> options = new HashMap<String, Object>();
		
        options.put("modal", true);
        options.put("width", width);
        options.put("height", height);
        options.put("contentWidth", "100%");
        options.put("contentHeight", "100%");
        options.put("size", "auto");
        options.put("closable", true);        
        options.put("draggable", true);
        
        return options;
	}
}
