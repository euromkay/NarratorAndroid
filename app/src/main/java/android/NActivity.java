package android;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import android.parse.Server;

public abstract class NActivity extends FragmentActivity{
    public NarratorService ns;

    public Server server;
    protected boolean isStarted = false;
	private ServiceConnection sC = null;
    protected void connectNarrator(final NarratorConnectListener ncl){
    	server = new Server();
    	if(isStarted)
    		server.Start();
		Intent i = new Intent(this, NarratorService.class);
		startService(i);
		if(sC == null) {
			sC = new ServiceConnection() {
				public void onServiceConnected(ComponentName className, IBinder binder) {
					NarratorService.MyBinder b = (NarratorService.MyBinder) binder;
					ns = b.getService();
					ns.server = server;
					ns.activity = NActivity.this;
					if (ncl != null)
						ncl.onConnect();

				}

				public void onServiceDisconnected(ComponentName className) {
					ns = null;
				}
			};
			bindService(i, sC, Context.BIND_AUTO_CREATE);
		}
	}
    public void unbindNarrator(){
		if(ns!=null)
		try {
			unbindService(sC);
			sC = null;
		}catch(IllegalArgumentException|NullPointerException e){}
    }
    
    public boolean networkCapable(){
		return server.IsLoggedIn();
	}

	public interface NarratorConnectListener{
		void onConnect();
	}

	public static void SetFont(int id, Activity c, boolean header){
		TextView text = (TextView) c.findViewById(id);
		String s;
		if(header)
			s = "AbrilFatface-Regular.ttf";
		else
			s = "JosefinSans-Regular.ttf";
		Typeface font = Typeface.createFromAsset(c.getAssets(), s);
		text.setTypeface(font);
	}

	public static int ParseColor(Context context, int id) {
		final int version = Build.VERSION.SDK_INT;
		if (version >= 23) {
			return ContextCompat.getColor(context, id);
		} else {
			return context.getResources().getColor(id);
		}
	}

	public static void setTextColor(TextView v, String color){
		v.setTextColor(Color.parseColor(color));
	}
	
}