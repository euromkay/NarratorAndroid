package android;

import org.json.JSONArray;
import org.json.JSONException;

public class JUtils{

	public static String getString(JSONArray jArray, int i) {
		try {
			return jArray.getString(i);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

}
