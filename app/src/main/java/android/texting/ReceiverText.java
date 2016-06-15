package android.texting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class ReceiverText extends BroadcastReceiver {

	public static final String PDUS = "pdus";

	public void onReceive(Context context, Intent intent){
		Bundle b = intent.getExtras();
		if(b != null){
			String number = "";
			String message = "";
			Object[] pdus = (Object[]) b.get(PDUS);
			for(int i = 0; i < pdus.length; i++){
				SmsMessage text = SmsMessage.createFromPdu((byte[])pdus[i]);
				number += text.getOriginatingAddress();
				message += text.getMessageBody().toString();
			}
			Intent i = new Intent();
			i.setAction("SMS_RECEIVED_ACTION");
			i.putExtra("message",  message.replaceAll("[^a-zA-Z0-9 ]",""));
			i.putExtra("number", number.replaceAll("[^a-zA-Z0-9 ]",""));
			context.sendBroadcast(i);
		}
		
	}
	
	public static void sendText(PhoneNumber number, String message){
		try{
			SmsManager.getDefault().sendTextMessage(number.toString(), null, message, null, null);
		}catch(IllegalArgumentException e){
			Log.d("text message crashed", "crahsed with text message :" + message);
		}catch(NullPointerException f){
			Log.d("text message crashed", "crahsed with text message :" + message);
		}
	}

}