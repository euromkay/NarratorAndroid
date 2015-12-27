package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;

public class Detective extends Role {

	public Detective(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Detective";
	public String getRoleName() {
		return ROLE_NAME;
	}

	public static final String NIGHT_PROMPT = "You have the ability to find out who someone visits.";
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}

	//private boolean visited = false;
	public static final String FEEDBACK = "Your target visited: ";
	public static final String NO_VISIT = "Your target didn't visit anyone";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		if(getVisited()){
			follow(owner, target, n);
			Role.event(owner, " followed ", target);
			return true;
		}else{
			setVisited(true);
			owner.visit(target);
			return false;
		}
			
	}
	public void dayReset(){
		setVisited(false);
	}
	private void setVisited(boolean b){
		setBool(0, b);
	}
	private boolean getVisited(){
		return getBool(0);
	}
	
	public static void follow(Player owner, Player target, Narrator n){
		PlayerList visits = target.getVisits();
		visits.sortByName();
		if(visits.isEmpty())
			owner.addNightFeedback(Event.StringFeedback(NO_VISIT, owner));
		else{
			Event e = Event.StringFeedback(FEEDBACK, owner);
			for(Player p: visits){
				e.add(p);
				if(visits.getLast() != p)
					e.add(", ");
			}
			owner.addNightFeedback(e);
		}
		
		
	}

	public void isAcceptableTarget(Player owner, Player target, int action) {
		deadCheck(target);
		allowedAbilities(action, MAIN_ABILITY);
		selfCheck(owner,target);
	}

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add("Follow");
		return list;
	}

	
	
}
