package voss.android.day;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import voss.android.texting.TextHandler;
import voss.android.wifi.MessageTarget;
import voss.android.wifi.SocketClient;
import voss.android.wifi.SocketHost;
import voss.android.wifi.WifiHost;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.listeners.CommandListener;
import voss.shared.logic.support.Constants;


public class IpDayListener implements CommandListener, ServiceConnection, Handler.Callback {

    private boolean isHost;
    private TextHandler th;
    private Narrator n;
    private ActivityDay a;
    public IpDayListener(boolean host, Narrator n, ActivityDay a){
        this.isHost = host;
        this.n = n;
        th = new TextHandler(n);
        this.a = a;



        Intent intent;
        if(host) 
            intent = new Intent(a, SocketHost.class);
        else
            intent = new Intent(a, SocketClient.class);
        
        a.bindService(intent, this, Context.BIND_AUTO_CREATE);
        
        n.addListener(this);
    }

    public void onCommand(String s){
    	mTarget.write(s);
    }


    MessageTarget mTarget;

    public void onServiceConnected(ComponentName className, IBinder binder) {
        if(isHost) {
            SocketHost.MyBinder b = (SocketHost.MyBinder) binder;
            SocketHost sh = b.getService();
            sh.connectPlayers(n);
            mTarget = b.getService();
        }else{
            SocketClient.MyBinder b = (SocketClient.MyBinder) binder;
            mTarget = b.getService();
        }
        mTarget.addHandler(new Handler(this), null);

        a.manager.initiate();
    }
    public void onServiceDisconnected(ComponentName className) {
        mTarget = null;
    }

    private String total_message = "";
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case WifiHost.MESSAGE_READ:
                Object[] obj = (Object[]) msg.obj;
                byte[] readBuf = (byte[]) obj[0];
                //ChatManager c = (ChatManager) obj[1];
                // construct a string from the valid bytes in the buffer
                synchronized (this) {
                    total_message = total_message + (new String(readBuf, 0, msg.arg1));
                    int loc;
                    String readMessage;
                    while (total_message.contains(Constants.INET_SEPERATOR)) {
                        loc = total_message.indexOf(Constants.INET_SEPERATOR);
                        readMessage = total_message.substring(0, loc);
                        try {
                            onRead(readMessage);
                        }catch(Exception e){
                            Toast.makeText(a, e.getMessage(), Toast.LENGTH_SHORT);
                        }
                        total_message = total_message.substring(loc + Constants.INET_SEPERATOR.length());
                    }
                }
                break;


        }
        return true;
    }

    public void onRead(String message){
        int index = message.indexOf(Constants.NAME_SPLIT);
        int id = Integer.parseInt(message.substring(0, index));
        Player owner = Narrator.getPlayerByID(n.getAllPlayers(), id);
        message = message.substring(index + Constants.NAME_SPLIT.length());
        message = message.replace("\n", "");

        synchronized (this){
        	if(!isHost)
        		n.removeListener(this);
        	try{
        		th.text(owner, message);
        	}catch(Error|Exception e){
        		e.printStackTrace();
        	}finally{
        		if(!isHost)
        			n.addListener(this);
        	}
        }
    }
}

