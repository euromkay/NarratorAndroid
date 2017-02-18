package android.day;

import java.util.ArrayList;

import android.CommunicatorPhone;
import android.JUtils;
import android.screens.SimpleGestureFilter;
import android.texting.StateObject;
import android.widget.TextView;
import json.JSONArray;
import json.JSONObject;
import shared.event.EventList;
import shared.event.Message;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.listeners.NarratorListener;
import shared.roles.Arsonist;
import shared.roles.Assassin;
import shared.roles.Mayor;
import voss.narrator.R;

public class DayScreenController{

	String currentPlayer;
	public ActivityDay dScreen;
	private DayManager manager;
	public DayScreenController(ActivityDay dScreen, DayManager manager){
		this.dScreen = dScreen;
		this.manager = manager;

		if(!dScreen.ns.server.IsLoggedIn()){
			manager.ns.local.addListener(new NarratorLocalListener(this));
		}
		//setNarratorInfoView();
	}

	public static class NarratorLocalListener implements NarratorListener{

		public DayScreenController dController;
		public NarratorLocalListener(DayScreenController dController){
			this.dController = dController;
		}
		public void onGameStart(){
			dController.onGameStart();
		}
		public void onNightStart(PlayerList lynched, PlayerList poisoned, EventList e){
			dController.onNightStart(lynched, poisoned);
		}
		public void onDayStart(PlayerList newDead){
			dController.onDayStart(newDead);
		}

		public void onEndGame(){
			dController.onEndGame();
		}
		
		public void onAssassination(Player assassin, Player victim, Message e){
			String name;
			if(victim == null)
				name = null;
			else
				name = victim.getName();
					
			dController.onAssassination(assassin.getName(), name);
		}


		public void onMayorReveal(Player mayor, Message e){
			dController.onMayorReveal(mayor.getName());
		}

		public void onArsonDayBurn(Player arson, PlayerList burned, Message e){
			dController.onArsonDayBurn(arson.getName(), burned.getNamesToStringList());

		}


		public void onVote(Player voter, Player target, int voteCount, Message e){
			dController.onVote(voter, target, voteCount, e);
		}
		public void onUnvote(Player voter, Player prev, int voteCountToLynch, Message e){
			dController.onUnvote(voter, prev, voteCountToLynch, e);
		}
		public void onChangeVote(Player voter, Player target, Player prevTarget, int toLynch, Message e){
			dController.onChangeVote(voter, target, prevTarget, toLynch, e);
		}



		public void onTargetSelection(Player owner){
			dController.onNightTarget();
		}
		public void onNightTargetRemove(Player owner, String command, PlayerList prev){
			dController.onNightTargetRemove(owner.getName(), command, prev.getNamesToStringList());
		}

		public void onEndNight(Player p){
			dController.onEndNight(p.getName());
		}
		public void onCancelEndNight(Player p){
			dController.onCancelEndNight(p.getName());
		}

		public void onMessageReceive(Player receiver, Message e){
			dController.onMessageReceive(receiver, e);
		}

		public void onModKill(Player bad){
			dController.onModKill(bad);
		}

		public void onNightEnding() {
			
		}

		public void onAnnouncement(Message nl) {
			
		}
	}

	public void init(){
		dScreen.setupFramerSpinner();
		dScreen.updateMembers();//will only get updated when someone has the potential for dying

		setDayLabel();

		setupPlayerDrawer();
		updatePlayerControlPanel();
		setRoles();
	}


	public static final String SKIP_NIGHT_TEXT = "End Night";
	public static final String CANCEL_SKIP_NIGHT_TEXT = "Cancel End Night";
	public static final String PlayerMenuHeader = "General Info";

	public void onGameStart(){}

	public void onNightStart(PlayerList lynched, PlayerList poisoned) {
		deadCurrentPlayerCheck(lynched.getNamesToStringList());//if the current player died or was lynched
		deadCurrentPlayerCheck(poisoned.getNamesToStringList());

		setDayLabel();
		dScreen.updateMembers();

		updatePlayerControlPanel();
		setupPlayerDrawer();

	}



	public void onDayStart(PlayerList newDead) {
		deadCurrentPlayerCheck(newDead.getNamesToStringList());

		setDayLabel();
		setVotesToLynch();//starts off at 0

		updatePlayerControlPanel();

		dScreen.updateMembers();

		//no one died
		if(!newDead.isEmpty())
			setupPlayerDrawer();
	}

	public void onEndGame() {
		//TODO SPEECH LISTENER
		//dScreen.say(getNarrator().getWinMessage().access(Message.PUBLIC, false));
		dScreen.endGame();
	}

	public void onMayorReveal(String mayor) {
		dScreen.say(mayor + " has revealed.");
		if (currentPlayer == null ||currentPlayer.equals(mayor ))
			dScreen.hideDayButton();
		updateChatPanel();

	}


