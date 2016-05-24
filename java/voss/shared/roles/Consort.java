package voss.shared.roles;

import voss.shared.logic.Player;

public class Consort extends Blocker {
	
	public Consort(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Consort";
	
	public String getRoleName(){
		return ROLE_NAME;
	}
	
}