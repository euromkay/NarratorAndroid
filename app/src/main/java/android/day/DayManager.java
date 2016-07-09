package android.day;

import android.content.Intent;

import android.NarratorService;
import android.PhoneBook;
import android.parse.GameListing;
import android.parse.Server;
import android.setup.TextAdder;
import android.texting.TextController;
import android.texting.TextHandler;
import android.texting.TextInput;

import shared.ai.Brain;
import shared.ai.Simulations;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.support.Constants;
import shared.logic.support.Random;
import shared.logic.templates.TestController;
import shared.roles.Framer;

public class DayManager implements TextInput{

	
	private   Player currentPlayer;

	
	public DayScreenController dScreenController;
	private TextController tC;
	public NarratorService ns;
	public PhoneBook phoneBook;
	protected TextHandler tHandler;
	public DayManager(NarratorService ns){
		this.ns = ns;
	}
	public void initiate(ActivityDay dScreen){
		dScreenController = new DayScreenController(dScreen, this);
		dScreenController.init();
		dScreenController.setNarratorInfoView();
		ns.local.addListener(dScreenController);

		TextInput th = null;
		if (isHost())
			th = this;
		tHandler = new TextHandler(ns.local, th, TextAdder.getTexters(ns.local.getAllPlayers()));
		
		phoneBook = new PhoneBook(ns.local);

		tC = new TextController(this);

		Brain b = new Brain(new PlayerList(), new Random());

		for(Player p: ns.local.getAllPlayers()){
			if(p.isComputer()){
				b.slaves.add(p);
			}
		}


	}
	private Simulations simulations;


	
	public void buttonClick(){
		synchronized(ns.local){
			if(!dScreenController.playerSelected() || dScreenController.currentPlayer.isDead())
				return;
			if (getNarrator().isDay())
				dayAction(currentPlayer);
			else{
				if (currentPlayer.endedNight()) {
					cancelEndNight(currentPlayer);
				}else {
					endNight(currentPlayer);
				}
			}
		}
	}

	public void dayAction(Player p) {

		if(isHost()){
			p.doDayAction();
			tC.doDayAction(p);
		}else{
			tC.doDayAction(p);
		}
	}

	public void skipVote(Player owner){
		if(isHost()){
			owner.voteSkip();
		}
		tC.skipVote(owner);
	}

	public void vote(Player owner, Player target) {
		if(owner == target)
			return;
		if(isHost()){
			owner.vote(target);
		}
		tC.vote(owner, target);
	}

	public void unvote(Player owner){
		if(isHost()){
			owner.unvote();
		}
		tC.unvote(owner);
	}
	public void target(Player owner, Player target, String ability_s){
		int ability = owner.parseAbility(ability_s);
		if(owner.getRoleName().equals(Framer.ROLE_NAME)){
			Team t = dScreenController.dScreen.getSpinnerSelectedTeam();
			owner.setTarget(target, ability, t.getColor());
			
			tC.setNightTarget(owner, target, ability_s, t.getName());
		}else{
			owner.setTarget(target, ability);
			
			tC.setNightTarget(owner, target, ability_s);
		}

	}

	public void untarget(Player owner, Player target, String ability_s) {
		owner.cancelTarget(target, owner.parseAbility(ability_s));
		
		tC.cancelNightTarget(owner, target, ability_s);
	}


	public void talk(Player p, String message) {
		synchronized(ns.local){
			if(p.isDead() || p.isBlackmailed() || !p.getTeam().knowsTeam())
				return;
			String key = Constants.REGULAR_CHAT;
			if(ns.local.isNight())
				key = p.getTeam().getName();
				
			p.say(message, key);
			tC.say(p, message, key);
		}
	}

	

	public void cancelEndNight(Player p) {
		if(isHost()){
			p.cancelEndNight();
		}
		tC.cancelEndNight(p);
	}

	

	public void endNight(Player p){
		if(isHost()){
			p.endNight();
		}
		tC.endNight(p);
	}

	

	//from gui input
	//garuntee that someone is selected
	protected void command(Player target){
		if (!dScreenController.playerSelected() || (getNarrator().isNight() && getCurrentPlayer().endedNight()) || dScreenController.currentPlayer.isDead()) {
			dScreenController.updateActionPanel();
			return;
		}
		
		if (target == null){
			//probably just frame being set
			return;
		}
		if(!dScreenController.playerSelected()){
			dScreenController.dScreen.uncheck(target);
			return;
		}
		
		synchronized(ns.local){
		Narrator n = getNarrator();
		if(n.isDay()){
			boolean unvote = target.getVoters().contains(currentPlayer);
			//if owner voted for target already, gotta be an unvote
			if(unvote)
				unvote(currentPlayer);
			else{
				if(target == n.Skipper)
					skipVote(currentPlayer);
				else
					vote(currentPlayer, target);
			}
		}else {
			String ability_s = dScreenController.dScreen.getSelectedAbility();
			int ability = currentPlayer.parseAbility(ability_s);
			if(ability == -1){
				System.out.println("bad line is \t"+ability_s);
			}
			target(currentPlayer, target, ability_s);
		}
		}
	}
	

	

	protected void setCurrentPlayer(Player p){
		currentPlayer = p;
		dScreenController.currentPlayer = p;
	}
	protected void setNextAbility(int direction){
		dScreenController.setNextAbility(direction);
	}



	
	
	public Narrator getNarrator(){
		return ns.local;
	}

	public void nextSimulation(){
		if(simulations!= null)
			simulations.next();
	}

	


	public Player getCurrentPlayer(){
		return currentPlayer;
	}

	


	public boolean isHost() {
		if(Server.IsLoggedIn())
			return false;
		if(dScreenController.dScreen.networkCapable())
			return ns.socketHost != null;
		return true;
	}
	
	public void text(Player p, String s, boolean sync){
		s = p.getName() + Constants.NAME_SPLIT + s;
		if(isHost()){
			if(ns.socketHost != null)
				ns.socketHost.write(s);
		}else{
			if(Server.IsLoggedIn()) {
				s = "," + s;
				if(!sync)
					s = Server.GetCurrentUserName() + s;
				else
					s = " " + s;
				double day = ns.local.getDayNumber();
				if(ns.local.isNight())
					day += 0.5;
				Server.PushCommand(ns.getGameListing(), s, day);
			}else
				ns.socketClient.send(s);

		}
	}

	public void parseCommand(Intent i){
		if(!i.hasExtra(GameListing.ID))
			return;
		//check if someone else is pinging this
		if(!i.getStringExtra(GameListing.ID).equals(ns.getGameListing().getID()))
			return;

		String message = i.getStringExtra("stuff");
		String[] command = message.split(",");

		String sender = command[0];
		message = message.substring(sender.length() + 1);//1 length for comma
		if(sender.equals(" ") || !sender.equals(Server.GetCurrentUserName())){//everyone should do it.
			ns.onRead(message, null);//nulll for chat manager, unused in this function, also its synch protected.
		}
	}
}