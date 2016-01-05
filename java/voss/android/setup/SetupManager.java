package voss.android.setup;

import java.util.ArrayList;
import java.util.Random;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import voss.android.ActivitySettings;
import voss.android.CommunicatorPhone;
import voss.android.NarratorService;
import voss.android.SuccessListener;
import voss.android.day.ActivityDay;
import voss.android.parse.ParseConstants;
import voss.android.parse.Server;
import voss.android.parse.ServerResponder;
import voss.android.texting.TextHandler;
import voss.shared.ai.Computer;
import voss.shared.logic.Member;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Rules;
import voss.shared.logic.exceptions.IllegalGameSettingsException;
import voss.shared.logic.exceptions.IllegalRoleCombinationException;
import voss.shared.logic.support.CommandHandler;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.packaging.Packager;
import voss.shared.roles.RandomRole;


public class SetupManager {

    public ActivityCreateGame screen;

    public TextAdder textAdder;

    public SetupScreenController screenController;

    private ArrayList<SetupListener> listeners;

    private IntentFilter intentFilter;

    private Random rand;
    public NarratorService ns;
    
    public SetupManager(ActivityCreateGame a, NarratorService ns){
    	this.ns = ns;
    	ns.setSetupManager(this);
        screen = a;
        listeners = new ArrayList<>();

        this.rand = new Random();
        
        //if(Narrator.DEBUG && isHost() && narrator.getAllRoles().size() == 0 && false)
            //debugSettings();

        intentFilter = new IntentFilter();
        if(Server.IsLoggedIn())
            intentFilter.addAction(ParseConstants.PARSE_FILTER);
        else
            intentFilter.addAction("SMS_RECEIVED_ACTION");


        textAdder = new TextAdder(this);
        resumeTexting();


        screenController = new SetupScreenController(a, isHost());
        listeners.add(ns);
        listeners.add(screenController);
    }

    private ServerResponder sResponder;
    private HostAdder hAdder;
    //private ClientAdder cAdder;
    public void setupConnection(){
        if (Server.IsLoggedIn()){
            sResponder = new ServerResponder(ns.getGameListing(), this);
        }else{
        	if(isHost()){
        		hAdder = new HostAdder(ns);
        		listeners.add(hAdder);
        	}else{
        		new ClientAdder(this);
        		//listeners.add(cAdder);
        	}
        }
    }

    public Narrator getNarrator(){
        return ns.local;
    }

    public boolean isHost(){
        if(Server.IsLoggedIn()) {
            try {
                return Server.GetCurrentUserName().equals(ns.getGameListing().getHostName());
            }catch(NullPointerException e){
                if(ns == null)
                    throw new NullPointerException("ns was null");
                if(ns.getGameListing() == null)
                    throw new NullPointerException("game listing was null");
                throw new NullPointerException("neither were null");
            }
        }
        return ns.socketClient == null;
    }

    public void setSeed(long l){
    	rand.setSeed(l);
    }

    protected void onRuleChange(){
        if(Server.IsLoggedIn()){
            Server.UpdateRules(ns.getGameListing(), ns.local.getRules());
        }
    }

    private void log(String s){
        Log.e("SetupManager", s);
    }

    public static RoleTemplate TranslateRole(RoleTemplate role){
    	if(role.isRandom()){
            String name = role.getName();
            switch (name) {
                case Constants.TOWN_RANDOM_ROLE_NAME:
                    return RandomRole.TownRandom();
                case Constants.TOWN_INVESTIGATIVE_ROLE_NAME:
                    return RandomRole.TownInvestigative();
                case Constants.TOWN_PROTECTIVE_ROLE_NAME:
                    return RandomRole.TownProtective();
                case Constants.TOWN_KILLING_ROLE_NAME:
                    return RandomRole.TownKilling();
                case Constants.MAFIA_RANDOM_ROLE_NAME:
                    return RandomRole.MafiaRandom();
                case Constants.YAKUZA_RANDOM_ROLE_NAME:
                    return RandomRole.YakuzaRandom();
                case Constants.NEUTRAL_RANDOM_ROLE_NAME:
                    return RandomRole.NeutralRandom();
                case Constants.ANY_RANDOM_ROLE_NAME:
                    return RandomRole.AnyRandom();
            }
    	}
    	return role;
    }
    
    public synchronized void addRole(RoleTemplate role){
        role = TranslateRole(role);
        
        for(SetupListener sL: listeners){
            sL.onRoleAdd(role);
        }
    }

    public synchronized void removeRole(RoleTemplate role){
        for(SetupListener sL: listeners){
            sL.onRoleRemove(role);
        }
    }
    
