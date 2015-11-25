package voss.shared.roles;

import voss.shared.logic.Player;

public class BusDriver extends Driver {
	public BusDriver(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Bus Driver";
	
	public String getRoleName(){
		return ROLE_NAME;
	}
	
}
