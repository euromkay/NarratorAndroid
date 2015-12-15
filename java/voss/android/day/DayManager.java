package voss.android.day;

import android.content.Intent;

import java.util.Random;

import voss.android.NarratorService;
import voss.android.PhoneBook;
import voss.android.parse.Server;
import voss.android.texting.TextController;
import voss.android.texting.TextHandler;
import voss.android.texting.TextInput;
import voss.shared.ai.Simulations;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;
import voss.shared.logic.templates.TestController;
import voss.shared.roles.Framer;

public class DayManager implements TextInput{

	
	private   Player currentPlayer;

	
	protected DayScreenController dScreenController;
	private TextController tC;
	public NarratorService ns;
	public PhoneBook phoneBook;
	protected TextHandler tHandler;
	public DayManager(NarratorService ns){
		this.ns = ns;
	}
	public void initiate(ActivityDay dScreen){
		dScreenController = new DayScreenController(dScreen, this);
		dScreenController.setNarratorInfoView();
		ns.local.addListener(dScreenController);
		
		tHandler = new TextHandler(ns.local, this);
		
		phoneBook = new PhoneBook(ns.local);

		simulations = new Simulations(new TestController(ns.local), new Random(), ns.local);
		

		tC = new TextController(this);
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
			owner.setTarget(target, ability, t.getAlignment());
			
			tC.setNightTarget(owner, target, ability_s, t.getName());
		}else{
			owner.setTarget(target, ability);
			
			tC.setNightTarget(owner, target, ability_s);
		}

	}

	public void untarget(Player owner, Player target, String ability_s) {
		owner.removeTarget(owner.parseAbility(ability_s), true);
		
		tC.removeNightTarget(owner, ability_s);
	}


	public void talk(Player p, String message) {
		synchronized(ns.local){
			p.say(message);
		tC.say(p, message);
		
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
			Player prev = currentPlayer.getTarget(ability);

			//untargeting someone
			if (target == prev)
				untarget(currentPlayer, prev, ability_s);
			else {
				target(currentPlayer, target, ability_s);
			}
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
		simulations.next();
	}

	


	public Player getCurrentPlayer(){
		return currentPlayer;
	}

	


	public boolean isHost() {
		return ns.socketHost != null;
	}
	
	public void text(Player p, String s, boolean sync){
		s = p.getName() + Constants.NAME_SPLIT + s;
		if(isHost()){
			ns.socketHost.write(s);
		}else{
			if(Server.IsLoggedIn()) {
				s = "," + s;
				if(!sync)
					s = Server.GetCurrentUserName() + s;
				else
					s = " " + s;
				Server.PushCommand(ns.getGameListing(), s);
			}else
				ns.socketClient.send(s);

		}
	}

	public void parseCommand(Intent i){
		String message = i.getStringExtra("stuff");
		String[] command = message.split(",");

		String sender = command[0];
		message = message.substring(sender.length() + 1);//1 length for comma
		if(sender.equals(" ") || !sender.equals(Server.GetCurrentUserName())){//everyone should do it.
			ns.onRead(message, null);//nulll for chat manager, unused in this function, also its synch protected.
		}
	}
}