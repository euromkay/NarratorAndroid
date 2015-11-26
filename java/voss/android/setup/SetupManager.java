package voss.android.setup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import voss.android.ActivitySettings;
import voss.android.CommunicatorPhone;
import voss.android.day.ActivityDay;
import voss.android.parse.Server;
import voss.android.parse.ServerResponder;
import voss.android.screens.ActivityHome;
import voss.android.texting.CommunicatorText;
import voss.android.texting.PhoneNumber;
import voss.android.texting.TextHandler;
import voss.android.wifi.ChatManager;
import voss.android.wifi.CommunicatorInternet;
import voss.android.wifi.WifiHost;
import voss.shared.ai.Computer;
import voss.shared.logic.Member;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.exceptions.IllegalGameSettingsException;
import voss.shared.logic.exceptions.IllegalRoleCombinationException;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.roles.RandomRole;
import voss.packaging.Board;


public class SetupManager implements ChatManager.ChatListener {

    private boolean isHost;
    private String name;
    public WifiHost wifi;

    private ActivityCreateGame screen;

    private Narrator narrator;
    public TextAdder textAdder;

    private IpAdder ipAdder;
    private SetupScreenController screenController;

    private ArrayList<SetupListener> listeners;

    private IntentFilter intentFilter;

    private Random rand;
    
    public SetupManager(boolean isHost, ActivityCreateGame a, Narrator narrator){

        this.isHost = isHost;
        screen = a;
        this.narrator = narrator;
        listeners = new ArrayList<>();

        this.rand = new Random();
        
        if(Narrator.DEBUG && isHost && narrator.getAllRoles().size() == 0 && false)
            debugSettings();


        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVED_ACTION");

        textAdder = new TextAdder(this);

        

        screenController = new SetupScreenController(a);
        synchronized (listeners){
        	listeners.add(screenController);
        }
    }

    public void setupConnection(String ip){
        if (Server.IsLoggedIn()) {
            synchronized (listeners){
                listeners.add(new ServerResponder(ip, screen));
            }
        }else{
            wifi = new WifiHost(screen, this);
            if (ip != null)
                wifi.setIp(ip);
            wifi.start(isHost());

            ipAdder = new IpAdder(this, wifi);
            synchronized (listeners) {
                listeners.add(ipAdder);
            }
        }
    }

    public void setName(String name){
        this.name = name;
    }
    public String getName(){
        return name;
    }

    public Narrator getNarrator(){
        return narrator;
    }

    public boolean isHost(){
        return isHost;
    }

    public void setSeed(long l){
    	rand.setSeed(l);
    }
    
    //gets called on by network thread
    public void onNewPlayer(ChatManager c){
        if(!isHost){
            //this means that the chat manager is ready, and now i can start sending my name. once the narrator receives it, he'll send me the roles and players
            c.write(Constants.REQUEST_INFO);
        }
    }

    private void log(String s){
        Log.e("SetupManager", s);
    }

    public void addListener(SetupListener sL){
    	synchronized (listeners){
    		listeners.add(sL);
    	}
    }
    public void removeListener(SetupListener sL){
    	synchronized (listeners){
    		listeners.remove(sL);
    	}
    }

    public void addRole(RoleTemplate role){
        if(role.isRandom()){
            RandomRole rr = null;
            String name = role.getName();
            switch (name) {
                case Constants.TOWN_RANDOM_ROLE_NAME:
                    rr = RandomRole.TownRandom();
                    break;
                case Constants.TOWN_INVESTIGATIVE_ROLE_NAME:
                    rr = RandomRole.TownInvestigative();
                    break;
                case Constants.TOWN_PROTECTIVE_ROLE_NAME:
                    rr = RandomRole.TownProtective();
                    break;
                case Constants.TOWN_KILLING_ROLE_NAME:
                    rr = RandomRole.TownKilling();
                    break;
                case Constants.MAFIA_RANDOM_ROLE_NAME:
                    rr = RandomRole.MafiaRandom();
                    break;
                case Constants.YAKUZA_RANDOM_ROLE_NAME:
                    rr = RandomRole.YakuzaRandom();
                    break;
                case Constants.NEUTRAL_RANDOM_ROLE_NAME:
                    rr = RandomRole.NeutralRandom();
                    break;
                case Constants.ANY_RANDOM_ROLE_NAME:
                    rr = RandomRole.AnyRandom();
                    break;

            }
            narrator.addRole(rr);
        }else{
            narrator.addRole(role.getName(), role.getColor());
        }
        for(SetupListener sL: listeners){
            sL.onRoleAdd(role);
        }
    }

    public void removeRole(RoleTemplate role){
        narrator.removeRole(role);
        for(SetupListener sL: listeners){
            sL.onRoleRemove(role);
        }
    }

