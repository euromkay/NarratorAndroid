package voss.android.day;

import java.util.ArrayList;

import voss.android.CommunicatorPhone;
import voss.android.parse.Server;
import voss.android.screens.SimpleGestureFilter;
import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Mayor;

public class DayScreenController implements NarratorListener{

	Player currentPlayer;
	public ActivityDay dScreen;
	private DayManager manager;
	public DayScreenController(ActivityDay dScreen, DayManager manager){
		this.dScreen = dScreen;
		this.manager = manager;

		dScreen.setupFramerSpinner();

		setDayLabel();
		
		setupPlayerDrawer();
		updatePlayerControlPanel();
		//setNarratorInfoView();
	}
	
	public Narrator getNarrator(){
		return manager.getNarrator();
	}
	

	private static final String SKIP_NIGHT_TEXT = "End Night";
	private static final String CANCEL_SKIP_NIGHT_TEXT = "Cancel End Night";
	public static final String PlayerMenuHeader = "General Info";
	
	public void onNightStart(PlayerList lynched) {
		deadCurrentPlayerCheck(lynched);//if the current player died or was lynched

		setDayLabel();
		
		updatePlayerControlPanel();
		setupPlayerDrawer();
	}

	
	
	public void onDayStart(PlayerList newDead) {
		deadCurrentPlayerCheck(newDead);

		setDayLabel();
		setVotesToLynch();//starts off at 0

		updatePlayerControlPanel();
		
		//no one died
		if(!newDead.isEmpty())
			setupPlayerDrawer();
	}

	public void onEndGame() {
		//TODO SPEECH LISTENER
		dScreen.say(getNarrator().getWinMessage());
		dScreen.endGame();
	}

	public void onMayorReveal(Player mayor) {
		dScreen.say(mayor.getName() + " has revealed.");
		if (currentPlayer == mayor)
			dScreen.hideDayButton();
		updateChatPanel();
		
	}

	
	public void onArsonDayBurn(Player arson, PlayerList burned) {
		setupPlayerDrawer();
		if (currentPlayer == arson)
			dScreen.hideDayButton();

		if (burned.contains(currentPlayer)){
			dScreen.onBackPressed();
			setNarratorInfoView();
		}
		updatePlayerControlPanel();
		
	}

	public void onVote(Player voter, Player target, int voteCount) {
		updateActionPanel();
		updateChatPanel();
	}

	
	public void onUnvote(Player voter, Player prev, int voteCountToLynch) {
		onVote(voter, prev, voteCountToLynch);
	}

	
	public void onChangeVote(Player voter, Player target, Player prevTarget, int toLynch) {
		onVote(voter, target, toLynch);	
	}

	
	public void onNightTarget(Player owner, Player target) {
		if (owner == currentPlayer) {
			dScreen.check(target);
		}
		if (!playerSelected())
			dScreen.check(owner);
		
		Team t = owner.getTeam();
		if(t.hasMember(currentPlayer))
			updateChatPanel();
	}

	public void onNightTargetRemove(Player owner, Player prev) {
		if (owner == currentPlayer) {
			dScreen.uncheck(prev);
		}
		if (!playerSelected())
			dScreen.uncheck(owner);
		
		Team t = owner.getTeam();
		if(t.hasMember(currentPlayer))
			updateChatPanel();
		
	}

	public void onEndNight(Player p) {
		if(playerSelected() && !currentPlayer.endedNight())
			return;
		updateActionPanel();
		setCancelSkipNightText();
		
	}

	public void onCancelEndNight(Player p) {
		if(playerSelected() && !currentPlayer.endedNight())
			return;
		updateActionPanel();
		setSkipNightText();
	}

