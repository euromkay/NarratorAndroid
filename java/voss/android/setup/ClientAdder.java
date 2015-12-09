package voss.android.setup;


import java.util.ArrayList;
import java.util.HashMap;

import voss.android.ActivitySettings;
import voss.android.CommunicatorPhone;
import voss.android.NarratorService;
import voss.android.wifi.ChatManager;
import voss.android.wifi.CommunicatorInternet;
import voss.android.wifi.SocketClient;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;


public class ClientAdder implements SetupListener{

    private SetupManager manager;
    public ClientAdder(SetupManager sm){
        manager = sm;
    }


    public HashMap<ChatManager, ArrayList<String>> chatNames = new HashMap<>();
    
    public static void ClientRead(String s, ChatManager cm, NarratorService ns){
        if(s.startsWith(Constants.NEW_PLAYER_ADDITION)){
            s = s.substring(Constants.NEW_PLAYER_ADDITION.length());
            ns.addPlayer(s, new CommunicatorNull());            
        }else if(s.startsWith(Constants.ALLOW_CONTROL)){
        	s = s.substring(Constants.ALLOW_CONTROL.length());
        	ns.socketClient.setName(s);
        	ns.addPlayer(s, new CommunicatorPhone());
        	
        }else if(s.startsWith(Constants.NAME_OK)){
        	ns.sc.onSuccess();//doesn't add the name, an allow control command is incoming
        }else if(s.startsWith(Constants.NAME_BAD)){
        	ns.sc.onFailure();
        	
        }else if(s.startsWith(Constants.REMOVE_PLAYER)) {//should never be the client
            String name = s.substring(Constants.REMOVE_PLAYER.length());
            
            ns.removePlayer(name);
            
            //Player toRemove = ns.global.getPlayerByName(name);
            //ns.global.removePlayer(toRemove);
            //toRemove = ns.local.getPlayerByName(name);
            //ns.local.removePlayer(toRemove);
            
        }else if(s.startsWith(Constants.ADD_ROLE)) {
            RoleTemplate l = RoleTemplate.FromIp(s.substring(Constants.ADD_ROLE.length()));
            ns.addRole(l);
        }else if(s.startsWith(Constants.REMOVE_ROLE)){
            RoleTemplate l = RoleTemplate.FromIp(s.substring(Constants.REMOVE_ROLE.length()));
            ns.removeRole(l);

        }else if(s.startsWith(Constants.START_GAME)){
        	Long l = Long.parseLong(s.substring(Constants.START_GAME.length()));
        	ns.startGame(l, ActivitySettings.getRules(ns));
        }
    }

    public void onRoleAdd(RoleTemplate l){
    	
    }

    public void onRoleRemove(RoleTemplate l){
    	
    }

    public void onPlayerAdd(String name, Communicator c){
        
    }

    public void onPlayerRemove(String name){
        
    }

	public static void SubmitName(String name, SocketClient socketClient) {
		socketClient.send(Constants.SUBMIT_NAME + name);
	}

}
