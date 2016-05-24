package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;

public class Cultist extends Role {


	public Cultist(Player p) {
		super(p);
	}



	public static final String ROLE_NAME = "Cultist";
	public String getRoleName() {

		return ROLE_NAME;
	}

	public int getAbilityCount(){
		return 0;
	}

	
	public String getRoleInfo() {
		return "You are part of the cult.  Collaborate with your leader to convert someone.";
	}

	
	public ArrayList<String> getAbilities() {
		return new ArrayList<String>();
	}

	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		noAcceptableTargets();
	}

	
	public boolean doNightAction(Player owner, Narrator n) {
		return NoNightActionVisit(owner, n);
	}

	public boolean isPowerRole() {
		return false;
	}
}
