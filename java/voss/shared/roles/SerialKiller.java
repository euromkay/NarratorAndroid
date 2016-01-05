package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.support.Constants;


public class SerialKiller extends Role {

	public static final String ROLE_NAME = "Serial Killer";
	
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	public SerialKiller(Player p){
		super(p);
		if(p.getNarrator().getRules().serialKillerIsInvulnerable)
			p.setImmune(true);
	}


	public String getRoleInfo(){
		return "You are a crazed psychopath trying to kill everyone. Do it.";
	}

	
	
	public void isAcceptableTarget(Player owner, Player target, int action) {
		allowedAbilities(action, MAIN_ABILITY);
		deadCheck(target);
	}
	public static final String STAB = "Stab";
	public int parseAbility(String message, boolean isDay){
		if(message.equalsIgnoreCase(STAB) && !isDay)
			return MAIN_ABILITY;
		else
			return INVALID_ABILITY;
	}

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(STAB);
		return list;
	}
	

	
	public static final String DEATH_FEEDBACK = "You were killed by a Serial Killer!";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		Kill(owner, target, Constants.SK_KILL_FLAG, n);//visiting taken care of already in kill
		return true;
	}
	
}
