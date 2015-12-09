package voss.android;


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
import voss.android.parse.Server;
import voss.android.screens.ActivityHome;
import voss.android.setup.ClientAdder;
import voss.android.setup.HostAdder;
import voss.android.setup.SetupListener;
import voss.android.setup.SetupManager;
import voss.android.wifi.ChatManager;
import voss.android.wifi.SocketClient;
import voss.android.wifi.SocketHost;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Rules;
import voss.shared.logic.exceptions.IllegalActionException;
import voss.shared.logic.exceptions.PhaseException;
import voss.shared.logic.support.CommandHandler;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;

public class NarratorService extends Service implements Callback, SetupListener{

	
	public Narrator local;//this is the one i keep communicators in
	private CommandHandler ch;
	public int onStartCommand(Intent i, int flags, int startId){
		if(local == null){
			local = Narrator.Default();
			ch = new CommandHandler(local);
		}
        return Service.START_STICKY;
	}
	
    private final IBinder mBinder = new MyBinder();
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }
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

	
	

//	public static final boolean GLOBAL = false;
//	public static final boolean LOCAL = true;

	public SocketHost socketHost;
	public void startHost(Activity activity, String name) {
        Intent intent = new Intent(activity, SocketHost.class);
        intent.putExtra(ChatManager.NAME, name);
        activity.startService(intent);
        activity.bindService(intent, new ServiceConnection(){
			public void onServiceConnected(ComponentName className, IBinder binder) {
                SocketHost.MyBinder b = (SocketHost.MyBinder) binder;
                socketHost = b.getService();
                socketHost.addHandler(new Handler(NarratorService.this));
			}

			public void onServiceDisconnected(ComponentName className) {
				
			}
        }, Context.BIND_AUTO_CREATE);
		
	}


    private boolean isHost(){
    	return socketClient == null;
    }

	public SocketClient socketClient;
	public void startClient(ActivityHome a, String ip, SocketClient.ClientListener cl) {
		Intent intent = new Intent(this, SocketClient.class);
        intent.putExtra(SocketClient.HOST_IP_ADDRESS, ip);
        startService(intent);

        bindService(intent, new ServiceConnection(){
			public void onServiceConnected(ComponentName className, IBinder binder) {
				SocketClient.MyBinder b = (SocketClient.MyBinder) binder;
				socketClient = b.getService();
                socketClient.addHandler(new Handler(NarratorService.this));
                
                cl.onHostConnect();
			}

			public void onServiceDisconnected(ComponentName className) {
				
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
	
	private void onRead(String message, ChatManager c){
		synchronized(local){
			if(Server.IsLoggedIn()){
				
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
						System.err.println(message);
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	
	public void disconnect(ServiceConnection sC, Activity a){
		a.unbindService(sC);
	}

	
	
	
	
	
	
	
	
	private SetupManager sManager;
	public void setSetupManager(SetupManager sm){
		sManager = sm;
	}
	public void removeSetupManager(){
		sManager = null;
	}
	public void addRole(RoleTemplate rt){
		if(sManager == null){
			onRoleAdd(SetupManager.TranslateRole(rt));
		}else{
			sManager.addRole(rt);
		}
	}
	public void removeRole(RoleTemplate rt){
		if(sManager == null){
			onRoleRemove(rt);
		}else{
			sManager.removeRole(rt);
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

	
	
	
	
	
	
	public String getGameListing() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	public void startGame(long seed, Rules r){
		local.setSeed(seed);
		local.setRules(r);
		local.startGame();
		
		if(isHost()){
			if (socketHost.sockets.isEmpty()){
				socketHost.onDestroy();
			}else{
				socketHost.write(Constants.START_GAME + seed);
			}
		}else{
			sManager.startDay();
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

	
}
