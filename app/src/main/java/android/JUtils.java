package android;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;

public class JUtils{

	public static String getString(JSONArray jArray, int i) {
		try {
			return jArray.getString(i);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getString(JSONObject jObject, String key){
		try {
			return jObject.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int getInt(JSONObject jObject, String key){
		try {
			return jObject.getInt(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static JSONArray getJSONArray(JSONObject jo, String key){
		try {
			return jo.getJSONArray(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static JSONObject getJSONObject(JSONArray jo, int i){
		try {
			return jo.getJSONObject(i);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void put(JSONObject jo, String key, String message){
		try {
			jo.put(key, message);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
}
