package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;


public class Mafioso extends Role{
	
	public static final String ROLE_NAME = "Mafioso";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	
	public Mafioso(Player p){super(p);}
	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		noAcceptableTargets();
	}

	public int getAbilityCount(){
		return 0;
	}

	public static final String NIGHT_PROMPT = "You are a minion of the Mafia.  Collaborate on who to kill during the night, and cause havoc during the day.";
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}

	public static final String DEATH_FEEDBACK = "You were killed by the mafia!";
	public boolean doNightAction(Player owner, Narrator n) {
		return NoNightActionVisit(owner, n);
	}


	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		return list;
	}

	public double nightVotePower(PlayerList pl){
		return 1;
	}
	
	public boolean isPowerRole() {
		return false;
	}
}
