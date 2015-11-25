package voss.shared.roles;

import voss.shared.logic.Player;

public class Escort extends Blocker {
	
	public Escort(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Escort";
	
	public String getRoleName(){
		return ROLE_NAME;
	}
	
}