package android.setup;

import java.util.ArrayList;

import android.CommunicatorPhone;
import android.NarratorService;
import android.content.Intent;
import android.content.IntentFilter;
import android.day.ActivityDay;
import android.parse.Server;
import android.util.Log;
import json.JSONException;
import json.JSONObject;
import shared.ai.Computer;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.exceptions.IllegalGameSettingsException;
import shared.logic.exceptions.IllegalRoleCombinationException;
import shared.logic.support.Communicator;
import shared.logic.support.Constants;
import shared.logic.support.Random;
import shared.logic.support.RoleTemplate;
import shared.roles.RandomMember;


public class SetupManager {

    public ActivityCreateGame screen;

    public TextAdder textAdder;

    public SetupScreenController screenController;

    private ArrayList<SetupListener> listeners;

    private IntentFilter intentFilter;

    private Random rand;
    public NarratorService ns;
    private Server server;
    
    public SetupManager(ActivityCreateGame a, NarratorService ns) throws JSONException{
    	this.ns = ns;
    	server = a.server;
    	ns.setSetupManager(this);
        screen = a;
        listeners = new ArrayList<>();

        this.rand = new Random();

        intentFilter = new IntentFilter();
        if(server.IsLoggedIn()) {
            //intentFilter.addAction(ParseConstants.PARSE_FILTER);
        }else
            intentFilter.addAction("SMS_RECEIVED_ACTION");


        textAdder = new TextAdder(this);
        resumeTexting();


        screenController = new SetupScreenController(a, isHost());
        listeners.add(screenController);

        try {
            if (server.IsLoggedIn()) {

            } else {
                a.onConnect(this);
            }
        }catch(JSONException e){
        	e.printStackTrace();
        }
        a.onConnect(this);
        screen.refreshAvailableRolesList();
    }



    

    public Narrator getNarrator(){
        return ns.local;
    }

    public boolean isHost(){
    	return ns.isHost();
    }

    public void setSeed(long l){
    	rand.setSeed(l);
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
                    return RandomMember.TownRandom();
                case Constants.TOWN_INVESTIGATIVE_ROLE_NAME:
                    return RandomMember.TownInvestigative();
                case Constants.TOWN_PROTECTIVE_ROLE_NAME:
                    return RandomMember.TownProtective();
                case Constants.TOWN_KILLING_ROLE_NAME:
                    return RandomMember.TownKilling();
                case Constants.MAFIA_RANDOM_ROLE_NAME:
                    return RandomMember.MafiaRandom();
                case Constants.YAKUZA_RANDOM_ROLE_NAME:
                    return RandomMember.YakuzaRandom();
                case Constants.NEUTRAL_RANDOM_ROLE_NAME:
                    return RandomMember.NeutralRandom();
                case Constants.ANY_RANDOM_ROLE_NAME:
                    return RandomMember.AnyRandom();
            }
    	}
    	return role;
    }

    public synchronized void addRole(RandomMember rr){
        if(server.IsLoggedIn()){

        }else{
            for (SetupListener sL : listeners) {
                sL.onRoleAdd(rr);
            }
        }
    }
    
    public synchronized void addRole(RoleTemplate rt){
    	
        if(server.IsLoggedIn()){

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
    		if(server.IsLoggedIn() && Server.isHost()){
    			
    		}
    	}
    }

    public void requestRemovePlayer(String name){
		//ns.socketClient.send(Constants.REMOVE_PLAYER + name);
    }

    public synchronized void removePlayer(String name, boolean notifyOnlyScreen){
    	if(!server.IsLoggedIn()){
    		ns.local.removePlayer(name);
    	}
    	for(SetupListener sl: listeners){
	    	if(notifyOnlyScreen)
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
        RandomMember rr;
        for (int i = 0; i < count; i++){
            rr = getRandomRole(rand);
        	addRole(rr);
        }
    }

	public static RandomMember getRandomRole(Random rand){
		switch (rand.nextInt(8)){
        case 0:
            return RandomMember.TownRandom();
        case 1:
        	return RandomMember.MafiaRandom();
        case 2:
        	return RandomMember.NeutralEvilRandom();
        case 3:
        	return RandomMember.NeutralRandom();
        case 4:
        	return RandomMember.YakuzaRandom();
        default:
        	return RandomMember.AnyRandom();
		}
	}






    public void talk(String message){
        if(!server.IsLoggedIn())
            return;

        ns.talk(server.GetCurrentUserName(), message);
    }

    public void setRules(JSONObject ruleToBeChanged){
        //ns.local.setRules(r);
        //screenController.setRoleInfo(screenController.activeRole, Constants.A_NORMAL);
    }

    public void updateNarrator(Intent i) throws JSONException{

        //JSONObject oj = new JSONObject(i.getStringExtra("stuff"));


        /*switch(oj.getString("command")){
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
                screen.updateChat();
        }*/
    }
    public void ruleChange(String id, boolean val) {
    	ns.ruleChange(id, val);
    }
    public void ruleChange(String id, int val) {
    	ns.ruleChange(id, val);
    }
}
