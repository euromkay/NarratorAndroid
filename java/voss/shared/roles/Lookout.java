package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.event.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;

public class Lookout extends Role {

	public static final String ROLE_NAME = "Lookout";
	public Lookout(Player p){super(p);}

	public String getRoleName() {
		return ROLE_NAME;
	}


	public static final String NIGHT_PROMPT = "You have the ability to find out all who visit someone.";
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}

	//private boolean visited = false;
	public static final String FEEDBACK = "These are the people who visited your target: ";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		if(getVisited()){
			watch(owner, target, n);
			Role.event(owner, " watched ", target);
			return true;
		}
		else{
			setVisited(true);
			owner.visit(target);
			return false;
		}
		
		
	}
	
	private boolean getVisited(){
		return getBool(0);
	}
	
	private void setVisited(boolean b){
		setBool(0, b);
	}
	public static final String NO_VISIT = "No one visited your target";
	public static void watch(Player owner, Player target, Narrator n){
		PlayerList visitors = target.getVisitors();
		visitors.remove(owner);
		visitors.sortByName();
		if(visitors.isEmpty())
			owner.addNightFeedback(Event.StringFeedback(NO_VISIT, owner));
		else{
			Event e = Event.StringFeedback(FEEDBACK, owner);
			for(Player p: visitors){
				e.add(p);
				if(visitors.getLast() != p)
					e.add(", ");
			}
			owner.addNightFeedback(e);
		}
	}
	public void dayReset(Player p){
		setVisited(false);
	}

	private static final String COMMAND = "Watch";

	public void isAcceptableTarget(Player owner, Player target, int action) {
		allowedAbilities(action, MAIN_ABILITY);
		deadCheck(target);
	}

	
}
