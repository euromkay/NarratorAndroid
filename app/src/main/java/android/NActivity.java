package android;

import android.alerts.RoleCardPopUp;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.day.ActivityDay;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.IBinder;
import android.setup.ActivityCreateGame;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;

import android.parse.Server;
import android.widget.Toast;

import java.util.List;

import shared.logic.Member;

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
		if(ns != null)
			ns.activity = this;
		if(sC == null) {
			sC = new ServiceConnection() {
				public void onServiceConnected(ComponentName className, IBinder binder) {
					NarratorService.MyBinder b = (NarratorService.MyBinder) binder;
					ns = b.getService();
					ns.server = server;
					ns.activity = NActivity.this;
					synchronized(ns){
						if(ns.activity.getClass().equals(ActivityDay.class))
							ns.pendingDay = false;
						else if(ns.activity.getClass().equals(ActivityCreateGame.class))
							ns.pendingCreate = false;
					}
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
		try {
			unbindService(sC);
		} catch (IllegalArgumentException | NullPointerException e) {}
		sC = null;
		if(ns!=null) {
			if(ns.activity == this)
				ns.activity = null;
		}

    }
    
    public boolean networkCapable(){
		return server.IsLoggedIn();
	}

	public interface NarratorConnectListener{
		void onConnect();
	}

	static Typeface headerFont = null, subFont = null;

	public static void SetFont(TextView text, Context c, boolean header){
		String s;
		Typeface font;
		if(header) {
			if(headerFont == null){
				s = "AbrilFatface-Regular.ttf";
				headerFont = Typeface.createFromAsset(c.getAssets(), s);
			}
			font = headerFont;
		}else {
			if (subFont == null) {
				s = "JosefinSans-Regular.ttf";
				subFont = Typeface.createFromAsset(c.getAssets(), s);
			}
			font = subFont;
		}
		text.setTypeface(font);
	}

	public static void SetFont(int id, Activity c, boolean header){
		TextView text = (TextView) c.findViewById(id);
		SetFont(text, c, header);
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

	public Toast toast(String message){
		Toast t = Toast.makeText(this, message, Toast.LENGTH_LONG);
		t.show();
		return t;
	}

	public RoleCardPopUp roleCardPopUp;
	public abstract List<Member> setMembers();
}