package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;


public class Citizen extends Role{
	
	public Citizen(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Citizen";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	public String getRoleInfo() {
		return "During the day you are a voice of reason.  You don't do anything at night.";
	}


	public void isAcceptableTarget(Player p, Player player, int ability) {
		noAcceptableTargets();
	}
	
	public int getAbilityCount(){
		return 0;
	}
	
	public boolean doNightAction(Player owner, Narrator n) {
		return Role.NoNightActionVisit(owner, n);
	}

	public boolean isPowerRole() {
		return false;
	}

	public ArrayList<String> getAbilities() {
		return new ArrayList<String>();
	}
}
