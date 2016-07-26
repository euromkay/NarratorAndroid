package android.setup;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.CommunicatorPhone;
import android.NarratorService;
import android.SuccessListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.day.ActivityDay;
import android.parse.ParseConstants;
import android.parse.Server;
import android.parse.ServerResponder;
import android.util.Log;
import shared.ai.Computer;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.exceptions.IllegalGameSettingsException;
import shared.logic.exceptions.IllegalRoleCombinationException;
import shared.logic.support.CommandHandler;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorNull;
import shared.logic.support.Constants;
import shared.logic.support.Random;
import shared.logic.support.RoleTemplate;
import shared.roles.RandomRole;


public class SetupManager {

    public ActivityCreateGame screen;

    public TextAdder textAdder;

    public SetupScreenController screenController;

    private ArrayList<SetupListener> listeners;

    private IntentFilter intentFilter;

    private Random rand;
    public NarratorService ns;
    
    public SetupManager(ActivityCreateGame a, NarratorService ns) throws JSONException{
    	this.ns = ns;
    	ns.setSetupManager(this);
        screen = a;
        listeners = new ArrayList<>();

        this.rand = new Random();

        intentFilter = new IntentFilter();
        if(Server.IsLoggedIn())
            intentFilter.addAction(ParseConstants.PARSE_FILTER);
        else
            intentFilter.addAction("SMS_RECEIVED_ACTION");


        textAdder = new TextAdder(this);
        resumeTexting();


        screenController = new SetupScreenController(a, isHost());
        listeners.add(screenController);

        try {
            if (Server.IsLoggedIn()) {

            } else {
                a.onConnect(this);
            }
        }catch(JSONException e){
        	e.printStackTrace();
        }
        screen.refreshFactionList();
    }



    private ServerResponder sResponder;
    private HostAdder hAdder;
    //private ClientAdder cAdder;
    public void setupConnection(){
        sResponder = new ServerResponder(ns.getGameListing(), this);
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

    @SuppressWarnings("unused")
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

    public synchronized void addRole(RandomRole rr){
        if(Server.IsLoggedIn()){

        }else{
            for (SetupListener sL : listeners) {
                sL.onRoleAdd(rr);
            }
        }
    }
    
    public synchronized void addRole(RoleTemplate rt){
    	
        if(Server.IsLoggedIn()){

        }else {
            for (SetupListener sL : listeners) {
                sL.onRoleAdd(rt);
            }
        }

    }

    public synchronized void removeRole(String roleName, String color){
    	ns.removeRole(roleName, color);
    }
    
    public synchronized void onRoleRemove(String name, String color){
    	for(SetupListener sL: listeners){
            sL.onRoleRemove(name, color);
        }
    }
    
    public synchronized void addListener(SetupListener playerPopUp){
    	listeners.add(playerPopUp);
    }
    public synchronized void removeListener(SetupListener sl){
    	listeners.remove(sl);
    }
    public synchronized void addPlayer(String name, Communicator c){
    	if(isHost()){ //c should never be null, so it'll hit the narrator and then the controller
    		ns.onPlayerAdd(name, c);
    		for(SetupListener sl: listeners)
    			sl.onPlayerAdd(name, c);
    		if(Server.IsLoggedIn() && Server.isHost()){
    			
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
        try{
            screen.unregisterReceiver(textAdder);
        }catch (IllegalArgumentException e){}


    }
    public void resumeTexting(){
        screen.registerReceiver(textAdder, intentFilter);
    }

    @SuppressWarnings("unused")
	private void debugSettings(){
        Player slave;
        for (int i = 1; i <= 5; i++){
        	String compName = Computer.NAME + Computer.toLetter(i);
            addPlayer(compName, new CommunicatorPhone());
            ns.setComputer(compName);
        }
        addRandomRole(6);
    }

    private void addRandomRole(int count){
        RandomRole rr;
        for (int i = 0; i < count; i++){
            rr = getRandomRole(rand);
        	addRole(rr);
        }
    }

	public static RandomRole getRandomRole(Random rand){
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






    public void talk(String message){
        if(!Server.IsLoggedIn())
            return;

        Player p = ns.local.getPlayerByName(Server.GetCurrentUserName());
        p.say(message, Constants.REGULAR_CHAT);

        message = Server.GetCurrentUserName() + "," + Server.GetCurrentUserName() + Constants.NAME_SPLIT + CommandHandler.SAY + " " + null + " " + message;
        Server.PushCommand(ns.getGameListing(), message, 0);

        screen.updateChat();

    }

    public void setRules(JSONObject ruleToBeChanged){
        //ns.local.setRules(r);
        //screenController.setRoleInfo(screenController.activeRole, Constants.A_NORMAL);
    }

    public void updateNarrator(Intent i) throws JSONException{

        JSONObject oj = new JSONObject(i.getStringExtra("stuff"));


        switch(oj.getString("command")){
            case ParseConstants.ADD_PLAYER:
                addPlayer(oj.getString("name"), new CommunicatorNull());
                return;
            case ParseConstants.REMOVE_PLAYER:
                removePlayer(oj.getString("name"), true);
                return;
            case ParseConstants.ADD_ROLE:
                if (!isHost())
                    //addRole(oj.getString("roleName"), oj.getString("roleColor"));
                return;
            case ParseConstants.REMOVE_ROLE:
                if (!isHost())
                    removeRole(oj.getString("roleName"), oj.getString("roleColor"));
                return;
            case ParseConstants.RULES:
                if(!isHost())
                    setRules(oj.getJSONObject("rule"));
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
                /*if(command[0].equals(Server.GetCurrentUserName()))
                    return;
                message = message.substring(command[0].length() + 1);//1 length for comma
                ns.onRead(message, null);//adds it to my narrator
                screen.updateChat();*/
        }
    }
    public void ruleChange(String id, boolean val) {
    	ns.ruleChange(id, val);
    }
    public void ruleChange(String id, int val) {
    	ns.ruleChange(id, val);
    }
}
