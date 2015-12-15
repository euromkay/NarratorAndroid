package voss.android;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public abstract class NActivity extends FragmentActivity{
    public NarratorService ns;

	private ServiceConnection sC;
    protected void connectNarrator(final NarratorConnectListener ncl){
		Intent i = new Intent(this, NarratorService.class);
		startService(i);
		sC = new ServiceConnection(){
			public void onServiceConnected(ComponentName className, IBinder binder) {
				NarratorService.MyBinder b = (NarratorService.MyBinder) binder;
                ns = b.getService();
				if(ncl != null)
					ncl.onConnect();
			}

			public void onServiceDisconnected(ComponentName className) {
				//toast("narrator background service disconnected");
			}
		};
		bindService(i, sC, Context.BIND_AUTO_CREATE);
	}
    public void finish(){
		try {
			unbindService(sC);
		}catch(IllegalArgumentException e){}
    	super.finish();
    }
    
    protected boolean networkCapable(){
		return Build.VERSION.SDK_INT >= 18;
	}

	public interface NarratorConnectListener{
		void onConnect();
	}
}
