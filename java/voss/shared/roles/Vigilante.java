package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;


public class Vigilante extends Role {

	public static final String ROLE_NAME = "Vigilante";
	
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	public Vigilante(Player p){
		super(p);
		setShots(p.getNarrator().getRules().vigilanteShots);
	}
	

	public static final String NIGHT_PROMPT = "You have the ability to kill people at night.";
	public String getNightText(ArrayList<Team> t) {
		if(getShots() == 0)
			return "You have no more shots to make";
		else{
			String message  = "Type " + NQuote(COMMAND) + " to go on kill this person";
			if(getShots() > 0)
				message += "You have " + getShots() + " bullets left.";
			return message;
		}
	}
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}
	
	public void isAcceptableTarget(Player owner, Player target, int action) {
		deadCheck(target);
		allowedAbilities(action, MAIN_ABILITY);
		selfCheck(target, owner);
	}
	private static final String COMMAND = "Shoot";
	public int parseAbility(String message){
		if(message.equalsIgnoreCase(COMMAND) && getShots() != 0)
			return MAIN_ABILITY;
		else
			return INVALID_ABILITY;
	}

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}

	
	public static final String DEATH_FEEDBACK = "You were killed by a Vigilante!";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		if(getShots() != 0){
			Kill(owner, target, Constants.VIGILANTE_KILL_FLAG, n);//visiting taken care of already in kill
			decrementShots();
			return true;
		}
		return Role.NoNightActionVisit(owner, n);
	}
	
	private int getShots(){
		return getInt(0);
	}
	
	private void decrementShots(){
		setShots(getShots() - 1);
	}
	
	private void setShots(int i){
		setInt(0, i);
	}
	
	
}
