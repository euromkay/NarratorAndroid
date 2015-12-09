package voss.android.setup;


import voss.android.CommunicatorPhone;
import voss.android.NarratorService;
import voss.android.wifi.ChatManager;
import voss.shared.logic.Player;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;


public class HostAdder implements SetupListener{

    private NarratorService ns;
    public HostAdder(NarratorService ns){
        this.ns = ns;
    }
    
    //s is the command given.  usually for setup
    //this is synchronized by local narrator from where its called
    public static void HostRead(String s, ChatManager cm, NarratorService ns){
    	if(s.startsWith(Constants.SUBMIT_NAME)){
    		s = s.substring(Constants.SUBMIT_NAME.length());
            if(ns.local.getPlayerByName(s) == null){
            	for (Player alreadyIn : ns.local.getAllPlayers()){
                	/*ArrayList<String> names = chatNames.get(cm);
                	if(names == null){
                		names = new ArrayList<String>();
                		chatNames.put(cm, names);
                	}else if(names.contains(alreadyIn.getName())){
                		names.toString();
                	}else{
                		names.add(alreadyIn.getName());
                	}*/
                    cm.write(Constants.NEW_PLAYER_ADDITION +alreadyIn.getName());
                }
                for (RoleTemplate rt: ns.local.getAllRoles())
                    cm.write(Constants.ADD_ROLE+rt.toIpForm());

                ns.addPlayer(s, new CommunicatorPhone());
                
            	cm.write(Constants.NAME_OK);//just a trigger to move on, add player in background
            	
                ns.socketHost.send(Constants.NEW_PLAYER_ADDITION + s, cm);
                cm.write(Constants.ALLOW_CONTROL + s);
            	
            }else{
            	cm.write(Constants.NAME_BAD + s);
            }
    	}else if(s.startsWith(Constants.NEW_PLAYER_ADDITION)) {
            s = s.substring(Constants.NEW_PLAYER_ADDITION.length());
            if(ns.local.getPlayerByName(s) == null){
            	ns.addPlayer(s, new CommunicatorPhone());
            	ns.socketHost.send(Constants.NEW_PLAYER_ADDITION + s, cm);
                cm.write(Constants.ALLOW_CONTROL + s);
            }else{
            	cm.write(Constants.NAME_BAD + s);
            }
        }else if(s.startsWith(Constants.REMOVE_PLAYER)) {
        	//for client player deletes
            String name = s.substring(Constants.REMOVE_PLAYER.length());
            
            ns.removePlayer(name);
            
            //we do this here, even though the hostAdder can do it, because the manager
            //might not be initialized, and we want this process to always complete
            ns.socketHost.send(Constants.REMOVE_PLAYER + s, cm);   
        }
        
    }

    
    

    public void onRoleAdd(RoleTemplate l){
    	ns.socketHost.write(Constants.ADD_ROLE+l.toIpForm());
    }

    public void onRoleRemove(RoleTemplate l){
        ns.socketHost.write(Constants.REMOVE_ROLE + l.toIpForm());
    }

    public void onPlayerAdd(String name, Communicator c){
        ns.socketHost.write(Constants.NEW_PLAYER_ADDITION + name);
    }

    public void onPlayerRemove(String name){
        ns.socketHost.write(Constants.REMOVE_PLAYER + name);
    }



}
