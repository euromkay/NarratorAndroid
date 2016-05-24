package voss.shared.roles;

import voss.shared.logic.Player;

public class Chauffeur extends Driver {
	public Chauffeur(Player p) {
		super(p);
	}
	public static final String ROLE_NAME = "Chauffeur";
	public String getRoleName(){
		return ROLE_NAME;
	}
}
