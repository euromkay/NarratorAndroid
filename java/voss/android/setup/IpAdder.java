package voss.android.setup;


import java.util.ArrayList;

import voss.android.CommunicatorPhone;
import voss.android.wifi.ChatManager;
import voss.android.wifi.CommunicatorInternet;
import voss.android.wifi.WifiHost;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;


public class IpAdder implements SetupListener{

    private SetupManager manager;
    private WifiHost wifi;
    public IpAdder(SetupManager sm, WifiHost wifi){
        manager = sm;
        this.wifi = wifi;
    }

    public void hostRead(String s, ChatManager cm){
        Narrator narrator = manager.getNarrator();
        synchronized(narrator){
        	
        if(s.startsWith(Constants.NEW_PLAYER_ADDITION)) {
            s = s.substring(Constants.NEW_PLAYER_ADDITION.length());
            manager.addPlayer(s, cm);
            
        }else if(s.startsWith(Constants.CONFIRM_PLAYER_ADD)){
        	s = s.substring(Constants.CONFIRM_PLAYER_ADD.length());
        	int id = Integer.parseInt(s);
        	Player p = narrator.getPlayerByID(id);
        	if(p.getCommunicator().getClass() == CommunicatorInternet.class){
        		CommunicatorInternet ci = (CommunicatorInternet) p.getCommunicator();
        		if (ci.getManager() == cm)
        			cm.write(Constants.ALLOW_CONTROL + p.getID());
        	}
        }else if(s.startsWith(Constants.REMOVE_PLAYER)) {
            int id = Integer.parseInt(s.substring(Constants.REMOVE_PLAYER.length()));
            Player toRemove = manager.getNarrator().getPlayerByID(id);
            if (toRemove != null)
                manager.removePlayer(toRemove);

        }else if(s.startsWith(Constants.START_GAME)){
            long seed = Long.parseLong(s.substring(Constants.START_GAME.length()));
            manager.startGame(seed);
        }else if(s.startsWith(Constants.REQUEST_INFO)){
            for (Player alreadyIn : narrator.getAllPlayers())
                cm.write(Constants.NEW_PLAYER_ADDITION + alreadyIn.getID()+Constants.SEPERATOR+alreadyIn.getName());

            for (RoleTemplate rt: narrator.getAllRoles())
                cm.write(Constants.ADD_ROLE+rt.toIpForm());

            cm.write(Constants.FINISH_INITIAL_REQUEST);
        }
        }
    }

    public void clientRead(String s, ChatManager cm){
        if(s.startsWith(Constants.NEW_PLAYER_ADDITION)){
            s = s.substring(Constants.NEW_PLAYER_ADDITION.length());
            String[] sections = s.split(Constants.SEPERATOR);
            int id = Integer.parseInt(sections[0]);
            
            manager.addPlayer(sections[1], id, null); //null means not from the client
            
            
            
        }else if(s.startsWith(Constants.ALLOW_CONTROL)){
        	s = s.substring(Constants.ALLOW_CONTROL.length());
        	int id = Integer.parseInt(s);
        	Player p = manager.getNarrator().getPlayerByID(id);
        	p.setCommunicator(new CommunicatorPhone());
        }else if(s.startsWith(Constants.REMOVE_PLAYER)) {//should never be the client
            PlayerList allPlayers = manager.getNarrator().getAllPlayers();
            int id = Integer.parseInt(s.substring(Constants.REMOVE_PLAYER.length()));
            Player toRemove = manager.getNarrator().getPlayerByID(id);
            if(toRemove != null)
                manager.removePlayer(toRemove);

        }else if(s.startsWith(Constants.NAME_CHANGE)){
            s = s.substring(Constants.NAME_CHANGE.length());
            String[] sections = s.split(Constants.SEPERATOR);
            PlayerList allPlayers = manager.getNarrator().getAllPlayers();
            int id = Integer.parseInt(sections[0]);
            Narrator.getPlayerByID(allPlayers, id).setName(sections[1]);

        }else if(s.startsWith(Constants.ADD_ROLE)) {
            RoleTemplate l = RoleTemplate.FromIp(s.substring(Constants.ADD_ROLE.length()));
            manager.addRole(l);

        }else if(s.startsWith(Constants.REMOVE_ROLE)){
            RoleTemplate l = RoleTemplate.FromIp(s.substring(Constants.REMOVE_ROLE.length()));
            manager.removeRole(l);

        }else if(s.startsWith(Constants.FINISH_INITIAL_REQUEST)) {
            cm.write(Constants.NEW_PLAYER_ADDITION + manager.getName());
        }else if(s.startsWith(Constants.START_GAME)){
        	Long l = Long.parseLong(s.substring(Constants.START_GAME.length()));
        	manager.startGame(l);
        }
    }

    public void onRoleAdd(RoleTemplate l){
        if(manager.isHost())
            wifi.write(Constants.ADD_ROLE+l.toIpForm());
    }

    public void onRoleRemove(RoleTemplate l){
        if(manager.isHost())
            wifi.write(Constants.REMOVE_ROLE + l.toIpForm());
    }

    public void onPlayerAdd(Player newPlayer){
        if(manager.isHost())
            wifi.write(AddPlayerIPFormat(newPlayer));
        else{
            wifi.write(Constants.CONFIRM_PLAYER_ADD+newPlayer.getID());
        }
    }

    public static String AddPlayerIPFormat(Player newPlayer){
        return Constants.NEW_PLAYER_ADDITION + newPlayer.getID()+Constants.SEPERATOR+newPlayer.getName();
    }

    public void onPlayerRemove(Player p){
        String message = Constants.REMOVE_PLAYER + p.getID();
        wifi.write(message);
        manager.wifi.write(message);
    }

    public void onNameChange(Player p, String name){
        if(manager.isHost())
        	wifi.write(Constants.NAME_CHANGE+p.getID()+Constants.SEPERATOR+name);
    }

    public void startGame(long seed){
        if(manager.isHost()) {
        	wifi.write(Constants.START_GAME + seed);
        }
    }

    private PlayerList getSocketCommunicators(){
        PlayerList list = new PlayerList();
        ArrayList<Communicator> alreadyAdded = new ArrayList<>();
        for(Player p: manager.getNarrator().getAllPlayers()){
            Communicator c = p.getCommunicator();
            if(c.getClass() == CommunicatorInternet.class){
                if(!alreadyAdded.contains(c)) {
                    list.add(p);
                    alreadyAdded.add(c);
                }
            }
        }
        return list;
    }

}
