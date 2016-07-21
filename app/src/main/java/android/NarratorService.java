package android;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.parse.GameListing;
import android.parse.Server;
import android.screens.ActivityHome;
import android.setup.ClientAdder;
import android.setup.HostAdder;
import android.setup.SetupListener;
import android.setup.SetupManager;
import android.texting.StateObject;
import android.util.Log;
import android.wifi.ChatManager;
import android.wifi.SocketClient;
import android.wifi.SocketHost;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.exceptions.IllegalActionException;
import shared.logic.exceptions.PhaseException;
import shared.logic.support.CommandHandler;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorNull;
import shared.logic.support.Constants;
import shared.logic.support.Faction;
import shared.logic.support.FactionManager;
import shared.logic.support.RoleTemplate;

public class NarratorService extends Service implements Callback, SetupListener{

	
	public Narrator local;//this is the one i keep communicators in
	public FactionManager fManager;
	private CommandHandler ch;
	public int onStartCommand(Intent i, int flags, int startId){
		if(local == null)
			refresh();
        return Service.START_STICKY;
	}
	public void refresh(){
		local = Narrator.Default();
		fManager = new FactionManager(local);
		ch = new CommandHandler(local);

	}
	
    private final IBinder mBinder = new MyBinder();
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

	/*public void setRules(Rules rules, String original) {
		if(sManager==null)
			local.setRules(rules);
		else
			sManager.setRules(original);
	}*/

	public class MyBinder extends Binder {
        public NarratorService getService() {
            return NarratorService.this;
        }
    }
    
	public void shutdown() {
		
	}

	public Narrator getNarrator() {
		return local;
	}

	private StateObject stateObject(){
		return new StateObject(local, fManager){
			
			public boolean isActive(Player p) {
				return true;
			}

			public JSONObject getObject() throws JSONException {
				JSONObject jo = new JSONObject();
				JSONArray arr = new JSONArray();
				jo.put(StateObject.type, arr);
				return jo;
			}

			public void write(Player p, JSONObject jo) throws JSONException {
				
			}
			
		};
	}
	
	public JSONObject getFactions() throws JSONException{
		if(Server.IsLoggedIn()){
			return null;
		}else{
			StateObject so = stateObject();
			so.addState(StateObject.RULES);
			return so.send((Player) null).getJSONObject(StateObject.factions);
		}
	}

	public SocketHost socketHost;
	private ServiceConnection socketHostServiceConnection;
	public void startHost(Activity activity, String name) {
        Intent intent = new Intent(activity, SocketHost.class);
        intent.putExtra(ChatManager.NAME, name);
        activity.startService(intent);
        activity.bindService(intent, socketHostServiceConnection = new ServiceConnection(){
			public void onServiceConnected(ComponentName className, IBinder binder) {
                SocketHost.MyBinder b = (SocketHost.MyBinder) binder;
                socketHost = b.getService();
                socketHost.addHandler(new Handler(NarratorService.this));
			}

			public void onServiceDisconnected(ComponentName className) {
				socketHost = null;
			}
        }, Context.BIND_AUTO_CREATE);
	}


    private boolean isHost(){
    	return socketClient == null;
    }

	public SocketClient socketClient;
	private ServiceConnection socketClientServiceConnection;
	public void startClient(ActivityHome a, String ip, final SocketClient.ClientListener cl) {
		Intent intent = new Intent(this, SocketClient.class);
        intent.putExtra(SocketClient.HOST_IP_ADDRESS, ip);
        startService(intent);

        bindService(intent, socketClientServiceConnection = new ServiceConnection(){
			public void onServiceConnected(ComponentName className, IBinder binder) {
				SocketClient.MyBinder b = (SocketClient.MyBinder) binder;
				socketClient = b.getService();
                socketClient.addHandler(new Handler(NarratorService.this));
                
                cl.onHostConnect();
			}

			public void onServiceDisconnected(ComponentName className) {
				socketClient = null;
			}
        }, Context.BIND_AUTO_CREATE);
	}

	public static final int MESSAGE_READ = 0x400 + 1;
	public static final int MESSAGE_CONNECTED = 0x400 + 2;
	private String total_message = "";
	private Object messageLock = new Object();
	public boolean handleMessage(Message msg) {
		synchronized(messageLock){
			
		switch (msg.what) {
			case MESSAGE_CONNECTED:
				break;
	        case MESSAGE_READ:
	            Object[] obj = (Object[]) msg.obj;
	            byte[] readBuf = (byte[]) obj[0];
	            ChatManager c = (ChatManager) obj[1];
	            // construct a string from the valid bytes in the buffer
	            //synchronized (lock) {
	                total_message = total_message + (new String(readBuf, 0, msg.arg1));
	                int loc;
	                String readMessage;
	                while (total_message.contains(Constants.INET_SEPERATOR)) {
	                    loc = total_message.indexOf(Constants.INET_SEPERATOR);
	                    readMessage = total_message.substring(0, loc);
	                    onRead(readMessage, c);
	                    try{
	                    total_message = total_message.substring(loc + Constants.INET_SEPERATOR.length());
	                    }catch(Exception|Error e){
	                    	e.printStackTrace();
	                    	throw e;
	                    }
	                }
	            //}
	            break;
	    }
		
		}
	    return true;
	}

