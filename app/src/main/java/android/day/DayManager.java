package android.day;

import java.util.ArrayList;
import java.util.Collections;

import android.NarratorService;
import android.PhoneBook;
import android.content.Intent;
import android.setup.TextAdder;
import android.texting.TextHandler;
import shared.ai.Brain;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.templates.TestController;
import shared.roles.Assassin;
import shared.roles.Driver;
import shared.roles.Framer;

public class DayManager{

	
	private String currentPlayer;

	
	public DayScreenController dScreenController;
	public NarratorService ns;
	public PhoneBook phoneBook;
	protected TextHandler tHandler;
	public Brain b;
	public DayManager(NarratorService ns){
		this.ns = ns;
	}
	public void initiate(ActivityDay dScreen){
		dScreenController = new DayScreenController(dScreen, this);
		dScreenController.init();
		dScreenController.setNarratorInfoView();


		tHandler = new TextHandler(ns.local, TextAdder.getTexters(ns.local.getAllPlayers()));
		
		phoneBook = new PhoneBook(ns.local);


		PlayerList computers = new PlayerList();
		for(Player p: ns.local.getAllPlayers()){
			if(p.isComputer()){
				computers.add(p);
			}
		}

		b = new Brain(computers, ns.local.getRandom(), new TestController(ns.local));
		b.setNarrator(ns.local);

	}


	
	public void buttonClick(){
		synchronized(ns.local){
			if(!dScreenController.playerSelected() || ns.isDead(currentPlayer))
				return;
			if (ns.isDay()) {
				int checkedPosition = dScreenController.dScreen.actionLV.getCheckedItemPosition();
				String target;
				if(checkedPosition != -1)
					target = dScreenController.dScreen.actionList.get(checkedPosition);
				else
					target = null;
				ns.doDayAction(currentPlayer, target);
			}else{
				if (ns.endedNight(currentPlayer)) {
					ns.cancelEndNight(currentPlayer);
					dScreenController.setSkipNightText();
				}else {
					ns.endNight(currentPlayer);
					dScreenController.setCancelSkipNightText();
				}
			}
		}
	}



	public void talk(String message) {
		synchronized(ns.local){
			ns.talk(currentPlayer, message);
		}
	}

	

	//from gui input
	//garuntee that someone is selected
	protected void command(boolean targeting, String ... target){
		ArrayList<String> targets = new ArrayList<>();
		for(String s: target)
			targets.add(s);
		command(targeting, targets);
	}
	protected void command(boolean targeting, ArrayList<String> targets){
		if (!dScreenController.playerSelected() || (!ns.isDay() && ns.endedNight(currentPlayer)) || ns.isDead(currentPlayer)) {
			dScreenController.updateActionPanel();
			return;
		}
		
		if (targets == null){
			//probably just frame being set
			return;
		}
		
		if(getCommand().equals(Driver.COMMAND)){
			Collections.sort(targets);
		}
			
		
		if(ns.isDay()){
			String command = dScreenController.dScreen.commandTV.getText().toString(); 
			String target = targets.get(0);
			if(command.contains(" ")){
				boolean unvote = ns.isVoting(currentPlayer, target);
				//if owner voted for target already, gotta be an unvote
				if(unvote)
					ns.unvote(currentPlayer);
				else{
					if(target.equalsIgnoreCase("Skip Day"))
						ns.skipVote(currentPlayer);
					else
						ns.vote(currentPlayer, target);
				}
				return;
			}else if(command.equalsIgnoreCase(Assassin.ASSASSINATE)){
				ArrayList<String> actionList = dScreenController.dScreen.actionList;
				for(int i = 0; i < actionList.size(); i++){
					if(actionList.get(i).equals(target)){
						dScreenController.dScreen.actionLV.setItemChecked(i, true);
					}else{
						dScreenController.dScreen.actionLV.setItemChecked(i, true);
					}
				}
				return;
			}
		}else {
			String ability_s = dScreenController.dScreen.getSelectedAbility();
			String option;
			if(ability_s.equalsIgnoreCase(Framer.FRAME))
				option = dScreenController.dScreen.framerSpinner.getSelectedItem().toString();
			else
				option = null;
			ns.target(currentPlayer, targets, ability_s, option, targeting);
		}
		
	}
	

	

	protected void setCurrentPlayer(String name){
		currentPlayer = name;
		dScreenController.currentPlayer = name;
		dScreenController.dScreen.targetablesAdapter = null;
	}
	protected void setNextAbility(int direction){
		dScreenController.setNextAbility(direction);
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

	public String getCommand(){
		return dScreenController.dScreen.commandTV.getText().toString();
	}


	public boolean isHost() {
		if(dScreenController.dScreen.ns.server.IsLoggedIn())
			return false;
		return true;
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
		if(sender.equals(" ") || !sender.equals(dScreenController.dScreen.ns.server.GetCurrentUserName())){//everyone should do it.
			//ns.onRead(message, null);//nulll for chat manager, unused in this function, also its synch protected.
		}
	}
}