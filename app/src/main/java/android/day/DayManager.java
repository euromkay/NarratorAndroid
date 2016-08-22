package android.day;

import android.GUIController;
import android.NarratorService;
import android.PhoneBook;
import android.content.Intent;
import android.setup.TextAdder;
import android.texting.TextController;
import android.texting.TextHandler;
import android.texting.TextInput;
import shared.ai.Brain;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.support.Constants;
import shared.logic.support.Random;
import shared.logic.templates.TestController;
import shared.roles.Framer;

public class DayManager implements TextInput{

	
	private String currentPlayer;

	
	public DayScreenController dScreenController;
	private TextController tC;
	public NarratorService ns;
	public PhoneBook phoneBook;
	protected TextHandler tHandler;
	private Brain b;
	public DayManager(NarratorService ns){
		this.ns = ns;
	}
	public void initiate(ActivityDay dScreen){
		dScreenController = new DayScreenController(dScreen, this);
		dScreenController.init();
		dScreenController.setNarratorInfoView();


		tHandler = new TextHandler(ns.local, TextAdder.getTexters(ns.local.getAllPlayers()));
		
		phoneBook = new PhoneBook(ns.local);

		tC = new TextController(this);

		b = new Brain(new PlayerList(), new Random());

		for(Player p: ns.local.getAllPlayers()){
			if(p.isComputer()){
				if(dScreenController.dScreen.server.IsLoggedIn())
					b.addSlave(p, new GUIController(dScreen));
				else{
					b.addSlave(p, new TestController(ns.local));
				}
			}
		}


	}


	
	public void buttonClick(){
		synchronized(ns.local){
			if(!dScreenController.playerSelected() || ns.isDead(currentPlayer))
				return;
			if (getNarrator().isDay())
				ns.doDayAction(currentPlayer);
			else{
				if (ns.endedNight(currentPlayer)) {
					ns.cancelEndNight(currentPlayer);
				}else {
					ns.endNight(currentPlayer);
				}
			}
		}
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


	public void talk(String message) {
		synchronized(ns.local){
			ns.talk(currentPlayer, message);
		}
	}

	

	//from gui input
	//garuntee that someone is selected
	protected void command(String target){
		if (!dScreenController.playerSelected() || (getNarrator().isNight() && ns.endedNight(currentPlayer)) || ns.isDead(currentPlayer)) {
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
			boolean unvote = ns.isVoting(currentPlayer, target);
			//if owner voted for target already, gotta be an unvote
			if(unvote)
				ns.unvote(currentPlayer);
			else{
				if(target.equals("Skip Day"))
					ns.skipVote(currentPlayer);
				else
					ns.vote(currentPlayer, target);
			}
		}else {
			String ability_s = dScreenController.dScreen.getSelectedAbility();
			ns.target(currentPlayer, target, ability_s);
		}
		}
	}
	

	

	protected void setCurrentPlayer(String name){
		currentPlayer = name;
		dScreenController.currentPlayer = name;
	}
	protected void setNextAbility(int direction){
		dScreenController.setNextAbility(direction);
	}



	
	
	public Narrator getNarrator(){
		return ns.local;
	}

	public void nextSimulation(){
		if(ns.isInProgress()){
			if(ns.isDay()){
				b.dayAction();
			}else{
				b.nightAction();
			}
		}
	}

	


	public String getCurrentPlayer(){
		return currentPlayer;
	}

	


	public boolean isHost() {
		if(dScreenController.dScreen.server.IsLoggedIn())
			return false;
		return true;
	}
	
	public void text(Player p, String s, boolean sync){
		s = p.getName() + Constants.NAME_SPLIT + s;
		if(isHost()){

		}else{
			if(dScreenController.dScreen.server.IsLoggedIn()) {
				s = "," + s;
				if(!sync)
					s = dScreenController.dScreen.server.GetCurrentUserName() + s;
				else
					s = " " + s;
				double day = ns.local.getDayNumber();
				if(ns.local.isNight())
					day += 0.5;
				//Server.PushCommand(ns.getGameListing(), s, day);
			}else{
				//ns.socketClient.send(s);
			}
		}
	}

	public void parseCommand(Intent i){
		//if(!i.hasExtra(GameListing.ID))
		//	return;
		//check if someone else is pinging this
		//if(!i.getStringExtra(GameListing.ID).equals(ns.getGameListing().getID()))
		//	return;

		String message = i.getStringExtra("stuff");
		String[] command = message.split(",");

		String sender = command[0];
		message = message.substring(sender.length() + 1);//1 length for comma
		if(sender.equals(" ") || !sender.equals(dScreenController.dScreen.server.GetCurrentUserName())){//everyone should do it.
			//ns.onRead(message, null);//nulll for chat manager, unused in this function, also its synch protected.
		}
	}
}