    //from the phone
    public void addPlayer(String name){
    	synchronized(narrator){
    		if(isHost()){
    			Player player = narrator.addPlayer();
    			player.setName(name);
    			player.setCommunicator(new CommunicatorPhone());

    			newPlayerJoined(player);
    		}else{
    			wifi.write(Constants.NEW_PLAYER_ADDITION+name);
    		}
    	}
    }

    //slave function from the ip
    public void addPlayer(String name, int id, Communicator c){
    	synchronized (narrator){
    		Player player = narrator.addPlayer(id);
        	player.setName(name);
        	
        	if(c == null){
        		c = new CommunicatorNull();
        	}
        		
        	player.setCommunicator(c);
        	
        	newPlayerJoined(player);
    	}
    }

    public void addPlayer(String name, PhoneNumber number) {
    	synchronized (narrator){
    		Player player = narrator.addPlayer(new CommunicatorText(number));
    		player.setName(name);
    		player.sendMessage("You are " + name + ".");
    		newPlayerJoined(player);
    	}
    }

    public void addPlayer(String name, ChatManager cm){
	    synchronized (narrator){
	        CommunicatorInternet ci = new CommunicatorInternet(cm);
	        Player player = narrator.addPlayer(ci);
	        player.setName(name);
	        newPlayerJoined(player);
    	}

    }

    public void removePlayer(Player p){
        narrator.removePlayer(p);
        playerLeft(p);
    }

    public void changeName(Player p, String name){
        p.setName(name);

        for(SetupListener sL: listeners){
            sL.onNameChange(p, name);
        }
    }

    public void newPlayerJoined(Player p){
    	Iterator<SetupListener> iter = listeners.iterator();
    	synchronized(listeners){
    		while(iter.hasNext()){
    			iter.next().onPlayerAdd(p);
    		}
    	}
    	
    }
    public void playerLeft(Player p){
        for (SetupListener sL: listeners)
            sL.onPlayerRemove(p);
    }

    public synchronized void onRead(String s, ChatManager c){
        if(s.length() == 0)
            return;
        try {
            if (isHost)
                ipAdder.hostRead(s, c);
            else
                ipAdder.clientRead(s, c);
        }catch(ArrayIndexOutOfBoundsException e){
            log("message was :" + s);
            e.printStackTrace();
            throw e;
        }
    }







    public void startGame(long seed){
        try {
            narrator.setSeed(seed);
            narrator.setRules(ActivitySettings.getRules(screen));
            narrator.shufflePlayers();
            narrator.startGame();

            ipAdder.startGame(seed);



            shutdown();

            Intent i = new Intent(screen, ActivityDay.class);
            i.putExtra(Narrator.KEY, Board.GetParcel(narrator));
            i.putExtra(ActivityHome.ISHOST, isHost);



            screen.startActivity(i);
            screen.finish();

        } catch (IllegalGameSettingsException |IllegalRoleCombinationException e) {
            toast(e.getMessage());
        }
    }

    public void toast(String s){
        screen.toast(s);

    }

    public void shutdown(){
        stopTexting();
        if (!Server.IsLoggedIn())
            wifi.shutdown(isHost);
    }

    public void stopTexting(){
        TextHandler.stopTexting(screen, textAdder);
    }
    public void resumeTexting(){
        screen.registerReceiver(textAdder, intentFilter);
    }

    private void debugSettings(){
        Player slave;
        for (int i = 1; i <= 5; i++){
            slave = narrator.addPlayer();
            slave.setCommunicator(new CommunicatorPhone()).setName(Computer.NAME + toLetter(i));
            slave.setComputer();
        }
        addRandomRole(6);
    }

    private void addRandomRole(int count){
        for (int i = 0; i < count; i++){
        	addRole(getRandomRole(rand));
            
            
        }
    }

	public static RoleTemplate getRandomRole(Random rand){
		switch (rand.nextInt(8)){
        case 0:
            return RandomRole.TownRandom();
        case 1:
        	return RandomRole.MafiaRandom();
        case 2:
        	return RandomRole.NeutralEvilRandom();
        case 3:
        	return RandomRole.NeutralRandom();
        case 4:
        	return RandomRole.YakuzaRandom();
        default:
        	return RandomRole.AnyRandom();
		}
	}

    public static String toLetter(int i){
        int letter = (i-1) / 26;
        char c = (char) ((i-1)%26 + 65);
        if (letter == 0)
            return c+"";
        else{
            return toLetter(i/26) + c;
        }
    }

    private void addRole(Member m, int count){
        for(int i = 0; i < count; i++){
            addRole(m);
        }
    }



    private void addRole(RandomRole m){
        addRole(m, 1);
    }

    private void addRole(RandomRole r, int j){
        for (int i = 0; i < j; i++){
            addRole(r);
        }
    }


}
