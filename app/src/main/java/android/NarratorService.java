package android;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import com.google.firebase.iid.FirebaseInstanceId;

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
import android.widget.Toast;
import android.wifi.NodeListener;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import shared.event.Message;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorNull;
import shared.logic.support.Constants;
import shared.logic.support.Faction;
import shared.logic.support.FactionManager;
import shared.logic.support.RoleTemplate;
import shared.logic.support.rules.Rules;
import shared.roles.Arsonist;
import shared.roles.Assassin;
import shared.roles.Mayor;

public class NarratorService extends Service{

	public Server server;
	public Narrator local;//this is the one i keep communicators in
	public FactionManager fManager;
	public int onStartCommand(Intent i, int flags, int startId){
		if(local == null)
			refresh();
        return Service.START_STICKY;
	}
	public void refresh(){
		local = Narrator.Default();
		fManager = new FactionManager(local);
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
			if (p.isDead() || p.isBlackmailed())
				return;
			String key;
			if(local.isNight()){
				if(!p.getTeam().knowsTeam())
					return;
				key = p.getTeam().getName();
			}else
				key = Constants.REGULAR_CHAT;

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
			if(target_s.equalsIgnoreCase("Skip Day")) {
				return gameState.isSkipping;
			}
			JSONArray voteTargets = JUtils.getJSONArray(gameState.players, "Vote");
			if(voteTargets.length() == 0)
				return false;
			JSONObject playerObject;
			for(int i = 0; i < voteTargets.length(); i++){
				playerObject = JUtils.getJSONObject(voteTargets, 0);
				if(target_s.equals(JUtils.getString(playerObject, StateObject.playerName))){
					JSONArray jArray = JUtils.getJSONArray(playerObject, StateObject.playerSelectedColumn);
					return jArray.length() == 1;
				}
			}
			return false;
		}else{
			Player owner  = local.getPlayerByName(owner_s);
			Player target = owner.getVoteTarget();
			if(target == null)
				return false;
			return owner.getVoteTarget().getName().equals(target_s);
		}
	}

	public void target(String owner_s, ArrayList<String> target_s, String ability_s){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, ability_s + " " + target_s);
			sendMessage(jo);
		}else{
			Player owner = local.getPlayerByName(owner_s);
			PlayerList targets = PlayerList.FromNames(target_s, local);
			int ability = owner.parseAbility(ability_s);
			if(owner.getActions().isTargeting(targets, ability))
				owner.cancelTarget(targets, ability);
			else
				owner.setTarget(ability, null, targets.getArray());
		}
	}


	public void doDayAction(String name){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			String roleName = JUtils.getString(gameState.roleInfo, StateObject.roleBaseName);
			if(roleName.equals(Mayor.ROLE_NAME)){
				put(jo, StateObject.message, Mayor.REVEAL);
			}else if(roleName.equals(Arsonist.ROLE_NAME)){
				put(jo, StateObject.message, Arsonist.BURN);
			}else{
				ActivityDay ad = (ActivityDay) activity;
				int checkedPosition = ad.actionLV.getCheckedItemPosition();
				if(checkedPosition == -1){
					ad.toast("You must select someone to assasinate them");
					return;
				}
				String target = ad.actionList.get(checkedPosition);
				put(jo, StateObject.message, Assassin.ASSASSINATE + " " + target);
				
			}
			sendMessage(jo);
		}else{
			local.getPlayerByName(name).doDayAction();
		}
	}

	public void endNight(String name){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.END_NIGHT);
			sendMessage(jo);
		}else{
			local.getPlayerByName(name).endNight();
		}
	}
	public void cancelEndNight(String name){
		if(server.IsLoggedIn()){
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.END_NIGHT);
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
			return gameState.isDay;
		else
			return local.isDay();
	}
	public boolean isNight(){
		if(server.IsLoggedIn())
			return !gameState.isDay;
		else
			return local.isNight();
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
			if(sL != null)
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
				if(sL != null)
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
			if(activityCreateGameActive())
				((ActivityCreateGame) activity).resetView();
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
			if(activityCreateGameActive())
				((ActivityCreateGame) activity).resetView();
		}
	}

	public boolean activityCreateGameActive(){
		if(activity == null)
			return false;
		return activity.getClass() == ActivityCreateGame.class;
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
			if(activityCreateGameActive())
				((ActivityCreateGame) activity).resetView();
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
			if(activityCreateGameActive())
				((ActivityCreateGame) activity).resetView();
		}
	}


	public boolean isStarted(){
		if(server.IsLoggedIn())
			return gameState.isStarted;
		return local.isStarted();
	}
		
	public void sendMessage(JSONObject jo){
		put(jo, "name", server.GetCurrentUserName());
		try {
			mWebSocketClient.send(jo.toString());
		}catch(WebsocketNotConnectedException e){
			activity.toast("Connecting to server! Try again in a moment.");
			try {
				Thread.sleep(3000);
			}catch(InterruptedException f){}
			connectWebSocket(null);
		}
	}
	
	public static int WAIT_TIME = 10000;
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
					jo.put("sessionID", FirebaseInstanceId.getInstance().getToken());
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
		    			activityCheck();
		    		}else{
		    			String message = jo.getString("message");
		    			if(jo.has("chatReset"))
		    				gameState.resetChat();
		    			gameState.addToChat(message);
		    			
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
				if (mWebSocketClient.getConnection().isClosing() || mWebSocketClient.getConnection().isClosed()) {
					new Thread(new Runnable() {
						public void run() {
							if (activity != null) {
								Toast.makeText(activity, "Reconnecting", Toast.LENGTH_LONG).show();
							}
							if (WAIT_TIME != 0)
								try {
									Thread.sleep(WAIT_TIME);
								} catch (InterruptedException e) {
								}
							connectWebSocket(null);
						}
					}).start();
				}
				if (WAIT_TIME != 0)
					e.printStackTrace();
				Log.i("Websocket", "Error " + e.getMessage());
			}
		};
		mWebSocketClient.connect();
	}
	public GameState gameState;
	public boolean pendingDay = false;
	public boolean pendingCreate = false;
	private synchronized void activityCheck() throws JSONException{
		if(gameState == null)
			return;
		if(gameState.isStarted){
			if(activity.getClass() != ActivityDay.class && !pendingDay){
				Intent i = new Intent(activity, ActivityDay.class);
				activity.startActivity(i);
				pendingDay = true;
			}
		}else{
			if(activity.getClass() != ActivityCreateGame.class && !pendingCreate){
				Intent i = new Intent(activity, ActivityCreateGame.class);
				activity.startActivity(i);
				pendingCreate = true;
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
			return gameState.getChat();
		}else{
			if(local.isInProgress())
				return local.getEventManager().getEvents(shared.event.Message.PUBLIC).access(shared.event.Message.PUBLIC, true);
			else
				return local.getWinMessage().access(Message.PRIVATE, true) + "\n" +  
						local.getEventManager().getEvents(shared.event.Message.PRIVATE).access(shared.event.Message.PRIVATE, true);
		}
	}

	public String getEvents(String currentPlayer){
		if(server.IsLoggedIn()){
			return gameState.getChat();
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
	public int getMayorVotePower() {
		if(server.IsLoggedIn()){
			return gameState.mayorVoteCount;
		}else{
			return local.getRules().getInt(Rules.MAYOR_VOTE_POWER);
		}
	}
	public int getMinLynchVote() {
		if(server.IsLoggedIn()){
			int livePlayers = gameState.rolesList.length() - gameState.graveYard.length();
			livePlayers /= 2;
			return 1 + livePlayers; //51%
		}else
			return local.getMinLynchVote();
	}
	public JSONArray getActiveTeams() {
		if(server.IsLoggedIn()){
			return gameState.activeTeams;
		}else{
			StateObject so = stateObject();
			so.addState(StateObject.ACTIVETEAMS);
			JSONObject jo = so.send((Player) null);
			return JUtils.getJSONArray(jo, StateObject.activeTeams);
		}
	}
	public String getVoteCount(String name) {
		if(server.IsLoggedIn()){
			JSONArray playerList = JUtils.getJSONArray(gameState.players, "info");
			
			String nameToCheck;
			JSONObject playerObject;
			for(int i = 0 ; i < playerList.length(); i++){
				playerObject = JUtils.getJSONObject(playerList, i);
				nameToCheck = JUtils.getString(playerObject, StateObject.playerName);
				if(nameToCheck.equals(name)){
					return Integer.toString(JUtils.getInt(playerObject, StateObject.playerVote));
				}
			}
			throw new PlayerTargetingException();
		}else{
			return Integer.toString(local.getPlayerByName(name).getVoteCount());
		}
	}
	
}