	public SuccessListener sc;
	public void submitName(String name, SuccessListener sc) {
		this.sc = sc;
		ClientAdder.SubmitName(name, socketClient);
	}
	
	public void onRead(String message, ChatManager c){
		synchronized(local){
			if(Server.IsLoggedIn()){
				try {
					ch.parseCommand(message);
				}catch(Throwable e){
					Log.e("NarratorService reading", "" + e.getMessage());
				}
			}else{
				if(!local.isStarted()){
					if(isHost()){
						HostAdder.HostRead(message, c, this);
					}else{
						ClientAdder.ClientRead(message, c, this);
					}
				}else{
					try{
						int sync = ch.parseCommand(message);
						if(isHost()){
							if( sync == CommandHandler.SYNCH ){
								socketHost.send(message);
							}else
								socketHost.send(message, c);
						}
					}catch(IllegalActionException|PhaseException e){
						Log.e("onRead-NarratorService", e.getMessage());
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	public void disconnect(ServiceConnection sC, Activity a){
		try {
			a.unbindService(sC);
		}catch(IllegalArgumentException e){

		}
	}

	
	
	
	
	
	
	
	
	private SetupManager sManager;
	public void setSetupManager(SetupManager sm){
		sManager = sm;
	}
	public void removeSetupManager(){
		sManager = null;
	}
	public void addRole(String name, String color){
		//if server is hosting narrator instance, then you don't do anything here.  you just have to track the object that the server gives you
		//if you're hosting
		if(Server.IsLoggedIn()){
			
		}else{
			RoleTemplate rt = null;
			Faction f = fManager.getFaction(color);
			if(f != null)
				rt = f.getRole(name);
			if(rt == null){
				f = fManager.getFaction(Constants.A_NEUTRAL);
				rt = f.getRole(name);
				if(rt == null){
					f = fManager.getFaction(Constants.A_RANDOM);
					rt = f.getRole(name);
				}
			}else{
				rt = f.getRole(name);
			}

			local.addRole(rt);
			//onRoleAdd(SetupManager.TranslateRole(rt));
			if(sManager != null)
				sManager.addRole(rt);
		}
	}
	public void removeRole(RoleTemplate rt){
		if(sManager == null){
			onRoleRemove(rt);
		}else{
			sManager.removeRole(rt.getName(), rt.getColor());
		}
	}
	
	
	public void onRoleAdd(RoleTemplate s) {
		local.addRole(s);
	}

	public void onRoleRemove(RoleTemplate s) {
		local.removeRole(s);
	}
	
	public Player addPlayer(String name, Communicator c) {
		Player p = local.addPlayer(name, c);
		
		if(sManager != null){//just looking for a trigger here
			sManager.addPlayer(name, (Communicator) null); 
		}
		
		return p;
	}
	
	public void setComputer(String s){
		local.getPlayerByName(s).setComputer();
	}
	
	public void onPlayerAdd(String s, Communicator c) {
		if(c == null)
			c = new CommunicatorNull();
		local.addPlayer(s, c);
	}
	
	public void removePlayer(String name){
		if(sManager != null){
			sManager.removePlayer(name, true);//tells manager to not tell the hostAdder
		}else{
			onPlayerRemove(name);
		}
	}

	public void onPlayerRemove(String s) {
		local.removePlayer(s);
	}

	
	
	
	private GameListing gl;
	public void setGameListing(GameListing gl){
		if(gl == null)
			throw new NullPointerException("Game Listing cannot be null.");
		this.gl = gl;
	}
	
	public GameListing getGameListing() {
		return gl;
	}
	
	
	
	public synchronized void startGame(long seed){
		if(!local.isStarted()) {
			local.setSeed(seed);
			local.startGame();
		}


		sManager.startDay();

		if(!Server.IsLoggedIn() && isHost() && socketHost != null){
			if (socketHost.sockets.isEmpty()){
				socketHost.onDestroy();
			}else{
				socketHost.write(Constants.START_GAME + seed);
			}
		}
		removeSetupManager();
	}

	public String getIp() {
        WifiManager wm = (WifiManager) getSystemService(Activity.WIFI_SERVICE);
        int ip = wm.getConnectionInfo().getIpAddress();
        String ip_addr = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        return ip_addr;
	}

	public void onDestroy(){
		if(socketClient!=null)
			try{
				unbindService(socketClientServiceConnection);
			}catch(IllegalArgumentException|NullPointerException e){}
		if(socketHost!=null) {
			try {
				unbindService(socketHostServiceConnection);
			} catch (IllegalArgumentException|NullPointerException e) {}
		}
		Log.e("NarratorService", "ondestroy triggered");
	}
	public boolean isInProgress() {
		if(Server.IsLoggedIn()){
			return false;
		}else{
			return local.isInProgress();
		}
	}
	public boolean isDay() {
		if(Server.IsLoggedIn())
			return false;
		else
			return local.isDay();
	}
}
