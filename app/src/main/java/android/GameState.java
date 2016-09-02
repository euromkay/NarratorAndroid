package android;

import android.alerts.PlayerPopUp;
import android.day.ActivityDay;
import android.setup.ActivityCreateGame;
import android.texting.StateObject;
import android.view.View;
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
		chat = new StringBuilder();
	}

	public boolean isHost, isDay, isSkipping, showButton;
	
	public int skipVoteCount, timer, mayorVoteCount;

	public boolean isStarted = false;
	public boolean isOver = false;
	public boolean isAlive = true;
	public boolean endedNight = false;
	public String hostName, dayLabel;
	private StringBuilder chat;
	public JSONObject rules, factions, roleInfo, players;
	public JSONArray graveYard, rolesList, activeTeams;

	public String getChat(){
		return chat.toString();
	}
	
	public void resetChat(){
		chat = new StringBuilder();
	}
	
	public void addToChat(String s){
		chat.append(s);
		chat.append("\n");
	}
	
	public void parse(JSONObject jo) throws JSONException {
		if(jo.has(StateObject.gameStart))
			isStarted = jo.getBoolean(StateObject.gameStart);
		if(jo.has(StateObject.isFinished))
			isOver = jo.getBoolean(StateObject.isFinished);
		if(jo.has(StateObject.host))
			hostName = jo.getString(StateObject.host);
		if(jo.has(StateObject.isDay))
			isDay = jo.getBoolean(StateObject.isDay);
		if(jo.has(StateObject.endedNight)){
			endedNight = jo.getBoolean(StateObject.endedNight);
			if(isActivityDay()){
				runOnMain(new Runnable(){
					public void run(){
						getActivityDay().manager.dScreenController.setButtonText();
					}
				});
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
						ActivityCreateGame ac = (ActivityCreateGame) ns.activity;
						ac.resetView();
					}
				});
			
			JSONObject mayorRule = rules.getJSONObject(Rules.MAYOR_VOTE_POWER[0]);
			mayorVoteCount = JUtils.getInt(mayorRule, "val");
		}
		if(jo.has(StateObject.isHost)){
			isHost = jo.getBoolean(StateObject.isHost);
			if(isActivityCreate()) {
				final View v = ns.activity.findViewById(R.id.create_createTeamButton);
				if (v != null)
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
			if(isActivityDay()){
				runOnMain(new Runnable(){
					public void run(){
						getActivityDay().showButton();
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
			if(isActivityCreate()){
				final PlayerPopUp pPop = ((ActivityCreateGame) ns.activity).pPop;
				if(pPop != null){
					runOnMain(new Runnable(){
						public void run(){
							pPop.updatePlayerList();
							pPop.setTitle();
						}
					});
				}
			}else if(isActivityDay()){
				runOnMain(new Runnable(){
					public void run(){
						getActivityDay().manager.dScreenController.updateActionPanel();
					}
				});
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
			runOnMain(new Runnable(){
				public void run(){
					if(isActivityDay()){
						getActivityDay().manager.dScreenController.setDayLabel();
					}
				}
			});
		}
		if(jo.has(StateObject.timer))
			timer = jo.getInt(StateObject.timer);
	}

	private boolean isActivityCreate(){
		if(ns.activity == null)
			return false;
		return ns.activity.getClass().equals(ActivityCreateGame.class);
	}

	private boolean isActivityDay(){
		if(ns.activity == null)
			return false;
		return ns.activity.getClass().equals(ActivityDay.class);
	}
	private ActivityDay getActivityDay(){
		return (ActivityDay) ns.activity;
	}

	private void refreshRolesList(){
		runOnMain(new Runnable(){
			public void run(){
				((ActivityCreateGame) ns.activity).refreshRolesList();
			}
		});
	}

	private void runOnMain(Runnable r){
		if(ns.activity != null)
			ns.activity.runOnUiThread(r);

	}

	public void refreshChat() {
		Runnable r = new Runnable(){
			public void run(){
				if(ns.activity == null)
					return;
				if(ns.activity.getClass() == ActivityCreateGame.class){
					((ActivityCreateGame) ns.activity).updateChat(); 
				}else if(ns.activity.getClass() == ActivityDay.class){
					((ActivityDay) ns.activity).manager.dScreenController.updateChatPanel();
				}
			}
		};
		runOnMain(r);
		
	}

}
