package android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.alerts.PlayerPopUp;
import android.app.FragmentManager;
import android.day.ActivityDay;
import android.setup.ActivityCreateGame;
import android.texting.StateObject;
import android.view.View;
import voss.narrator.R;

public class GameState {

	
	private NarratorService ns;
	public GameState(NarratorService ns) {
		this.ns = ns;
		players = new JSONObject();
		factions = new JSONObject();
		rolesList = new JSONArray();
		try {
			players.put("Lobby", new JSONArray());
			factions.put(StateObject.factionNames, new JSONArray());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public boolean isHost, isDay, isSkipping, showButton;
	
	public int skipVoteCount, timer;
	
	private boolean isStarted = false;
	private boolean isOver = false;
	public boolean isAlive = true;
	public boolean endedNight = false;
	public String hostName, dayLabel, chat = "";
	public JSONObject rules, factions, roleInfo, players;
	public JSONArray graveYard, rolesList;

	public void parse(JSONObject jo) throws JSONException {
		if(jo.has("gameStart"))
			isStarted = jo.getBoolean("gameStart");
		if(jo.has("isFinished"))
			isOver = jo.getBoolean("isFinished");
		if(jo.has("host"))
			hostName = jo.getString("host");
		if(jo.has(StateObject.endedNight))
			endedNight = jo.getBoolean(StateObject.endedNight);
		if(jo.has(StateObject.rules)){
			rules = jo.getJSONObject(StateObject.rules);
			factions = jo.getJSONObject(StateObject.factions);
			((ActivityCreateGame)ns.activity).resetView();
		}
		if(jo.has(StateObject.isHost)){
			isHost = jo.getBoolean(StateObject.isHost);
			int visibility;
			if(isHost)
				visibility = View.VISIBLE;
			else
				visibility = View.GONE;
			ns.activity.findViewById(R.id.create_createTeamButton).setVisibility(visibility);
		}
		if(jo.has(StateObject.roleInfo))
			roleInfo = jo.getJSONObject(StateObject.roleInfo);
		
		if(jo.has(StateObject.graveYard)){
			graveYard = jo.getJSONArray(StateObject.graveYard);
			JSONObject jPlayer;
			for(int i = 0; i < graveYard.length(); i++){
				jPlayer = graveYard.getJSONObject(i);
				if(jPlayer.getString("name").equals(ns.server.GetCurrentUserName())){
					isAlive = false;
				}
			}
				
		}
		
		if(jo.has(StateObject.isDay))
			isDay = jo.getBoolean(StateObject.isDay);
		
		if(jo.has(StateObject.showButton))
			showButton = jo.getBoolean(StateObject.showButton);
		
		if(jo.has(StateObject.skipVote)){
			skipVoteCount = jo.getInt(StateObject.skipVote);
			isSkipping = jo.getBoolean(StateObject.isSkipping);
		}
		
		if(jo.has(StateObject.playerLists)){
			players = jo.getJSONObject(StateObject.playerLists);
			FragmentManager fm = ns.activity.getFragmentManager();
			PlayerPopUp pPop = (PlayerPopUp) fm.get(ActivityCreateGame.PLAYER_POP_UP);
			if(pPop != null){
				pPop.updatePlayerList();
				pPop.setTitle();
			}
				
		}
		if(jo.has(StateObject.roles)){
			rolesList = jo.getJSONArray(StateObject.roles);
			if(ns.activity.getClass().equals(ActivityCreateGame.class)){
				((ActivityCreateGame) ns.activity).refreshRolesList();
			}else if(ns.activity.getClass().equals(ActivityDay.class)){
				
			}
		}
		if(jo.has(StateObject.dayLabel))
			dayLabel = jo.getString(StateObject.dayLabel);
		
		if(jo.has(StateObject.timer))
			timer = jo.getInt(StateObject.timer);
	}
    

}
