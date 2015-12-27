package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.HTString;


public class Sheriff extends Role{


	public static final String ROLE_NAME = "Sheriff";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	public Sheriff(Player p){super(p);}
	
	public static final String NIGHT_PROMPT = "You have the ability to see what team someone is on.";
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}
	
	public void isAcceptableTarget(Player owner, Player target, int action) {
		deadCheck(target);
		allowedAbilities(action, MAIN_ABILITY);
		selfCheck(owner,target);
	}
	
	public static final String NOT_SUSPICIOUS = "Your target is not suspicious.";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);;
		if(target == null)
			return false;
		owner.visit(target);
		
		Team sheriffTeam = owner.getTeam();
		
		Team targetTeam = target.getTeam();
		
		
		int status = target.getFrameStatus();
		if(status != Constants.A_NORMAL){
			targetTeam = n.getTeam(status);
		} 
		if(!target.isDetectable())
			targetTeam = sheriffTeam;
		if(!sheriffTeam.sheriffDetects(targetTeam.getAlignment()))
			targetTeam = null;
		 
		Event nightFeedback = generateFeedback(targetTeam);
		nightFeedback.setVisibility(owner);
		
		owner.addNightFeedback(nightFeedback);
		Role.event(owner, " checked ", target);
		return true;
	}
	
	public static Event generateFeedback(Team t){
		Event e = new Event().dontShowPrivate();
		if(t == null)
			return e.add(NOT_SUSPICIOUS);
		if(t.knowsTeam())
			e.add("Your target is a member of the ");
		else
			e.add("Your target is a");
		return e.add(new HTString(t.getName(), t.getAlignment()), ".");
	}
	
	private static final String COMMAND = "Check";
	

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}

}
