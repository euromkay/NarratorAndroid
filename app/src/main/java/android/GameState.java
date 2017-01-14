package android;

import android.alerts.PlayerPopUp;
import android.day.ActivityDay;
import android.day.ChatItem;
import android.setup.ActivityCreateGame;
import android.texting.StateObject;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import shared.logic.support.rules.Rules;
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
		chat = new ArrayList<>();
	}


	public boolean isHost, isDay, isSkipping, showButton;
	
	public int skipVoteCount, timer, mayorVoteCount;

	public boolean seenMessage = false;
	public boolean isStarted = false;
	public boolean isOver = false;
	public boolean isAlive = true;
	public boolean endedNight = false;
	public String hostName, dayLabel;
	private ArrayList<ChatItem> chat;
	public JSONObject rules, factions, roleInfo, players;
	public JSONArray graveYard, rolesList, activeTeams;

	public ArrayList<ChatItem> getChat(){
		return chat;
	}
	
	public void resetChat(){
		chat.clear();
	}
	
	public void addToChat(ChatItem ci){
		chat.add(ci);
	}
	
	public void parse(JSONObject jo) throws JSONException {
		if(jo.has(StateObject.gameStart)) {
			isStarted = jo.getBoolean(StateObject.gameStart);
			Log.d("myauth", "seenMessage is now true");
			seenMessage = true;
		}if(jo.has(StateObject.isFinished))
			isOver = jo.getBoolean(StateObject.isFinished);
		if(jo.has(StateObject.host))
			hostName = jo.getString(StateObject.host);
		if(jo.has(StateObject.isDay))
			isDay = jo.getBoolean(StateObject.isDay);
		if(jo.has(StateObject.endedNight)){
			endedNight = jo.getBoolean(StateObject.endedNight);
			for(final NActivity nac: ns.nacs){
				if(nac instanceof ActivityDay){
					runOnMain(new Runnable(){
						public void run(){
							ActivityDay ad = (ActivityDay) nac;
							ad.manager.dScreenController.setButtonText();
						}
					});
				}
			}
		}
		if(jo.has(StateObject.activeTeams))
			activeTeams = jo.getJSONArray(StateObject.activeTeams);
		if(jo.has(StateObject.rules)){
			rules = jo.getJSONObject(StateObject.rules);
			factions = jo.getJSONObject(StateObject.factions);
			if(isActivityCreate())
				runOnMain(new Runnable(){
					public void run(){
						for(NActivity ac : ns.nacs){
							ac.resetView();
						}
					}
				});
			
			JSONObject mayorRule = rules.getJSONObject(Rules.MAYOR_VOTE_POWER[0]);
			mayorVoteCount = JUtils.getInt(mayorRule, "val");
		}
		if(jo.has(StateObject.isHost)){
			isHost = jo.getBoolean(StateObject.isHost);
			if(isActivityCreate()) {
				for(NActivity nac: ns.nacs){
					final View v = nac.findViewById(R.id.create_createTeamButton);
					if (v != null && nac instanceof ActivityCreateGame)
						runOnMain(new Runnable() {
							public void run() {
								int visibility;
								if (isHost)
									visibility = View.VISIBLE;
								else
									visibility = View.GONE;
								v.setVisibility(visibility);
							}
						});
				}

			}
		}
		if(jo.has(StateObject.roleInfo))
			roleInfo = jo.getJSONObject(StateObject.roleInfo);
		
		if(jo.has(StateObject.graveYard)){
			graveYard = JUtils.getJSONArray(jo, StateObject.graveYard);
			JSONObject jPlayer;
			for(int i = 0; i < graveYard.length(); i++){
				jPlayer = graveYard.getJSONObject(i);
				if(jPlayer.getString("name").equals(ns.server.GetCurrentUserName())){
					isAlive = false;
				}
			}
				
		}
		
		
		if(jo.has(StateObject.showButton)){
			showButton = jo.getBoolean(StateObject.showButton);
			for(final NActivity nac: ns.nacs){
				if(nac instanceof ActivityDay)
					runOnMain(new Runnable(){
						public void run(){
							((ActivityDay) nac).showButton();
						}
					});
			}
		}
		
		if(jo.has(StateObject.skipVote)){
			skipVoteCount = jo.getInt(StateObject.skipVote);
			isSkipping = jo.getBoolean(StateObject.isSkipping);
		}
		
		if(jo.has(StateObject.playerLists)){
			players = jo.getJSONObject(StateObject.playerLists);
			for(final NActivity nac: ns.nacs){
				if((nac instanceof ActivityCreateGame)){
					final PlayerPopUp pPop = ((ActivityCreateGame) nac).pPop;
					if(pPop != null){
						runOnMain(new Runnable(){
							public void run(){
								pPop.updatePlayerList();
								pPop.setTitle();
							}
						});
					}
				}else if(nac instanceof ActivityDay){
					runOnMain(new Runnable(){
						public void run(){
							((ActivityDay) nac).manager.dScreenController.updateActionPanel();
						}
					});
				}
			}
				
		}
		if(jo.has(StateObject.roles)){
			rolesList = jo.getJSONArray(StateObject.roles);
			if(isActivityCreate()){
				refreshRolesList();
			}else if(isActivityDay()){
				
			}
		}
		if(jo.has(StateObject.dayLabel)){
			dayLabel = jo.getString(StateObject.dayLabel);
			for(final NActivity nac: ns.nacs) {
				if(nac instanceof ActivityDay)
					runOnMain(new Runnable() {
					public void run() {
						((ActivityDay) nac).manager.dScreenController.updateActionPanel();
					}
				});
			}
		}
		if(jo.has(StateObject.timer))
			timer = jo.getInt(StateObject.timer);
	}

	private boolean isActivityCreate(){
		return ns.activityCreateGameActive();
	}

	private boolean isActivityDay(){
		return ns.activityDayActive();
	}

	/*private ActivityDay getActivityDay(){
		return (ActivityDay) ns.getActivity();
	}*/

	private void refreshRolesList(){
		for(final NActivity nac: ns.nacs) {
			nac.runOnUiThread(new Runnable(){
				public void run() {
					nac.refreshRolesList();
				}
			});
		}
	}

	private void runOnMain(Runnable r){
		for(NActivity nac: ns.nacs)
			nac.runOnUiThread(r);

	}

	public void refreshChat() {
		Runnable r;
		for(final NActivity nac: ns.nacs){
			r = new Runnable(){
				public void run(){
				nac.updateChat();
				}
			};
			runOnMain(r);
		}

		
	}

}
