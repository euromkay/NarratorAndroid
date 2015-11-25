package voss.android.day;

import java.util.ArrayList;
import java.util.Random;

import voss.android.CommunicatorPhone;
import voss.android.PhoneBook;
import voss.android.screens.SimpleGestureFilter;
import voss.android.texting.TextHandler;
import voss.shared.ai.Simulations;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.logic.templates.TestController;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Framer;
import voss.shared.roles.Mayor;

public class DayManager implements NarratorListener {

	public   ActivityDay dScreen;
	protected PhoneBook phoneBook;
	private   Player currentPlayer;

	private static final String SKIP_NIGHT_TEXT = "End Night";
	private static final String CANCEL_SKIP_NIGHT_TEXT = "Cancel End Night";
	
	public static final String PlayerMenuHeader = "Narrator Info";

	
	protected DayManager(ActivityDay dScreen, Narrator n){
		this.dScreen = dScreen;
		this.n = n;
		phoneBook = new PhoneBook(n);


	}
	private Simulations simulations;
	private IpDayListener ipManager;
	private boolean isHost;
	protected void startIpListener(boolean isHost){
		ipManager = new IpDayListener(isHost, n, dScreen);
		this.isHost = isHost;
	}
	protected void initiate(){
		dScreen.setupFramerSpinner();
		setDayLabel();
		setupPlayerDrawer();

		updatePlayerControlPanel();

		resetNarratorInfoView();


		n.addListener(this);
		dScreen.tHandler = new TextHandler(n);
		

		simulations = new Simulations(new TestController(n), new Random());
	}

	public void resetNarratorInfoView(){
		currentPlayer = null;
		dScreen.onClick(dScreen.infoButton);
	}

