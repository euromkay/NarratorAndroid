package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;


public class Blackmailer extends Role{
	
	public Blackmailer(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Blackmailer";
	public static final String FEEDBACK = "You were blackmailed";
	
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	public static final String NIGHT_ACTION_DESCRIPTION = "You have the ability to stop people from voting and talking.";
	public String getRoleInfo() {
		return NIGHT_ACTION_DESCRIPTION;
	}
	
	public static final int BM_ = MAIN_ABILITY;
	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		deadCheck(target);
		allowedAbilities(ability, BM_);
	}

	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		owner.visit(target);
		target.setBlackmailed(true);
		target.addNightFeedback(FEEDBACK);
		Event e = new Event();
		e.add(owner, " blackmailed ", target, ".");
		e.setPrivate();
		n.addEvent(e);
		return true;
	}

	private static final String BLACKMAIL = "blackmail";
	
	
	
	
	
	


	
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(BLACKMAIL);
		return list;
	}
	
	
	
}
