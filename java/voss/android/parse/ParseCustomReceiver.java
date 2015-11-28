package voss.android.parse;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

public class ParseCustomReceiver extends ParsePushBroadcastReceiver{

    public void onReceive(Context context, Intent intent){
        Bundle b = intent.getExtras();
        Intent i = new Intent();
        try {
            JSONObject obj = new JSONObject(b.get("com.parse.Data").toString());
            i.putExtra("stuff", obj.getString("alert"));
        }catch(JSONException je){
            Log.e("ParseCustomReceiver", je.getMessage());
        }
        //Log.e("ParseCustomReceiver", "gotit!");
        i.setAction("PARSE_RECEIVED_ACTION");
        context.sendBroadcast(i);
    }

}