	public void onArsonDayBurn(String arson, ArrayList<String> burned) {
		setupPlayerDrawer();
		if (currentPlayer.equals(arson))
			dScreen.hideDayButton();

		if (burned.contains(currentPlayer)){
			dScreen.onBackPressed();
			setNarratorInfoView();
		}
		updatePlayerControlPanel();
		dScreen.updateMembers();

	}

	public void onVote(Player voter, Player target, int voteCount, Message e) {
		updateActionPanel();
		updateChatPanel();
	}


	public void onUnvote(Player voter, Player prev, int voteCountToLynch, Message e) {
		onVote(voter, prev, voteCountToLynch, e);
	}


	public void onChangeVote(Player voter, Player target, Player prevTarget, int toLynch, Message e) {
		onVote(voter, target, toLynch, e);
	}


	public void onNightTarget() {
		updateActionPanel(); //overkill
		updateChatPanel();
	}

	public void onNightTargetRemove(String owner, String command, ArrayList<String> prev) {
		if (owner.equals(currentPlayer)){
			dScreen.uncheck(prev);
		}
		if (!playerSelected() && manager.getCommand().equalsIgnoreCase(command))
			dScreen.uncheck(owner);

		updateChatPanel();

	}

	public void onEndNight(String p) {
		updateActionPanel();
		if(p.equals(currentPlayer))
			setCancelSkipNightText();
	}

	public void onCancelEndNight(String canceler) {
		//if someone isn't selected, update the action panel,
		//if person that just canceled the end night, they need their action panel updated
		//if current doesn't have their night canceled, also refresh action panel
		if(playerSelected() && !canceler.equals(currentPlayer) && !manager.ns.endedNight(currentPlayer))
			return;
		updateActionPanel();
		setSkipNightText();
	}

	public void onMessageReceive(Player p, Message e) {
		updateChatPanel();
	}

	public void onModKill(Player bad) {
		updatePlayerControlPanel();
		setupPlayerDrawer();
		dScreen.onClick(dScreen.chatButton);
	}


















	public boolean playerSelected(){
		return currentPlayer != null;
	}


	private void deadCurrentPlayerCheck(ArrayList<String> possibleDeadList){
		if(possibleDeadList.contains(currentPlayer)) {
			manager.setCurrentPlayer(null);
			setNarratorInfoView();
		}
	}

	public void setNarratorInfoView(){
		currentPlayer = null;
		dScreen.onClick(dScreen.infoButton);
	}

	public void setDayLabel(){
		if(dScreen.ns.server.IsLoggedIn())
			((TextView) dScreen.findViewById(R.id.day_title)).setText(manager.ns.gameState.dayLabel);
		else
			dScreen.setDayLabel(manager.ns.isDay(), this.manager.ns.local.getDayNumber());
	}

	private void setupPlayerDrawer() {
		JSONArray jArray = new JSONArray();
		if(dScreen.ns.server.IsLoggedIn()){
			jArray.put(dScreen.ns.server.GetCurrentUserName());
		}else{
			Narrator n = manager.ns.local;
			PlayerList list;
			if(manager.isHost()) {
				list = n.getLivePlayers();
			}else{
				list = new PlayerList();
				for(Player p: n.getLivePlayers()){
					if(p.getCommunicator().getClass() == CommunicatorPhone.class){
						list.add(p);
					}
				}
			}
			for(Player p: list){
				jArray.put(p.getName());
			}
		}
		dScreen.setupPlayerDrawer(jArray);
	}
	protected void updatePlayerControlPanel(){
		if (dScreen.drawerOut())//when the drawer closes, it'll update, so no need to do it now, you don't even know who you're supposed to udpate it for!
			return;

		String color;
		if (playerSelected()) {
			dScreen.setPlayerLabel(currentPlayer);
			color = manager.ns.getColor(currentPlayer);
		}else {
			dScreen.setPlayerLabel(PlayerMenuHeader);
			color = "#49C500";//NActivity.ParseColor(dScreen, R.color.trimmings);
		}
		dScreen.setTrimmings(color);
		dScreen.showButton();
		updateActionPanel();
		updateInfoPanel();
		updateChatPanel();
		if(dScreen.panel != null)//restore panel
			dScreen.onClick(dScreen.panel);
	}
	
	public void onAssassination(String assassin, String killed){
		ArrayList<String> dead = new ArrayList<>();
		if(killed != null)
			dead.add(killed);
		onArsonDayBurn(assassin, dead);
	}

	public void updateActionPanel() {
		dScreen.setActionButton();
		JSONObject playerListObject = manager.ns.getPlayers(currentPlayer);
		if (playerSelected() && !manager.ns.isDead(currentPlayer)){
			JSONArray types = JUtils.getJSONArray(playerListObject, StateObject.type);
			String key = null;
			if(types.length() == 0) {
				dScreen.setCommand("End the night and wait for morning");
				dScreen.setActionList(new JSONArray(), isDay());
			}else {
				key = JUtils.getString(types, abilityIndex % types.length());
				JSONArray playerList = JUtils.getJSONArray(playerListObject, key);
				dScreen.setCommand(key);
				dScreen.setActionList(playerList, manager.ns.isDay());
			}
			if(manager.ns.isDay() && key.equals("Vote"))
				setVotesToLynch();
			else
				setButtonText();
		} else {
			//dScreen.clearTargetList();
			if(playerListObject.has("info")){
				JSONArray list = JUtils.getJSONArray(playerListObject, "info");

				dScreen.setActionList(list, isDay());
			}
			if (isDay())
				dScreen.setCommand("People who haven't voted:");
			else
				dScreen.setCommand(HAVENT_ENDED_NIGHT_TEXT);
		}
		dScreen.showButton();
		dScreen.showFrameSpinner();
	}
	