    public synchronized void addListener(SetupListener playerPopUp){
    	listeners.add(playerPopUp);
    }
    public synchronized void removeListener(SetupListener sl){
    	listeners.remove(sl);
    }
    public synchronized void addPlayer(String name, Communicator c){
    	if(isHost() || Server.IsLoggedIn()){ //c should never be null, so it'll hit the narrator and then the controller
    		for(SetupListener sl: listeners){
    			if(c == null & (sl == ns || sl == hAdder))
    				continue;//will only update the screen with a toast, or the playerpopup
    			sl.onPlayerAdd(name, c);
    		}
    	}else{
    		if(c == null){//already added
    			for(SetupListener sl: listeners){
        			if(sl != ns)//will only update the screen with a toast, or the playerpopup
        				sl.onPlayerAdd(name, null);
        		}
    		}else{//requesting host to add
                if(!Server.IsLoggedIn())
    			    ns.socketClient.send(Constants.NEW_PLAYER_ADDITION + name);
    		}
    	}
    }

    public void requestRemovePlayer(String name){
		ns.socketClient.send(Constants.REMOVE_PLAYER + name);
    }

    public synchronized void removePlayer(String name, boolean notifyOnlyScreen){
    	for(SetupListener sl: listeners){
	    	if(notifyOnlyScreen && sl == hAdder)
	    		continue;//will only update the screen with a toast, or the playerpopup
	    	sl.onPlayerRemove(name);
    	}
    }





    public boolean checkNarrator(){
        try{
            ns.local.runSetupChecks();
            return true;
        }catch (IllegalGameSettingsException |IllegalRoleCombinationException e) {
            toast(e.getMessage());
            return false;
        }
    }

    public void startGame(long seed){
        if(checkNarrator()) {
            ns.startGame(seed);
            //startDay();
        }
    }
    
    public void startDay(){
    	Intent i = new Intent(screen, ActivityDay.class);

        screen.startActivity(i);
        screen.finish();

        shutdown();
    }

    public void exitGame(){
        sResponder.exitGame();
        screen.onBackPressed();
    }

    public void toast(String s){
        screen.toast(s);

    }

    public void shutdown(){
        stopTexting();
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
        	String compName = Computer.NAME + toLetter(i);
            addPlayer(compName, new CommunicatorPhone());
            ns.setComputer(compName);
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

    public static String toLetter(int num){
    	String result = "";
        while (num > 0) {
          num--; // 1 => a, not 0 => a
          int remainder = num % 26;
          char digit = (char) (remainder + 65);
          result = digit + result;
          num = (num - remainder) / 26;
        }

        return result;
    }

    @SuppressWarnings("unused")
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

    public void talk(String message){
        if(!Server.IsLoggedIn())
            return;

        Player p = ns.local.getPlayerByName(Server.GetCurrentUserName());
        p.say(message, Constants.REGULAR_CHAT);

        message = Server.GetCurrentUserName() + "," + Server.GetCurrentUserName() + Constants.NAME_SPLIT + CommandHandler.SAY + " " + null + " " + message;
        Server.PushCommand(ns.getGameListing(), message, 0);

        screen.updateChat();

    }

    public void setRules(String rules){
        Packager p = new Packager(new SetupDeliverer(rules));
        Rules r = new Rules(p);
        ns.local.setRules(r);
        screenController.setRoleInfo(screenController.activeRole, Constants.A_NORMAL, r);
    }

    public void updateNarrator(Intent i){
        String message = i.getStringExtra("stuff");
        String[] command = message.split(",");

        switch(command[0]){
            case ParseConstants.ADD_PLAYER:
                addPlayer(command[1], new CommunicatorNull());
                return;
            case ParseConstants.REMOVE_PLAYER:
                removePlayer(command[1], true);
                return;
            case ParseConstants.ADD_ROLE:
                if (!isHost())
                    addRole(RoleTemplate.FromIp(command[1]));
                return;
            case ParseConstants.REMOVE_ROLE:
                if (!isHost())
                    removeRole(RoleTemplate.FromIp(command[1]));
                return;
            case ParseConstants.RULES:
                if(!isHost())
                    setRules(command[1]);
                return;
            case ParseConstants.STARTGAME:
                Server.UpdateGame(ns, new SuccessListener() {
                    public void onSuccess() {
                        startGame(ns.local.getSeed());
                    }

                    public void onFailure(String message) {
                        toast("Game start failed.  Press back and go join again.");
                        Log.e("SetupManagerGamailure", message);
                    }
                });
                return;
            default:
                if(command[0].equals(Server.GetCurrentUserName()))
                    return;
                message = message.substring(command[0].length() + 1);//1 length for comma
                ns.onRead(message, null);//adds it to my narrator
                screen.updateChat();
        }
    }

    public void ruleChange() {
        if(Server.IsLoggedIn())
            Server.UpdateRules(ns.getGameListing(), ns.local.getRules());
        else if (isHost()) {
            SetupDeliverer sd = new SetupDeliverer();
            Packager p = new Packager(sd);
            ns.local.getRules().writeToPackage(p);
            if(hAdder != null)
                hAdder.onRulesChange(sd.toString());
        }
    }
}
