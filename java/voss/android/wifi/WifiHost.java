package voss.android.wifi;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;

import voss.android.setup.ActivityCreateGame;
import voss.shared.logic.support.Constants;


public class WifiHost implements Handler.Callback{

    Activity activity;



    public WifiHost(Activity activity, ChatManager.ChatListener chatListener) {
        this.activity = activity;
        this.chatListener = chatListener;
    }

    public void start(boolean isHost){
        if(isHost)
            startService();
        else
            discover(getIp());
    }



    public void setIp(String s){
        ip = s;
    }

    private String ip;
    public String getIp(){
        if(ip != null)
            return ip;
        WifiManager wm = (WifiManager) activity.getSystemService(Activity.WIFI_SERVICE);
        int ip = wm.getConnectionInfo().getIpAddress();
        String ip_addr = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        this.ip = ip_addr;
        return getIp();
    }

    private void startService() {

        Intent intent = new Intent(activity, SocketHost.class);
        intent.putExtra(ChatManager.NAME, ((ActivityCreateGame) activity).getManager().getName());
        sC = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder binder) {
                SocketHost.MyBinder b = (SocketHost.MyBinder) binder;
                mTarget = b.getService();
                mTarget.addHandler(new Handler(WifiHost.this), chatListener);
            }

            public void onServiceDisconnected(ComponentName className) {
                mTarget = null;
            }
        };
        activity.startService(intent);
        activity.bindService(intent, sC, Context.BIND_AUTO_CREATE);
    }


    ServiceConnection sC;
    MessageTarget mTarget;




    final HashMap<String, String> buddies = new HashMap<String, String>();

    public static void StartConnection(Activity a, String ip, String name){

        Intent intent = new Intent(a, SocketClient.class);
        intent.putExtra(ChatManager.NAME, name);
        intent.putExtra(SocketClient.HOST_IP_ADDRESS, ip);

        a.startService(intent);
    }

    private ChatManager.ChatListener chatListener;
    public void discover(String ip){
        Intent intent = new Intent(activity, SocketClient.class);
        intent.putExtra(ActivityCreateGame.IP_KEY, ip);
        sC = new ServiceConnection() {

            public void onServiceConnected(ComponentName className, IBinder binder) {
                SocketClient.MyBinder b = (SocketClient.MyBinder) binder;
                mTarget = b.getService();
                mTarget.addHandler(new Handler(WifiHost.this), chatListener);
                toast("Connected as peer");
            }
            public void onServiceDisconnected(ComponentName className) {
                mTarget = null;
            }
        };
        activity.bindService(intent, sC, Context.BIND_AUTO_CREATE);
    }




    private Object lock = new Object();
    public static final int MESSAGE_READ = 0x400 + 1;
    private String total_message = "";
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_READ:
                Object[] obj = (Object[]) msg.obj;
                byte[] readBuf = (byte[]) obj[0];
                ChatManager c = (ChatManager) obj[1];
                // construct a string from the valid bytes in the buffer
                synchronized (lock) {
                    total_message = total_message + (new String(readBuf, 0, msg.arg1));
                    int loc;
                    String readMessage;
                    while (total_message.contains(Constants.INET_SEPERATOR)) {
                        loc = total_message.indexOf(Constants.INET_SEPERATOR);
                        if(loc < -1)
                        	throw new NullPointerException();
                        readMessage = total_message.substring(0, loc);
                        chatListener.onRead(readMessage, c);
                        total_message = total_message.substring(loc + Constants.INET_SEPERATOR.length());
                    }
                }
                break;
        }
        return true;
    }

    public void disconnect(){
        try {
            activity.unbindService(sC);
        }catch(IllegalArgumentException e){

        }
    }

    public void shutdown(boolean isHost){
        disconnect();
        Class<?> c;
        if(isHost)
            c = SocketHost.class;
        else
            c = SocketClient.class;
        //activity.stopService(new Intent(activity, c));

    }

    public void write(String s){

        mTarget.write(s);
    }

    public void toast(String s){
        Toast.makeText(activity, s, Toast.LENGTH_SHORT).show();
        log(s);
    }

    private void log(String s){
        Log.e("WifiHost", s);
    }
}
