package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;

public class UnsetRole extends Role {

	public UnsetRole(Player p){super(p);}
	
	public static final String ROLE_NAME = "UnsetRole";
	
	public String getRoleName() {
		return ROLE_NAME;
	}

	public String getNightText(ArrayList<Team> t) {
		return "";
	}

	 
	public String getRoleInfo() {
		return "";
	}



	 
	public ArrayList<String> getAbilities() {
		return new ArrayList<String>();
	}

	 
	public int parseAbility(String message) {
		return Role.INVALID_ABILITY;
	}

	 
	public void isAcceptableTarget(Player owner, Player target, int ability) {
	}

	 
	public boolean doNightAction(Player owner, Narrator n) {
		return false;
	}
	public boolean isPowerRole() {
		return false;
	}
}
