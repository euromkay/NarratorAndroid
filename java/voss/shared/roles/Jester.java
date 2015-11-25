package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.support.Constants;

public class Jester extends Role{

	public static final String ROLE_NAME = "Jester";
	public static final String DEATH_FEEDBACK = "You killed yourself because you voted for the jester last night!";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	
	public Jester(Player p){super(p);}

	public String getRoleInfo() {
		return "Your goal is to get yourself lynched.  Do it through any means necessary.";
	}
	
	public void isAcceptableTarget(Player owner, Player target, int action) {
		noAcceptableTargets();
	}
	public int getAbilityCount(){
		return 0;
	}
	public boolean doNightAction(Player owner, Narrator n) {
		return NoNightActionVisit(owner, n);
	}
	
	
	public boolean isWinner(Player p, Narrator n){
		if(p.getAlignment() != Constants.A_BENIGN)
			return super.isWinner(p, n);
		if(p.isAlive())
			return false;
		return p.getDeathType().isLynch();
	}
	
	

	public ArrayList<String> getAbilities() {
		return new ArrayList<String>();
	}
	
	
}
