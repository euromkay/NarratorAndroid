package voss.android.wifi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import voss.shared.logic.Narrator;
import voss.shared.logic.support.Constants;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class SocketHost extends Service implements Runnable{

    ServerSocket socket = null;
    private static final int THREAD_COUNT = 10;
    private static final String TAG = "SocketHost";
    private String name;

    public int onStartCommand(Intent i, int flags, int startId){
        if (i == null){
            Log.e(TAG, "Start was null ------------------------->");
            return 0;
        }
    	name = i.getStringExtra(ChatManager.NAME);
        if (socket == null){
            try {
                socket = new ServerSocket(4545);
                if (t == null) {
                    pool.execute(this);
                    Log.d(TAG, "starting thread");
                }
                Log.d("SocketHost", "Socket Started");
            } catch(BindException f){

                pool.shutdownNow();
                Log.e("SocketHost", "address in use");
                throw new NullPointerException("address in use");
            } catch (IOException e) {
                e.printStackTrace();
                pool.shutdownNow();
            }
        }
        return Service.START_STICKY;
    }
    
    public boolean isLive(){
    	return socket != null;
    }
    
    public void onDestroy(){
        for(ChatManager cm: sockets)
            cm.close();
        try {
            if (socket != null)
                socket.close();
        }catch(IOException e){

        }
        pool.shutdown();
    }

    private final IBinder mBinder = new MyBinder();
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
    public class MyBinder extends Binder {
        public SocketHost getService() {
            return SocketHost.this;
        }
    }

    private final ThreadPoolExecutor pool = new ThreadPoolExecutor(
            THREAD_COUNT, THREAD_COUNT, 10, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    public ArrayList<ChatManager> sockets = new ArrayList<>();

    public void send(String s){

        Log.e("\t" + TAG + " " + sockets.size(), name + " writing \t" + s + Constants.INET_SEPERATOR);
        for (ChatManager cm: sockets){
            cm.write(s);
        }
    }
    public void send(String s, ChatManager notThisOne){
    	for (ChatManager cm: sockets){
    		if(cm != notThisOne){
    			cm.write(s);
    		}
        }
    }
    public void write(String s){
    	send(s);
    }

    public void onChatReady(){

    }

    public void connectPlayers(Narrator n){
        for (ChatManager c: sockets){
            for (String name: c.getNames()){
                n.getPlayerByName(name).setCommunicator(new CommunicatorInternet(c));
            }
        }
    }

    Handler handler;
    Thread t;
    public void addHandler(Handler h){
        handler = h;

        for(ChatManager cm: sockets){
            cm.setHandler(h);
        }
    }

    private int i = 0;
    public void run() {
        while (true) {
            try {
                Log.d(TAG, "starting threadx2");
                ChatManager cm = new ChatManager(socket.accept(), handler, name, i++);
                sockets.add(cm);
                try {
                    pool.execute(cm);
                }catch(RejectedExecutionException e){
                    Log.e(TAG, pool.getActiveCount()+"");
                    throw e;
                }
            } catch (SocketException f){
            	break;
            } catch (IOException e) {
                try {
                    if (socket != null && !socket.isClosed())
                        socket.close();
                } catch (IOException ioe) {
                	
                }
                e.printStackTrace();
                pool.shutdownNow();
                break;
            }
        }
    }
    
	public void shutdown() {
		onDestroy();
		
	}

}