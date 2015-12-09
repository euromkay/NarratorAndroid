
package voss.android.wifi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import android.os.Handler;
import android.util.Log;
import voss.android.NarratorService;
import voss.shared.logic.support.Constants;



public class ChatManager implements Runnable {

    private Socket socket = null;
    private Handler handler;
    private ArrayList<String> players;
    

    public static final String NAME = "name FOR SOCKETS";
    
    private String name;

    private int id;
    public ChatManager(Socket socket, Handler handler, String name, int id) {
        Log.d(TAG, "new thread made");
        this.socket = socket;
        this.handler = handler;
        players = new ArrayList<>();
        this.setName(name);
        this.id= id;
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    public void addName(String name){
        players.add(name);
    }

    public ArrayList<String> getNames(){
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
            handler.obtainMessage(NarratorService.MESSAGE_CONNECTED, -1, -1, new Object[]{}).sendToTarget();


            byte[] buffer = new byte[BUFFER];
            int bytes;
            String line;

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
                    Log.e(TAG, getName() + " Reading \t" + line);

                    handler.obtainMessage(NarratorService.MESSAGE_READ, bytes, -1, new Object[]{buffer, this}).sendToTarget();
                    buffer = new byte[BUFFER];
                } catch (SocketException e) {
                	if(e.getMessage().equals("Socket closed"))
                		break;
                	throw e;
                } catch (Exception|Error e) {
                	e.printStackTrace();
                    Log.e(TAG, "disconnected");
                    break;
                }
            }
        } catch (IOException e) {
        	e.printStackTrace();
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



	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isClosed(){
		return socket.isClosed();
	}
}