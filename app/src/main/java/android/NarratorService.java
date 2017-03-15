package android;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.iid.FirebaseInstanceId;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.day.ActivityDay;
import android.day.ChatItem;
import android.os.Binder;
import android.os.IBinder;
import android.parse.Server;
import android.screens.ActivityHome;
import android.setup.ActivityCreateGame;
import android.setup.SetupManager;
import android.support.annotation.NonNull;
import android.texting.StateObject;
import android.util.Log;
import android.view.View;
import android.wifi.NodeListener;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import shared.event.ChatMessage;
import shared.event.EventList;
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
import shared.logic.templates.Setup;
import shared.roles.Arsonist;
import shared.roles.Assassin;
import shared.roles.Mayor;
import shared.roles.Role;
import voss.narrator.R;

public class NarratorService extends Service {

	public Server server;
	public Narrator local;//this is the one i keep communicators in
	public FactionManager fManager;

	public int onStartCommand(Intent i, int flags, int startId) {
		if (nacs == null)
			refresh();
		return Service.START_STICKY;
	}

	public void refresh() {
		if(nacs == null)
			nacs = new ArrayList<>();
		Narrator n = new Narrator();
		Setup.Default(n);
		fManager = new FactionManager(local);
		fManager.importSetup(Setup.Default(n));
		Log.d("NS", "Narrator started");
		if (nListeners == null)
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

	public Narrator getNarrator() {
		return local;
	}

	private StateObject stateObject() {
		return new StateObject(local, fManager) {

			public boolean isActive(Player p) {
				return true;
			}

			public JSONObject getObject() throws JSONException {
				JSONObject jo = new JSONObject();
				JSONArray arr = new JSONArray();
				jo.put(StateObject.type, arr);
				return jo;
			}

			public void write(Player p, JSONObject jo) {

			}

		};
	}

	public JSONObject getFactions() throws JSONException {
		if (server.IsLoggedIn()) {
			return gameState.factions;
		} else {
			StateObject so = stateObject();
			so.addState(StateObject.RULES);
			return so.send((Player) null).getJSONObject(StateObject.factions);
		}
	}


	public boolean isHost() {
		if (server.IsLoggedIn()) {
			if (gameState == null)
				gameState = new GameState(this);
			return gameState.isHost;
		}
		return true;
	}


	public SuccessListener sc;

	public void submitName(String name, SuccessListener sc) {
		this.sc = sc;
	}


	public void disconnect(ServiceConnection sC, Activity a) {
		try {
			a.unbindService(sC);
		} catch (IllegalArgumentException e) {

		}
	}


	private SetupManager sManager;

	public void setSetupManager(SetupManager sm) {
		sManager = sm;
	}

	public void removeSetupManager() {
		sManager = null;
	}

	public void put(JSONObject jo, String key, Object o) {
		try {
			jo.put(key, o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean endedNight(String name) {
		if (server.IsLoggedIn()) {
			return gameState.endedNight;
		} else {
			return local.getPlayerByName(name).endedNight();
		}
	}

	public void vote(String voter_s, String target_s) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.VOTE + " " + target_s);
			sendMessage(jo);
		} else {
			Player voter = local.getPlayerByName(voter_s);
			Player target = local.getPlayerByName(target_s);
			voter.vote(target);
		}
	}

	public void skipVote(String voter) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.SKIP_VOTE);
			sendMessage(jo);
		} else {
			local.getPlayerByName(voter).voteSkip();
		}
	}

	public void unvote(String unvoter) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.UNVOTE);
			sendMessage(jo);
		} else {
			local.getPlayerByName(unvoter).unvote();
		}
	}

	public void talk(String name, String message) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			String team;
			if (gameState.isStarted) {
				team = JUtils.getString(gameState.roleInfo, StateObject.roleColor);
			} else {
				team = Constants.DAY_CHAT;
			}
			put(jo, StateObject.message, Constants.SAY + " " + team + " " + message);
			sendMessage(jo);
		} else {
			Player p = local.getPlayerByName(name);
			if (p.isDead() || p.isBlackmailed())
				return;
			String key;
			if (local.isNight()) {
				if (!p.getTeam().knowsTeam())
					return;
				key = p.getTeam().getName();
			} else
				key = Constants.DAY_CHAT;

			p.say(message, key);
		}
	}

	public boolean isDead(String name) {
		if (server.IsLoggedIn()) {
			JSONObject deadPlayer;
			for (int i = 0; i < gameState.graveYard.length(); i++) {
				deadPlayer = JUtils.getJSONObject(gameState.graveYard, i);
				if (name.equals(JUtils.getString(deadPlayer, "name"))) {
					return true;
				}
			}
			return false;
		} else {
			return local.getPlayerByName(name).isDead();
		}
	}

	public String getColor(String name) {
		if (server.IsLoggedIn()) {
			return JUtils.getString(gameState.roleInfo, StateObject.roleColor);
		} else {
			return local.getPlayerByName(name).getColor();
		}
	}


	public boolean isVoting(String owner_s, String target_s) {
		if (server.IsLoggedIn()) {
			if (target_s.equalsIgnoreCase("Skip Day")) {
				return gameState.isSkipping;
			}
			JSONArray voteTargets = JUtils.getJSONArray(gameState.players, "Vote");
			if (voteTargets.length() == 0)
				return false;
			JSONObject playerObject;
			for (int i = 0; i < voteTargets.length(); i++) {
				playerObject = JUtils.getJSONObject(voteTargets, 0);
				if (target_s.equals(JUtils.getString(playerObject, StateObject.playerName))) {
					JSONArray jArray = JUtils.getJSONArray(playerObject, StateObject.playerSelectedColumn);
					return jArray.length() == 1;
				}
			}
			return false;
		} else {
			Player owner = local.getPlayerByName(owner_s);
			Player target = owner.getVoteTarget();
			if (target == null)
				return false;
			return owner.getVoteTarget().getName().equals(target_s);
		}
	}

	public void target(String owner_s, ArrayList<String> target_s, String ability_s, String option, boolean submitAction) {
		ability_s = ability_s.toLowerCase();
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			if (!submitAction) {
				put(jo, StateObject.message, StateObject.cancelAction);
			} else
				put(jo, StateObject.message, StateObject.submitAction);

			if (ability_s.equals("give gun"))
				ability_s = "gun";
			else if (ability_s.equals("give armor")) {
				ability_s = "armor";
			}
			put(jo, StateObject.command, ability_s);
			put(jo, StateObject.targets, target_s);

			sendMessage(jo);
		} else {
			Player owner = local.getPlayerByName(owner_s);
			PlayerList targets = PlayerList.FromNames(target_s, local);
			int ability = owner.parseAbility(ability_s);
			if (owner.getActions().isTargeting(targets, ability))
				owner.cancelTarget(targets, ability);
			else
				owner.setTarget(ability, option, targets.getArray());
		}
	}


	public void doDayAction(String name, String target) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			String roleName = JUtils.getString(gameState.roleInfo, StateObject.roleBaseName);
			if (roleName.equals(Mayor.class.getSimpleName())) {
				put(jo, StateObject.message, Mayor.REVEAL);
			} else if (roleName.equals(Arsonist.class.getSimpleName())) {
				put(jo, StateObject.message, Arsonist.BURN);
			} else {
				if (target == null) {
					toast("You must select someone to assasinate them");
					return;
				}
				put(jo, StateObject.message, Assassin.ASSASSINATE + " " + target);
			}
			sendMessage(jo);
		} else {
			local.getPlayerByName(name).doDayAction();
		}
	}

	public void endNight(String name) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.END_NIGHT);
			sendMessage(jo);
		} else {
			local.getPlayerByName(name).endNight();
		}
	}

	public void cancelEndNight(String name) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, Constants.END_NIGHT);
			sendMessage(jo);
		} else {
			local.getPlayerByName(name).cancelEndNight();
		}
	}

	public void addRole(String name, String color) {
		//if server is hosting narrator instance, then you don't do anything here.  you just have to track the object that the server gives you
		//if you're hosting
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.roleColor, color);
			put(jo, StateObject.roleName, name);
			put(jo, StateObject.message, StateObject.addRole);
			sendMessage(jo);
		} else {
			RoleTemplate rt = null;
			Faction f = fManager.getFaction(color);
			if (f != null)
				rt = f.getRole(name);
			if (rt == null) {
				f = fManager.getFaction(Constants.A_NEUTRAL);
				rt = f.getRole(name);
				if (rt == null) {
					f = fManager.getFaction(Constants.A_RANDOM);
					rt = f.getRole(name);
				}
			} else {
				rt = f.getRole(name);
			}

			local.addRole(rt);
			//onRoleAdd(SetupManager.TranslateRole(rt));
			if (sManager != null)
				sManager.addRole(rt);
		}
	}

	public void removeRole(RoleTemplate rt) {
		if (sManager == null) {
			onRoleRemove(rt);
		} else {
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

		if (sManager != null) {//just looking for a trigger here
			sManager.addPlayer(name, (Communicator) null);
		}

		return p;
	}

	public void setComputer(String s) {
		local.getPlayerByName(s).setComputer();
	}

	public void onPlayerAdd(String s, Communicator c) {
		if (c == null)
			c = new CommunicatorNull();
		local.addPlayer(s, c);
	}

	public void removePlayer(String name) {
		if (sManager != null) {
			sManager.removePlayer(name, true);//tells manager to not tell the hostAdder
		} else {
			onPlayerRemove(name);
		}
	}

	public void onPlayerRemove(String s) {
		local.removePlayer(s);
	}


	public synchronized void startGame(long seed) {
		if (!local.isStarted()) {
			local.setSeed(seed);
			local.startGame();
		}


		sManager.startDay();


		removeSetupManager();
	}

	public boolean isInProgress() {
		if (server.IsLoggedIn()) {
			return !gameState.isOver;
		} else {
			return local.isInProgress();
		}
	}

	public boolean isDay() {
		if (server.IsLoggedIn())
			return gameState.isDay;
		else
			return local.isDay();
	}

	public boolean isNight() {
		if (server.IsLoggedIn())
			return !gameState.isDay;
		else
			return local.isNight();
	}

	public JSONObject getRuleById(String ruleName) throws JSONException {
		if (server.IsLoggedIn()) {
			return gameState.rules.getJSONObject(ruleName);
		} else {
			JSONObject jo = stateObject().addState(StateObject.RULES).send((Player) null);
			return jo.getJSONObject("rules").getJSONObject(ruleName);
		}
	}

	public void ruleChange(String id, boolean b) {
		if (server.IsLoggedIn()) {
			try {
				gameState.rules.getJSONObject(id).put("val", b);
				JSONObject jo = new JSONObject();
				put(jo, StateObject.message, StateObject.ruleChange);
				put(jo, StateObject.ruleChange, gameState.rules);
				sendMessage(jo);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else if (isHost()) {
			local.getRules().setBool(id, b);
		}
	}

	public void ruleChange(String id, int b) {
		if (server.IsLoggedIn()) {
			//server.UpdateRules(ns.getGameListing(), ns.local.getRules());
		} else if (isHost()) {
			local.getRules().setInt(id, b);
		}
	}

	public void removeRole(String roleName, String color) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.roleColor, color);
			put(jo, StateObject.roleName, roleName);
			put(jo, StateObject.message, StateObject.removeRole);
			sendMessage(jo);
		} else {
			RoleTemplate rt = local.getRolesList().get(roleName, color);
			local.removeRole(rt);
		}
		if (sManager != null)
			sManager.onRoleRemove(roleName, color);
	}

	public void newTeam(String name, String color, SuccessListener sL) {
		color = color.toUpperCase();
		if (server.IsLoggedIn()) {
			if (sL != null)
				sL.onSuccess();//too lazy to figure out a signaling message from the instance object;
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, "addTeam");
			put(jo, StateObject.color, color);
			put(jo, "teamName", name);
			sendMessage(jo);
		} else {
			Faction f = fManager.addFaction(name, color);
			f.setDescription("Custom team");
			if (sManager == null)
				return;
			try {
				if (sL != null)
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
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.deleteTeam);
			put(jo, StateObject.color, color);
			sendMessage(jo);
		} else {
			fManager.removeTeam(color);
			if (sManager == null)
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
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.removeTeamAlly);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.ally, color);
			sendMessage(jo);
		} else {
			Team curTeam = local.getTeam(teamColor);
			Team enemyTeam = local.getTeam(color);
			curTeam.setEnemies(enemyTeam);
			for(NActivity activity: nacs)
				activity.resetView();
		}
	}

	public void setAllies(String color, String teamColor, SuccessListener sl) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.removeTeamEnemy);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.enemy, color);
			sendMessage(jo);
		} else {
			Team curTeam = local.getTeam(teamColor);
			Team allyTeam = local.getTeam(color);
			curTeam.setAllies(allyTeam);
			for(NActivity activity: nacs)
				activity.resetView();
		}
	}

	public boolean activityCreateGameActive() {
		for(NActivity nac: nacs){
			if(nac instanceof ActivityCreateGame)
				return true;
		}
		return false;
	}

	public boolean activityDayActive() {
		for(NActivity nac: nacs){
			if(nac instanceof ActivityDay)
				return true;
		}
		return false;
	}

	public void addTeamRole(String className, String teamColor, SuccessListener sl) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.addTeamRole);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.simpleName, className);
			sendMessage(jo);
		} else {
			Faction f = fManager.getFaction(teamColor);
			f.makeAvailable(className, fManager);
			for(NActivity activity: nacs)
				activity.resetView();
		}
	}

	public void removeTeamRole(String name, String teamColor, SuccessListener sl) {
		if (server.IsLoggedIn()) {
			JSONObject jo = new JSONObject();
			put(jo, StateObject.message, StateObject.removeTeamRole);
			put(jo, StateObject.color, teamColor);
			put(jo, StateObject.roleName, name);
			sendMessage(jo);
		} else {
			Faction f = fManager.getFaction(teamColor);
			f.makeUnavailable(name, fManager);
			if (activityCreateGameActive())
				for(NActivity activity: nacs)
					activity.resetView();
		}
	}


	public boolean isStarted() {
		if (server.IsLoggedIn())
			return gameState.isStarted;
		return local.isStarted();
	}

	public void sendMessage(JSONObject jo) {
		put(jo, "name", server.GetCurrentUserName());
		try {
			if(mWebSocketClient != null) {  //this sometimes happens when clicking logout and i'm trying to send a message to the server
				Log.d("myAuth", "sending");
				Log.d("myAuth", "\t" + jo.toString());
				mWebSocketClient.send(jo.toString());
			}
		} catch (WebsocketNotConnectedException e) {
			toast("Connecting to server! Try again in a moment.");
			try {
				Thread.sleep(3000);
			} catch (InterruptedException f) {
			}
			connectWebSocket();
		}
	}

	private void sendGreeting(){//final NActivity.NarratorConnectListener nc){
		Log.d("myAuth", "trying to get firebase token");

		OnCompleteListener<GetTokenResult> x = new OnCompleteListener<GetTokenResult>() {
			public void onComplete(@NonNull Task<GetTokenResult> task) {
				if (task.isSuccessful()) {
					Log.d("myAuth", "was successful, sending the greeting now.");
					JSONObject jo = new JSONObject();

					try {
						jo.put("server", true);
						jo.put("message", "greeting");
						jo.put("tokenID", FirebaseInstanceId.getInstance().getToken());
						jo.put("sessionID", task.getResult().getToken());
						Log.d("gcr", task.getResult().getToken());

						Log.d("androidToken", FirebaseInstanceId.getInstance().getToken());
						sendMessage(jo);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					//if (nc != null)
					//	nc.onConnect();
				}
			}
		};
		server.getAuthToken(x);
	}

	ArrayList<NActivity> nacs;

	public synchronized void addActivity(NActivity na){
		if(!nacs.contains(na))
			nacs.add(na);
	}

	public synchronized void removeActivity(NActivity na){
		nacs.remove(na);
	}



	public static int WAIT_TIME = 10000;
	public WebSocketClient mWebSocketClient;
	private ArrayList<NodeListener> nListeners;

	public void connectWebSocket(){//final NActivity.NarratorConnectListener nc) {
		Log.d("myAuth", "trying to connect websocket");
		URI uri;
		try {
			uri = new URI("ws://narrator.systeminplace.net:3000");
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		}

		if (mWebSocketClient == null) {
			Log.d("myAuth", "trying to get open connection");
			mWebSocketClient = new WebSocketClient(uri) {
				public void onOpen(ServerHandshake unused) {

					sendGreeting();

				}

				;

				public void onMessage(String s) {

					while (s.substring(0, 1).equals("\n")) {
						s = s.substring(1);
					}

					try {
						JSONObject jo = new JSONObject(s);

						Log.d("myAuth", "receiving");
						Log.d("myAuth", "\t" + jo.toString());
						for (int i = 0; i < nListeners.size(); i++) {
							if (nListeners.get(i).onMessageReceive(jo)) {
								synchronized (this) {
									nListeners.remove(i);
								}
								i--;
							}
						}

						if (gameState == null)
							gameState = new GameState(NarratorService.this);
						if (hasBoolean("lobbyUpdate", jo)){
							Log.d("myAuth", "trying to show the buttons");
							showActivityHomeButtons();
							return;
						}

						if (hasBoolean(StateObject.guiUpdate, jo)) {
							gameState.parse(jo);

							if (gameState.seenMessage) {
								Log.d("myAuth", "checking activity");
								activityCheck();
							}
						} else if (gameState.seenMessage) {
							ChatItem ci;

							if (jo.has(StateObject.chatReset))
								gameState.resetChat();

							JSONArray jArray = jo.getJSONArray(StateObject.message);
							JSONObject jChat;
							for (int i = 0; i < jArray.length(); i++) {
								try {
									ci = new ChatItem(jArray.getString(i));
								} catch (JSONException e) {
									jChat = jArray.getJSONObject(i);
									String text = jChat.getString("text");
									String sender = jChat.getString("sender");
									ci = new ChatItem(sender, text);
								}

								gameState.addToChat(ci);
							}


							//only reason gamestate is doing this is because it runOnMain
							gameState.refreshChat();

						}

					} catch (JSONException e) {
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
								toast("Reconnecting");
								if (WAIT_TIME != 0)
									try {
										Thread.sleep(WAIT_TIME);
									} catch (InterruptedException e) {
									}
								if(NarratorService.this.mWebSocketClient != null)
									NarratorService.this.mWebSocketClient.close();
								NarratorService.this.mWebSocketClient = null;
								connectWebSocket();
							}
						}).start();
					}
					if (WAIT_TIME != 0)
						e.printStackTrace();
					Log.i("Websocket", "Error " + e.getMessage());
				}
			};
			mWebSocketClient.connect();
		} else if (mWebSocketClient.getConnection().isClosing() || mWebSocketClient.getConnection().isClosed()) {
			closeWebSocket();
			connectWebSocket();
		}else{
			sendGreeting();
		}
	}

	public void closeWebSocket(){
		mWebSocketClient.close();
		mWebSocketClient=null;
	}

	public GameState gameState;
	public boolean pendingDay = false;
	public boolean pendingCreate = false;
	private synchronized void activityCheck() throws JSONException{
		boolean hasDay = false;
		boolean hasSetup = false;
		for(NActivity nac: nacs){
			if(nac instanceof ActivityDay)
				hasDay = true;
			if(nac instanceof ActivityCreateGame)
				hasSetup = true;
		}

		if(gameState.isStarted){
			if(!pendingDay && !hasDay){
				Intent i = new Intent(nacs.get(0), ActivityDay.class);
				nacs.get(0).startActivity(i);
				pendingDay = true;
			}
		}else{
			if(!pendingCreate && !hasSetup) {
				Intent i = new Intent(nacs.get(0), ActivityCreateGame.class);
				nacs.get(0).startActivity(i);
				pendingCreate = true;
			}
		}
	}
	private synchronized void showActivityHomeButtons(){
		gameState = new GameState(this);
		for(NActivity nac: nacs) {
			if (nac instanceof ActivityHome) {
				nac.findViewById(R.id.home_host).setVisibility(View.VISIBLE);
				nac.findViewById(R.id.home_join).setVisibility(View.VISIBLE);
			} else {
				nac.finish();
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
	public synchronized void addNodeListener(NodeListener nL) {
		if(!nListeners.contains(nL))
			nListeners.add(nL);
	}
	public void removeNodeListener(NodeListener nL){
		nListeners.remove(nL);
	}
	public ArrayList<ChatItem> getChat() {
		if(server.IsLoggedIn()){
			return gameState.getChat();
		}else{
			String accessKey;
			if(local.isInProgress())
				accessKey = shared.event.Message.PUBLIC;
			else
				accessKey = shared.event.Message.PRIVATE;
			EventList el = local.getEventManager().getEvents(accessKey);
			ArrayList<ChatItem> ret = new ArrayList<>();

			ChatItem ci;
			ChatMessage cMessage;
			for(Message cm : el){
				if(cm instanceof ChatMessage){
					cMessage = (ChatMessage) cm;
					ci = new ChatItem(Message.accessHelper(cMessage.sender, accessKey, cm.getDay(), true));
				}else
					ci = new ChatItem(cm.access(accessKey, true));
				ret.add(ci);
			}

			if(!local.isInProgress())
				ret.add(new ChatItem(local.getWinMessage().access(Message.PRIVATE, true)));

			return ret;
		}
	}

	public ArrayList<ChatItem> getEvents(String currentPlayer){
		if(server.IsLoggedIn()){
			return gameState.getChat();
		}else{

			EventList eList;
			String accessKey;
			if(!local.isInProgress()) {
				eList = local.getEventManager().getEvents(shared.event.Message.PRIVATE);
				accessKey = shared.event.Message.PRIVATE;
			}else if(currentPlayer == null) {
				eList = local.getEventManager().getEvents(shared.event.Message.PUBLIC);
				accessKey = shared.event.Message.PUBLIC;
			}else {
				Player p = local.getPlayerByName(currentPlayer);
				eList = p.getEvents();
				accessKey = currentPlayer;
			}
			
			ArrayList<ChatItem> ret = new ArrayList<ChatItem>();
			ChatItem ci;
			ChatMessage cMessage;
			for(Message cm : eList){
				if(cm instanceof ChatMessage){
					cMessage = (ChatMessage) cm;
					ci = new ChatItem(Message.accessHelper(cMessage.sender, accessKey, cm.getDay(), true));
				}else
					ci = new ChatItem(cm.access(accessKey, true));
				ret.add(ci);
			}

			if(!local.isInProgress())
				ret.add(new ChatItem(local.getWinMessage().access(Message.PRIVATE, true)));

			return ret;
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

	public JSONArray getGraveyard(){
		if(server.IsLoggedIn()){
			return gameState.graveYard;
		}else{
			try {
				return StateObject.jGraveYard(local);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
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
				return currentPlayer.hasDayAction(Role.MAIN_ABILITY);
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
			return local.getPlayerByName(name).hasDayAction(Role.MAIN_ABILITY);
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


	public void shutdown() {
		// TODO Auto-generated method stub

	}

	public void toast(String message){
		for(NActivity na: nacs){
			na.toast(message);
		}
	}

	
}