	public void onMessageReceive(Player owner) {
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
	
	
	private void deadCurrentPlayerCheck(PlayerList possibleDeadList){
		if(possibleDeadList.contains(currentPlayer))
			setNarratorInfoView();
	}
	
	public void setNarratorInfoView(){
		currentPlayer = null;
		dScreen.onClick(dScreen.infoButton);
	}
	
	public void setDayLabel(){
		Narrator n = getNarrator();
		dScreen.setDayLabel(isDay(), n.getDayNumber());
	}
	
	private void setupPlayerDrawer() {
		Narrator n = getNarrator();
		PlayerList list;
		if(Server.IsLoggedIn()){
			list = new PlayerList(n.getPlayerByName(Server.GetCurrentUserName()));
		}else if(manager.isHost()) {
			list = n.getLivePlayers();
		}else{
			list = new PlayerList();
			for(Player p: n.getLivePlayers()){
				if(p.getCommunicator().getClass() == CommunicatorPhone.class){
					list.add(p);
				}
			}
		}
		dScreen.setupPlayerDrawer(list);
	}
	protected void updatePlayerControlPanel(){
		if (dScreen.drawerOut())//when the drawer closes, it'll update, so no need to do it now, you don't even know who you're supposed to udpate it for!
			return;

		if (playerSelected())
			dScreen.setPlayerLabel(currentPlayer.getName());
		else
			dScreen.setPlayerLabel(PlayerMenuHeader);
		updateActionPanel();
		updateInfoPanel();
		updateChatPanel();
		if(dScreen.panel != null)//restore panel
			dScreen.onClick(dScreen.panel);
	}
	
	protected void updateActionPanel() {
		Narrator n = getNarrator();
		dScreen.setActionButton();
		if (playerSelected() && (isDay() || !currentPlayer.endedNight())) {
			if (isDay()) {
				PlayerList allowedVoteTargets;
				if (currentPlayer.isBlackmailed()){
					allowedVoteTargets = new PlayerList(n.Skipper);
				}
				else{
					allowedVoteTargets = n.getLivePlayers().remove(currentPlayer).add(n.Skipper);
				}
				dScreen.setActionList(allowedVoteTargets, Narrator.DAY_START);
				//dScreen.clearTargetList();
				dScreen.check(currentPlayer.getVoteTarget());
				setVotesToLynch();
			} else { //isNight
				PlayerList allowedTargets = new PlayerList();
				String[] allAbilities = currentPlayer.getAbilities();

				if (allAbilities.length != 0) {
					String ability = allAbilities[abilityIndex%allAbilities.length];

					int abilityID = currentPlayer.parseAbility(ability);

					for (Player p : n.getAllPlayers()) {
						if (currentPlayer.isAcceptableTarget(p, abilityID))
							allowedTargets.add(p);
					}
					dScreen.setActionList(allowedTargets, isDay());
					dScreen.check(currentPlayer.getTarget(abilityID));

					dScreen.setCommand(ability);
				} else {
					dScreen.setCommand("End the night and wait for morning");
					dScreen.setActionList(new PlayerList(), isDay());
				}

				dScreen.showFrameSpinner();
				setButtonText();

			}

		} else {
			//dScreen.clearTargetList();
			PlayerList list = new PlayerList();
			for (Player p : n.getLivePlayers()) {
				if (isDay()) {
					if (p.getVoteTarget() == null)
						list.add(p);
				} else {
					if (!p.endedNight())
						list.add(p);

				}
			}
			dScreen.setActionList(list, isDay());
			if (isDay())
				dScreen.setCommand("People who haven't voted:");
			else
				dScreen.setCommand("People who haven't ended the night:");
		
		}
	}
	
	protected void updateInfoPanel(){
		Narrator n = getNarrator();
		if (playerSelected()) {
			dScreen.setAlliesHeader(currentPlayer.getAlignment());
			int color = currentPlayer.getTeam().getAlignment();
			ArrayList<String> names = new ArrayList<>();
			ArrayList<Integer> colors = new ArrayList<>();
			if (currentPlayer.getTeam().knowsTeam()) {
				for (Player teamMember : currentPlayer.getTeam().getMembers()) {
					if (teamMember.isAlive()) {
						names.add(teamMember.toString());
						colors.add(color);
					}
				}
			}
			dScreen.setListView(dScreen.alliesLV, names, colors);

			dScreen.updateRoleInfo(currentPlayer, currentPlayer.getAlignment());
			setButtonText();
		}
		else{
			dScreen.setRolesListHeader();
			ArrayList<RoleTemplate> roles = n.getAllRoles();
			ArrayList<String> names = new ArrayList<>();
			ArrayList<Integer> colors = new ArrayList<>();
			for (RoleTemplate r: roles){
				names.add(r.getName());
				colors.add(r.getColor());
			}
			dScreen.setListView(dScreen.rolesLV, names, colors);

			dScreen.updateMembers();
		}
	}
	
	private static boolean HTML = false;
	private void updateChatPanel(){
		String text;
		if (!playerSelected())
			text = manager.getNarrator().getEvents(Event.PUBLIC, HTML);
		else
			text = getNarrator().getEvents(currentPlayer, true);
		dScreen.updateChatPanel(text);
	}
	
	public void setButtonText(){
		if (!playerSelected()) {
			return;
		}
		if (isDay()){
			if (!currentPlayer.hasDayAction()) {
				return;
			}else if (currentPlayer.is(Mayor.ROLE_NAME))
				dScreen.setButtonText("Reveal as Mayor (+" + getNarrator().getRules().mayorVoteCount + " votes)");
			else if (currentPlayer.is(Arsonist.ROLE_NAME))
				dScreen.setButtonText("Burn all doused targets");
		}else{
			if (currentPlayer.endedNight())
				setCancelSkipNightText();
			else
				setSkipNightText();
		}
	}
	
	public void setVotesToLynch() {
		dScreen.setVotesToLynch(getNarrator().getMinLynchVote());
	}
	
	public void setCancelSkipNightText(){
		dScreen.setButtonText(CANCEL_SKIP_NIGHT_TEXT);
	}
	
	public void setSkipNightText(){
		dScreen.setButtonText(SKIP_NIGHT_TEXT);
	}
	
	
	private int abilityIndex = 0;
	protected void setNextAbility(int direction){
		if (isDay() || !playerSelected() || currentPlayer.getAbilities().length < 1)
			return;
		if (direction == SimpleGestureFilter.SWIPE_LEFT) {
			abilityIndex--;
			if (abilityIndex < 1){
				abilityIndex += currentPlayer.getAbilities().length;
			}
		}else
			abilityIndex++;
		updateActionPanel();
	}
	
	
	
	
	private boolean isDay(){
		return getNarrator().isDay();
	}
}
