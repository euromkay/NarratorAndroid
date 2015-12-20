package voss.android.parse;


import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import voss.android.R;
import voss.android.screens.ActivityHome;

public class ParseCustomReceiver extends ParsePushBroadcastReceiver{

    public void onReceive(Context context, Intent intent){
        Bundle b = intent.getExtras();
        Intent i = new Intent();
        try {
            JSONObject obj = new JSONObject(b.get("com.parse.Data").toString());
            i.putExtra("stuff", obj.getString("alert"));

            if(obj.has("phase")){
                notify(getPhaseString(obj.getString("phase")), context);
            }
            if(obj.has("start")){
                notify("Game by " + obj.getString("start") + " has started!", context);
            }


        }catch(JSONException je){
            Log.e("ParseCustomReceiver", je.getMessage());
        }
        i.setAction("PARSE_RECEIVED_ACTION");
        context.sendBroadcast(i);


    }

    private String getPhaseString(String s){
        double d= Double.valueOf(s);
        if (d==(int)d){
            return "Day " + ((int) d) + " has started!";
        }else{
            return "Night " + ((int) d) + " has started!";
        }
    }

    private void notify(String s, Context context){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.home)
                        .setContentTitle("Mafia")
                        .setContentText(s);

        Intent result = new Intent(context, ActivityHome.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        result,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId = 001;
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }
}
