package android;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.day.ActivityDay;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.parse.Server;
import android.setup.ActivityCreateGame;
import android.setup.SetupManager;
import android.texting.StateObject;
import android.util.Log;
import android.wifi.NodeListener;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.Team;
import shared.logic.support.CommandHandler;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorNull;
import shared.logic.support.Constants;
import shared.logic.support.Faction;
import shared.logic.support.FactionManager;
import shared.logic.support.RoleTemplate;
import shared.roles.Arsonist;
import shared.roles.Mayor;

public class NarratorService extends Service{

	Server server;
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
		Log.d("NS", "Narrator started");
		if(nListeners == null)
			nListeners = new ArrayList<>();
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

			public void write(Player p, JSONObject jo){
				
			}
			
		};
	}
	
	public JSONObject getFactions() throws JSONException{
		if(server.IsLoggedIn()){
			return gameState.factions;
		}else{
			StateObject so = stateObject();
			so.addState(StateObject.RULES);
			return so.send((Player) null).getJSONObject(StateObject.factions);
		}
	}


    public boolean isHost(){
    	if(server.IsLoggedIn()){
    		if(gameState == null)
    			gameState = new GameState(this);
    		return gameState.isHost;
    	}
    	return true;
    }



	public SuccessListener sc;
	public void submitName(String name, SuccessListener sc) {
		this.sc = sc;
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
	public void put(JSONObject jo, String key, Object o){
		try {
			jo.put(key, o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean endedNight(String name){
		if(server.IsLoggedIn()){
			return gameState.endedNight;
		}else{
			return local.getPlayerByName(name).endedNight();
		}
	}

	public void endNight(String name){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.endNight);
			sendMessage(jo);
		}else{
			local.getPlayerByName(name).endNight();
		}
	}

	public void vote(String voter_s, String target_s){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.VOTE + " " + target_s);
			sendMessage(jo);
		}else{
			Player voter  = local.getPlayerByName(voter_s);
			Player target = local.getPlayerByName(target_s);
			voter.vote(target);
		}
	}

	public void skipVote(String voter){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.SKIP_VOTE);
			sendMessage(jo);
		}else{
			local.getPlayerByName(voter).voteSkip();
		}
	}

	public void unvote(String unvoter){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.UNVOTE);
			sendMessage(jo);
		}else{
			local.getPlayerByName(unvoter).unvote();
		}
	}

	public void talk(String name, String message){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			String team;
			if(gameState.isStarted){
				team = JUtils.getString(gameState.roleInfo, StateObject.roleColor);
			}else{
				team = "null";
			}
			put(jo, StateObject.message, Constants.SAY + " " + team + " " + message);
			sendMessage(jo);
		}else {
			Player p = local.getPlayerByName(name);
			if (p.isDead() || p.isBlackmailed() || !p.getTeam().knowsTeam())
				return;
			String key = Constants.REGULAR_CHAT;
			if (local.isNight())
				key = p.getTeam().getName();

			p.say(message, key);
		}
	}

	public boolean isDead(String name){
		if(server.IsLoggedIn()){
			JSONObject deadPlayer;
			for(int i = 0; i < gameState.graveYard.length(); i++){
				deadPlayer = JUtils.getJSONObject(gameState.graveYard, i);
				if(name.equals(JUtils.getString(deadPlayer, "name"))){
					return true;
				}
			}
			return false;
		}else{
			return local.getPlayerByName(name).isDead();
		}
	}

	public String getColor(String name){
		if(server.IsLoggedIn()){
			return JUtils.getString(gameState.roleInfo, StateObject.roleColor);
		}else{
			return local.getPlayerByName(name).getColor();
		}
	}


	public boolean isVoting(String owner_s, String target_s){
		if(server.IsLoggedIn()){
			JSONArray voteTargets = JUtils.getJSONArray(gameState.players, "Vote");
			if(voteTargets.length() == 0)
				return false;
			JSONObject playerObject = JUtils.getJSONObject(voteTargets, 0);
			String playerName = JUtils.getString(playerObject, StateObject.playerName);
			return target_s.equals(playerName);
		}else{
			Player owner  = local.getPlayerByName(owner_s);
			Player target = owner.getVoteTarget();
			if(target == null)
				return false;
			return owner.getVoteTarget().getName().equals(target_s);
		}
	}

	public void target(String owner_s, String target_s, String ability){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, ability + " " + target_s);
			sendMessage(jo);
		}else{
			Player owner = local.getPlayerByName(owner_s);
			Player target = local.getPlayerByName(target_s);
			owner.setTarget(target, owner.parseAbility(ability));
		}
	}


	public void doDayAction(String name){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			if(JUtils.getString(gameState.roleInfo, StateObject.roleName).equals(Mayor.ROLE_NAME)){
				put(jo, StateObject.message, Mayor.REVEAL);
			}else{
				put(jo, StateObject.message, Arsonist.BURN);
			}
			sendMessage(jo);
		}else{
			local.getPlayerByName(name).doDayAction();
		}
	}

	public void cancelEndNight(String name){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.endNight);
			sendMessage(jo);
		}else{
			local.getPlayerByName(name).cancelEndNight();
		}
	}

	public void addRole(String name, String color){
		//if server is hosting narrator instance, then you don't do anything here.  you just have to track the object that the server gives you
		//if you're hosting
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.roleColor, color);
			put(jo, StateObject.roleName, name);
			put(jo, StateObject.message, StateObject.addRole);
			sendMessage(jo);
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

	
	
	

	
	public synchronized void startGame(long seed){
		if(!local.isStarted()) {
			local.setSeed(seed);
			local.startGame();
		}


		sManager.startDay();

		
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

	public boolean isInProgress() {
		if(server.IsLoggedIn()){
			return !gameState.isOver;
		}else{
			return local.isInProgress();
		}
	}
	public boolean isDay() {
		if(server.IsLoggedIn())
			return false;
		else
			return local.isDay();
	}
	public JSONObject getRuleById(String ruleName) throws JSONException {
		if(server.IsLoggedIn()){
			return gameState.rules.getJSONObject(ruleName);
		}else{
			JSONObject jo = stateObject().addState(StateObject.RULES).send((Player) null);
			return jo.getJSONObject("rules").getJSONObject(ruleName);
		}
	}
	public void ruleChange(String id, boolean b) {
		if(server.IsLoggedIn()){
			try {
				gameState.rules.getJSONObject(id).put("val", b);
				JSONObject jo = new JSONObject();
				put(jo, StateObject.message, StateObject.ruleChange);
				put(jo, StateObject.ruleChange, gameState.rules);
				sendMessage(jo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if (isHost()) {
			local.getRules().setBool(id, b);
		}
    }
		
	public void ruleChange(String id, int b) {
		if(server.IsLoggedIn()){
			//server.UpdateRules(ns.getGameListing(), ns.local.getRules());
		}else if (isHost()) {
			local.getRules().setInt(id, b);
		}
    }
	
	public void removeRole(String roleName, String color) {
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.roleColor, color);
			put(jo, StateObject.roleName, roleName);
			put(jo, StateObject.message, StateObject.removeRole);
			sendMessage(jo);
		}else{
			RoleTemplate rt = local.getAllRoles().get(roleName, color);
			local.removeRole(rt);
		}
		if(sManager != null)
			sManager.onRoleRemove(roleName, color);	
	}
	
	public void newTeam(String name, String color, SuccessListener sL) {
		color = color.toUpperCase();
		if(server.IsLoggedIn()){
			sL.onSuccess();//too lazy to figure out a signaling message from the instance object;
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, "addTeam");
			put(jo, StateObject.color, color);
			put(jo, "teamName", name);
			sendMessage(jo);
		}else{
			Faction f = fManager.addFaction(name, color);
			f.setDescription("Custom team");
			if(sManager == null)
				return;
			try {
				sL.onSuccess();
				ActivityCreateGame ac = sManager.screen;
				ac.refreshAvailableFactions();
				ac.activeFaction = getFactions().getJSONObject(f.getName());
				ac.activeRule = sManager.screen.activeFaction;
				ac.refreshAvailableRolesList();
				ac.refreshDescription();
				return;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	public void deleteTeam(String color) {
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.deleteTeam);
			put(jo, StateObject.color, color);
			sendMessage(jo);
		}else{
			fManager.removeTeam(color);
			if(sManager == null)
				return;
			ActivityCreateGame ac = sManager.screen;
			try {
				ac.activeFaction = null;
				ac.activeRule = null;
				ac.refreshAvailableFactions();
				ac.refreshAvailableRolesList();
				ac.refreshDescription();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
	}
	public void setEnemies(String color, String teamColor, SuccessListener sl) {
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.removeTeamAlly);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.ally, color);
			sendMessage(jo);
		}else{
			Team curTeam = local.getTeam(teamColor);
			Team enemyTeam = local.getTeam(color);
			curTeam.setEnemies(enemyTeam);
			sl.onSuccess();
		}
	}
	public void setAllies(String color, String teamColor, SuccessListener sl) {
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.removeTeamEnemy);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.enemy, color);
			sendMessage(jo);
		}else{
			Team curTeam = local.getTeam(teamColor);
			Team allyTeam = local.getTeam(color);
			curTeam.setAllies(allyTeam);
			sl.onSuccess();
		}
	}
	
	public void addTeamRole(String className, String teamColor, SuccessListener sl){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.addTeamRole);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.simpleName, className);
			sendMessage(jo);
		}else{
			Faction f = fManager.getFaction(teamColor);
			f.makeAvailable(className, fManager);
			sl.onSuccess();
		}
	}
	public void removeTeamRole(String name, String teamColor, SuccessListener sl) {
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.removeTeamRole);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.roleName, name);
			sendMessage(jo);
		}else{
			Faction f = fManager.getFaction(teamColor);
			f.makeUnavailable(name, fManager);
			sl.onSuccess();
		}
	}
		
	public void sendMessage(JSONObject jo){
		put(jo, "name", server.GetCurrentUserName());
		mWebSocketClient.send(jo.toString());
	}
	
	public WebSocketClient mWebSocketClient;
	private ArrayList<NodeListener> nListeners;
	protected NActivity activity;
	public void connectWebSocket(final NActivity.NarratorConnectListener nc) {
		  URI uri;
		  try {
		    uri = new URI("ws://narrator.systeminplace.net:3000");
		  } catch (URISyntaxException e) {
		    e.printStackTrace();
		    return;
		  }

		  mWebSocketClient = new WebSocketClient(uri) {
		    public void onOpen(ServerHandshake unused) {
		    	JSONObject jo = new JSONObject();
		    	try {
					jo.put("server", true);
					jo.put("message", "greeting");
					sendMessage(jo);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if(nc != null)
					nc.onConnect();
		    }


		    public void onMessage(String s) {
				if(gameState == null)
					gameState = new GameState(NarratorService.this);
				while(s.substring(0,1).equals("\n")){
					s = s.substring(1);
				}
		    	ArrayList<NodeListener> toRemove = new ArrayList<>();
		    	for(NodeListener nL: nListeners){
		    		if(nL.onMessageReceive(s))
		    			toRemove.add(nL);
		    	}
		    	for(NodeListener nL: toRemove){
		    		nListeners.remove(nL);
		    	}
		    	try{
		    		JSONObject jo = new JSONObject(s);
		    		if(hasBoolean("lobbyUpdate", jo))
		    			return;
		    		
		    		if(hasBoolean("guiUpdate", jo)){
		    			gameState.parse(jo);
		    			activityCheck(jo);
		    		}else{
		    			String message = jo.getString("message");
		    			if(jo.has("chatReset"))
		    				gameState.chat = "";
		    			gameState.chat += (message + "\n");
		    			
		    			//only reason gamestate is doing this is because it runOnMain
		    			gameState.refreshChat(); 
		    			
		    		}
		    			
		    	}catch(JSONException e){
					Log.e("NaratorService,", e.getLocalizedMessage());
		    		e.printStackTrace();
					throw new NullPointerException(e.getMessage());
		    	}
		    }


		    public void onClose(int i, String s, boolean b) {
		      Log.i("Websocket", "Closed because: " + s);
		    }


		    public void onError(Exception e) {
				e.printStackTrace();
		      Log.i("Websocket", "Error " + e.getMessage());
		    }
		  };
		  mWebSocketClient.connect();
		}
	public GameState gameState;
	private void activityCheck(JSONObject jo) throws JSONException{
		if(!jo.has(StateObject.gameStart))
			return;
		boolean gameStarted = jo.getBoolean(StateObject.gameStart);
		if(gameStarted){
			if(activity.getClass() != ActivityDay.class){
				Intent i = new Intent(activity, ActivityDay.class);
				activity.startActivity(i);
			}
		}else{
			if(activity.getClass() != ActivityCreateGame.class){
				Intent i = new Intent(activity, ActivityCreateGame.class);
				activity.startActivity(i);
			}
		}
	}
	
	private boolean hasBoolean(String s, JSONObject jo){
		try{
			if(!jo.has(s))
				return false;
			boolean b = jo.getBoolean(s);
			return b;
		}catch(JSONException e){
			e.printStackTrace();
		}
		return false;
	}
	public void addNodeListener(NodeListener nL) {
		nListeners.add(nL);
	}
	public void removeNodeListener(NodeListener nL){
		nListeners.remove(nL);
	}
	public String getChat() {
		if(server.IsLoggedIn()){
			return gameState.chat;
		}else{
			return local.getEventManager().getEvents(shared.event.Message.PUBLIC).access(shared.event.Message.PUBLIC, true);
		}
	}

	public String getEvents(String currentPlayer){
		if(server.IsLoggedIn()){
			return gameState.chat;
		}else{
			String text;
			if (!local.isInProgress()){
				text = local.getEventManager().getEvents(shared.event.Message.PRIVATE).access(shared.event.Message.PRIVATE, true);
			}else if (currentPlayer == null)
				text = local.getEventManager().getEvents(shared.event.Message.PUBLIC).access(shared.event.Message.PUBLIC, true);
			else{
				Player p = local.getPlayerByName(currentPlayer);
				text = p.getEvents().access(p, true);
			}
			return text;
		}
	}

	public JSONObject getPlayers(String name) {
		if(server.IsLoggedIn()){
			return gameState.players;
		}else{
			StateObject so = stateObject();
			so.addState(StateObject.PLAYERLISTS);
			try{
				Player p = local.getPlayerByName(name);
				return so.send(p).getJSONObject(StateObject.playerLists);
			}catch(JSONException e){
				e.printStackTrace();
			}
			return null;
		}
	}

	public JSONObject getRoleInfo(String name) {
		if(server.IsLoggedIn()){
			return gameState.roleInfo;
		}else{
			StateObject so = stateObject();
			so.addState(StateObject.ROLEINFO);
			try{
				return so.send(local.getPlayerByName(name)).getJSONObject(StateObject.roleInfo);
			}catch(JSONException e){
				e.printStackTrace();
			}
			return null;
		}
	}

	public boolean showButton(String name){
		if(server.IsLoggedIn()){
			return gameState.showButton;
		}else{
			Player currentPlayer = local.getPlayerByName(name);
			if(local.isDay())
				return currentPlayer.hasDayAction();
			else
				return currentPlayer.isAlive();
		}
	}

	public boolean hasDayAction(String name){
		if(server.IsLoggedIn()) {
			if(gameState.isDay)
				return gameState.showButton;
			else
				return false;
		}else
			return local.getPlayerByName(name).hasDayAction();
	}
	
	public JSONArray getRoles(){
		if(server.IsLoggedIn()){
			return gameState.rolesList;
		}else{
			try{
				StateObject so = stateObject();
				so.addState(StateObject.ROLESLIST);
				return so.send((Player) null).getJSONArray(StateObject.roles);
			}catch(JSONException e){
				e.printStackTrace();
			}
			return null;
		}
	}
	public int getSkipVotes() {
		if(server.IsLoggedIn()){
			return gameState.skipVoteCount;
		}else{
			return local.Skipper.getVoteCount();
		}
	}
}
