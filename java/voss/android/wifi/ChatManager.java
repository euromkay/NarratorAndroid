
package voss.android.wifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;
import voss.shared.logic.support.Constants;



public class ChatManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private MessageTarget mt;
    private ArrayList<Integer> players;
    private ChatListener chat;
    

    public static final String NAME = "name FOR SOCKETS";
    
    private String name;

    private int id;
    public ChatManager(Socket socket, Handler handler, MessageTarget mt, ChatListener chat, String name, int id) {
        Log.d(TAG, "new thread made");
        this.socket = socket;
        this.handler = handler;
        this.mt = mt;
        players = new ArrayList<>();
        this.chat = chat;
        this.name = name;
        this.id= id;
    }

    public void setHandler(Handler handler, ChatListener chat){
        this.handler = handler;
        this.chat = chat;
    }

    public void addID(int id){
        players.add(id);
    }

    public ArrayList<Integer> getIDs(){
        return players;
    }

    private InputStream iStream;
    private OutputStream oStream;
    private static final String TAG = "ChatManager";
    private static final int BUFFER = 1024;


    public void run() {
        try {

            oStream = socket.getOutputStream();
            iStream = socket.getInputStream();

            mt.onChatReady();
            if(chat != null)
                chat.onNewPlayer(this);
            byte[] buffer = new byte[BUFFER];
            int bytes;
            String line;
            //handler.obtainMessage(WiFiServiceDiscoveryActivity.MY_HANDLE, this).sendToTarget();

            while (true) {
                try {
                    // Read from the InputStream
                    bytes = iStream.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    line = new String(buffer, 0, bytes);

                    // Send the obtained bytes to the UI Activity
                    //if(!name.equals("Master"))
                    	Log.e(TAG, name + " Reading \t" + line);

                    handler.obtainMessage(WifiHost.MESSAGE_READ, bytes, -1, new Object[]{buffer, this}).sendToTarget();
                    buffer = new byte[BUFFER];

                } catch (IOException e) {
                    Log.e(TAG, "disconnected");
                    break;
                }
            }
        } catch (IOException e) {
        	e.getMessage();
        	Log.e(TAG, "exception");
        } finally {
        	close();
        }
    }

    private synchronized void write(byte[] buffer) {
        try {
        	while(oStream == null)
        		continue;
            oStream.write(buffer);
            oStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "null outstream");
        }
    }
    public void write(String s){
    	
        Log.e(TAG, name + " writing \t" + s + Constants.INET_SEPERATOR);
        write((s + Constants.INET_SEPERATOR).getBytes());
    }

    public void close(){
        Log.e(TAG, "closing sockets?!?");
        try {
            if(iStream != null)
                iStream.close();
            if(oStream != null)
                oStream.close();
            if(socket != null && !socket.isClosed())
                socket.close();
        } catch (IOException e) {
            Log.e("ChatManager", "chat closed");
            e.printStackTrace();
        }
    }




    public interface ChatListener {
        void onNewPlayer(ChatManager c);
        void onRead(String message, ChatManager p);
    }




	public int getID() {
		return id;
	}
}