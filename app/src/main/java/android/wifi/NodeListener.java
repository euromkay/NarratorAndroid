package android.wifi;

import json.JSONException;
import json.JSONObject;

public interface NodeListener {
	boolean onMessageReceive(JSONObject jo) throws JSONException;
}