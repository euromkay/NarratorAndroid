package android.wifi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.parse.Server;

public class SocketClient extends Service implements Runnable {

    public interface ClientListener {
    	void onHostConnect();
	}

	public static final String HOST_IP_ADDRESS = "host_ip_address";
    private static final String TAG = "SocketClient";
    private ChatManager chat;
    private String hostIp;

    public int onStartCommand(Intent i, int flags, int startId){
        if(Server.IsLoggedIn())
            return Service.START_STICKY;
        hostIp = i.getStringExtra(HOST_IP_ADDRESS);
        
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
            chat = new ChatManager(socket, handler, null, -1);
            new Thread(chat).start();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    Handler handler;
    Thread t;
    public void addHandler(Handler h){
        handler = h;
        if(t== null) {
            t = new Thread(this);
            t.start();
        }else if (chat != null){
            chat.setHandler(h);
        }
    }

    public ChatManager getChat() {
        return chat;
    }

    public void send(String s){
        if(Server.IsLoggedIn())
            return;
        chat.write(s);
    }

    public void onDestroy(){
        if(chat != null)
            chat.close();
    }

	public void shutdown() {
		onDestroy();		
	}

	public void setName(String name) {
		chat.setName(name);
	}

}