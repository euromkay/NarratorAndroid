package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;


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
		
		String nightFeedback;
		int status = target.getFrameStatus();
		if(status != Constants.A_NORMAL){
			nightFeedback = generateFeedback(n.getTeam(status));
		} else { 
			if(!target.isDetectable())
				nightFeedback = generateFeedback(null);
			else if(sheriffTeam.sheriffDetects(target.getAlignment()))
				nightFeedback = generateFeedback(target.getTeam());
			else
				nightFeedback = generateFeedback(null);
		}
		 
		owner.addNightFeedback(nightFeedback);
		Role.event(owner, " checked ", target);
		return true;
	}
	
	public static String generateFeedback(Team t){
		if(t == null)
			return NOT_SUSPICIOUS;
		return "Your target is a member of the " + t.getName();
	}
	
	private static final String COMMAND = "Check";
	

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}

}
