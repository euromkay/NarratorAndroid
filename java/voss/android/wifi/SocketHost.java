package voss.android.wifi;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import voss.logic.Narrator;


public class SocketHost extends Service implements MessageTarget, Runnable{

    ServerSocket socket = null;
    private static final int THREAD_COUNT = 10;
    private static final String TAG = "SocketHost";
    private String name;

    public int onStartCommand(Intent i, int flags, int startId){
    	name = i.getStringExtra(ChatManager.NAME);
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
        return Service.START_STICKY;
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

    private ArrayList<ChatManager> sockets = new ArrayList<>();

    public void write(String s){
        for (ChatManager cm: sockets){
            cm.write(s);
        }
    }

    public void onChatReady(){

    }

    public void connectPlayers(Narrator n){
        for (ChatManager c: sockets){
            for (int id: c.getIDs()){
                n.getPlayerByID(id).setCommunicator(new CommunicatorInternet(c));
            }
        }
    }

    Handler handler;
    ChatManager.ChatListener chat;
    Thread t;
    public void addHandler(Handler h, ChatManager.ChatListener chat){
        handler = h;
        this.chat = chat;

        for(ChatManager cm: sockets){
            cm.setHandler(h, chat);
        }
    }

    private int i = 0;
    public void run() {
        while (true) {
            try {
                Log.d(TAG, "starting threadx2");
                ChatManager cm = new ChatManager(socket.accept(), handler, this, chat, name, i++);
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