	protected void updateInfoPanel(){
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
			setButton();
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

	private int abilityIndex = 0;
	protected void updateActionPanel() {
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
				dScreen.check(n.getVoteTarget(currentPlayer));
				setVotesToLynch();
			} else {
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
				setButton();

			}

		} else {
			//dScreen.clearTargetList();
			PlayerList list = new PlayerList();
			for (Player p : n.getLivePlayers()) {
				if (isDay()) {
					if (n.getVoteTarget(p) == null)
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



	public void setDayLabel(){
		dScreen.setDayLabel(isDay(), n.getDayNumber());
	}
	public void buttonClick(){
		synchronized(ipManager){
			if (isDay())
					dayAction(currentPlayer);
			else{
				if (currentPlayer.endedNight()) {
					cancelEndNight(currentPlayer);
				}
				else {
					endNight(currentPlayer);
				}
			}
		}
	}

	public void dayAction(Player p){
		p.doDayAction(!isHost);
	}

	public void onMayorReveal(Player mayor){
		dScreen.say(mayor.getName() + " has revealed.");
		if (currentPlayer == mayor)
			dScreen.hideDayButton();
		updateChatPanel();
	}

	public void onArsonDayBurn(Player arson, PlayerList burned){
		setupPlayerDrawer();
		if (currentPlayer == arson)
			dScreen.hideDayButton();

		if (currentPlayer != null && burned.contains(currentPlayer)){
			dScreen.onBackPressed();
			dScreen.onBackPressed();
		}
		updateChatPanel();
	}
	private void updateChatPanel(){
		dScreen.updateChatPanel();
	}

	public void vote(Player owner, Player target){
		owner.vote(target, !isHost);
	}
	public void onVote(Player owner, Player target, int voteCount){
		updateActionPanel();
		dScreen.updateChatPanel();
	}
	public void onUnvote(Player owner, Player target, int voteCount){
		onVote(owner, target, voteCount);
	}
	public void onChangeVote(Player owner, Player target, Player prev, int voteCount){
		onVote(owner, target, voteCount);
	}

	public void unvote(Player p){
		p.unvote(!isHost);
	}
	public void target(Player owner, Player target, int ability){
		if(owner.getRoleName().equals(Framer.ROLE_NAME))
			owner.setTarget(target, ability, dScreen.getSpinnerSelectedID(), !isHost);
		else
			owner.setTarget(target, ability, !isHost);

	}

	public void onNightTarget(Player owner, Player target){
		if (owner == currentPlayer) {
			dScreen.check(target);
		}
		if (currentPlayer == null)
			dScreen.check(owner);
		updateChatPanel();
	}

	public void untarget(Player owner, Player target, int ability){
		owner.removeTarget(ability, true);
	}
	public void onNightTargetRemove(Player owner, Player prev){
		if (owner == currentPlayer) {
			dScreen.uncheck(prev);
		}
		if (currentPlayer == null)
			dScreen.uncheck(owner);
		updateChatPanel();
	}

	public void talk(Player p, String message){
		p.say(message, isHost);
	}
	public void onMessageReceive(Player p){
		updateChatPanel();
	}

	public void setVotesToLynch() {
		dScreen.setVotesToLynch(n.getMinLynchVote());
	}

	public void cancelEndNight(Player p) {
		p.cancelEndNight(!isHost);
	}

	public void setSkipNightText(){
		dScreen.setButtonText(SKIP_NIGHT_TEXT);
	}

	public void endNight(Player p){
		p.endNight(!isHost);
	}

	public void setCancelSkipNightText(){
		dScreen.setButtonText(CANCEL_SKIP_NIGHT_TEXT);

	}

	//from gui input
	//garuntee that someone is selected
	protected void command(Player target){
		if (target == null){
			//probably just frame being set
			return;
		}
		if(!playerSelected()){
			dScreen.uncheck(target);
			return;
		}
		synchronized(ipManager){
			if(n.isDay()){
				boolean unvote = n.getVoteListOf(target).contains(currentPlayer);
				//if owner voted for target already, gotta be an unvote
				if(unvote)
					unvote(currentPlayer);
				else
					vote(currentPlayer, target);
			}else {
				String ability_s = dScreen.getSelectedAbility();
				int ability = currentPlayer.parseAbility(ability_s);
				Player prev = currentPlayer.getTarget(ability);
	
				//untargeting someone
				if (target == prev)
					untarget(currentPlayer, prev, ability);
				else {
					target(currentPlayer, target, ability);
				}
			}
		}
	}
	

	

	protected void setCurrentPlayer(Player p){
		currentPlayer = p;
	}
	protected void updatePlayerControlPanel(){
		if (dScreen.drawerOut())
			return;

		if (playerSelected())
			dScreen.setPlayerLabel(currentPlayer.getName());
		else
			dScreen.setPlayerLabel(PlayerMenuHeader);
		updateActionPanel();
		updateInfoPanel();
		dScreen.updateChatPanel();
		if(dScreen.panel != null)
			dScreen.onClick(dScreen.panel);
	}
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


	
	public void onNightStart(PlayerList lynched) {
		deadCurrentPlayerCheck(lynched);

		setDayLabel();
		updatePlayerControlPanel();

		setupPlayerDrawer();
	}

	private void deadCurrentPlayerCheck(PlayerList possibleDeadList){
		if(possibleDeadList.contains(currentPlayer))
			resetNarratorInfoView();
	}

	public void onDayStart(PlayerList newDead) {
		deadCurrentPlayerCheck(new PlayerList());
		setDayLabel();
		setVotesToLynch();//starts off at 0

		updatePlayerControlPanel();

		
		//no one died

		setupPlayerDrawer();

	}

	private void setupPlayerDrawer() {
		PlayerList list;
		if(!isHost){
			list = new PlayerList();
			for(Player p: n.getLivePlayers()){
				if(p.getCommunicator().getClass() == CommunicatorPhone.class){
					list.add(p);
				}
			}
		}else{
			list = n.getLivePlayers();
		}
		dScreen.setupPlayerDrawer(list);
	}

	public void onEndGame() {
		dScreen.say(n.getWinMessage());
		dScreen.endGame(n);
	}
	

	private Narrator n;
	public boolean isDay(){
		return n.isDay();
	}
	public Narrator getNarrator(){
		return n;
	}
	
	
	


	public void setButton(){
		if (!playerSelected()) {
			return;
		}
		if (isDay()){
			if (!currentPlayer.hasDayAction()) {
				return;
			}else if (currentPlayer.is(Mayor.ROLE_NAME))
				dScreen.setButtonText("Reveal as Mayor (+" + n.getRules().mayorVoteCount + " votes)");
			else if (currentPlayer.is(Arsonist.ROLE_NAME))
				dScreen.setButtonText("Burn all doused targets");
		}else{
			if (currentPlayer.endedNight())
				setCancelSkipNightText();
			else
				setSkipNightText();
		}
	}

	public void nextSimulation(){
		simulations.next();
	}

	public void onEndNight(Player p){
		if(playerSelected() && !currentPlayer.endedNight())
			return;
		updateActionPanel();
		setCancelSkipNightText();
	}
	public void onCancelEndNight(Player p){
		if(playerSelected() && !currentPlayer.endedNight())
			return;
		updateActionPanel();
		setSkipNightText();
	}


	public Player getCurrentPlayer(){
		return currentPlayer;
	}

	public boolean playerSelected(){
		return currentPlayer != null;
	}

	
	public void onModKill(Player p){
		updatePlayerControlPanel();
		setupPlayerDrawer();
		dScreen.onBackPressed();
	}
}
