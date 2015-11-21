package voss.android.wifi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import voss.android.wifi.ChatManager;

public class SocketClient extends Service implements MessageTarget, Runnable {

    public static final String HOST_IP_ADDRESS = "10.53.48.29";
    private static final String TAG = "SocketClient";
    private ChatManager chat;
    private String hostIp;
    private String name;

    public int onStartCommand(Intent i, int flags, int startId){
        if(hostIp == null){
        	name = i.getStringExtra(ChatManager.NAME);
            hostIp = i.getStringExtra(HOST_IP_ADDRESS);
        }
        return Service.START_STICKY;
    }

    private final IBinder mBinder = new MyBinder();
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
    public class MyBinder extends Binder {
        public SocketClient getService() {
            return SocketClient.this;
        }
    }

    static final int SERVER_PORT = 4545;
    public void run() {
        Socket socket = new Socket();
        try {
            socket.bind(null);
            socket.connect(new InetSocketAddress(hostIp, SERVER_PORT), 5000);
            Log.d(TAG, "Launching the I/O handler");
            chat = new ChatManager(socket, handler, this, null, name, -1);
            new Thread(chat).start();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO detect connectException
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public void onChatReady(){
        if(c != null)
            c.onNewPlayer(chat);
    }

    Handler handler;
    Thread t;
    ChatManager.ChatListener c;
    public void addHandler(Handler h, ChatManager.ChatListener c){
        handler = h;
        this.c = c;
        if(t== null) {
            t = new Thread(this);
            t.start();
        }else if (chat != null){
            chat.setHandler(h, c);
        }
    }

    public ChatManager getChat() {
        return chat;
    }

    public void write(String s){
        chat.write(s);
    }

    public void onDestroy(){
        if(chat != null)
            chat.close();
    }

	public void shutdown() {
		onDestroy();		
	}

}