	public static final String HAVENT_ENDED_NIGHT_TEXT = "People who haven't ended the night:";

	protected void updateInfoPanel(){
		if (playerSelected()) {
			setAllies();
			dScreen.updateRoleInfo(manager.ns.getRoleInfo(currentPlayer));
			setButtonText();
		}
		dScreen.showAllies();
	}
	private void setAllies(){
		JSONObject roleInfo = manager.ns.getRoleInfo(currentPlayer);
		ArrayList<String> names = new ArrayList<>();
		ArrayList<String> colors = new ArrayList<>();
		if (roleInfo.has(StateObject.roleTeam)) {
			JSONArray allyList = JUtils.getJSONArray(roleInfo, StateObject.roleTeam);
			for (int i = 0; i < allyList.length(); i++) {
				JSONObject jAlly = JUtils.getJSONObject(allyList, i);
				String allyName = JUtils.getString(jAlly, StateObject.teamAllyName);
				String allyRole = JUtils.getString(jAlly, StateObject.teamAllyRole);
				names.add(allyName + "[" + allyRole + "]");

				String allyColor = JUtils.getString(jAlly, StateObject.teamAllyColor);
				colors.add(allyColor);
			}
		}
		dScreen.setListView(dScreen.alliesLV, names, colors, 5);

	}
	private void setRoles(){
		JSONArray jRoles = manager.ns.getRoles();
		//RolesList roles = getNarrator().getAllRoles();
		ArrayList<String> names = new ArrayList<>();
		ArrayList<String> colors = new ArrayList<>();
		JSONObject jRole;
		for (int i = 0; i < jRoles.length(); i++){
			jRole = JUtils.getJSONObject(jRoles, i);
			names.add(JUtils.getString(jRole, StateObject.roleType));
			colors.add(JUtils.getString(jRole, StateObject.color));
		}


		dScreen.setListView(dScreen.rolesLV, names, colors);
	}

	public void updateChatPanel(){
		dScreen.updateChatPanel();
	}
	
	public void setButtonText(){
		if (!playerSelected() || manager.ns.isDead(currentPlayer)) {
			return;
		}
		if(!manager.ns.showButton(currentPlayer)){
			return;
		}

		if (isDay()){
			if (!manager.ns.hasDayAction(currentPlayer))
				return;
			JSONObject roleInfo = manager.ns.getRoleInfo(currentPlayer);
			String baseRoleName = JUtils.getString(roleInfo, StateObject.roleBaseName);
			if (baseRoleName.equals(Mayor.ROLE_NAME))
				dScreen.setButtonText("Reveal as Mayor (+" + manager.ns.getMayorVotePower() + " votes)");
			else if (baseRoleName.equals(Arsonist.ROLE_NAME))
				dScreen.setButtonText("Burn all doused targets");
			else if (baseRoleName.equals(Assassin.ROLE_NAME))
				dScreen.setButtonText("Assassinate");
		}else{
			if (manager.ns.endedNight(currentPlayer))
				setCancelSkipNightText();
			else
				setSkipNightText();
		}
	}
	
	public void setVotesToLynch() {
		dScreen.setVotesToLynch(manager.ns.getMinLynchVote());
	}
	
	public void setCancelSkipNightText(){
		dScreen.setButtonText(CANCEL_SKIP_NIGHT_TEXT);
	}
	
	public void setSkipNightText(){
		dScreen.setButtonText(SKIP_NIGHT_TEXT);
	}
	
	
	private int abilityIndex = 0;
	protected void setNextAbility(int direction){
		if (!playerSelected() || manager.ns.isDead(currentPlayer))
			return;
		JSONObject roleInfo = manager.ns.getRoleInfo(manager.getCurrentPlayer());
		String roleName = JUtils.getString(roleInfo, StateObject.roleBaseName);
		if(isDay() && !roleName.equals(Assassin.ROLE_NAME)){
			return;
		}
		JSONObject playerInfo = manager.ns.getPlayers(currentPlayer);
		JSONArray abilityTypes = JUtils.getJSONArray(playerInfo, StateObject.type);
		if(abilityTypes.length() == 0)
			return;
		if (direction == SimpleGestureFilter.SWIPE_LEFT) {
			abilityIndex--;
			if (abilityIndex < 1){
				abilityIndex += abilityTypes.length();
			}
		}else
			abilityIndex++;
		updateActionPanel();
	}
	
	
	
	
	private boolean isDay(){
		return manager.ns.isDay();
	}
}
