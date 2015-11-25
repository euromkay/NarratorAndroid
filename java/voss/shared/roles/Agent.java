package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;

public class Agent extends Role {

	public Agent(Player p) {
		super(p);
	}


	public static final String ROLE_NAME = "Agent";
	public String getRoleName() {
		return ROLE_NAME;
	}

	
	private static int STALK_ = MAIN_ABILITY;
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(STALK);
		return list;
	}
	public static final String NIGHT_ACTION_DESCRIPTION = "You can stalk someone to find out who they visited and who visited them.";
	public String getRoleInfo(){
		return NIGHT_ACTION_DESCRIPTION;
	}
	public static final String STALK = "stalk";
	public String getKeyWord(){
		return STALK;
	}

	public static final String FEEDBACK = "These are the people who visited your target: ";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		if(getVisited()){
			Detective.follow(owner, target, n);
			Lookout.watch(owner, target, n);
			Role.event(owner, " stalkd ", target);
			return true;
		}
		else{
			setVisited(true);
			owner.visit(target);
			return false;
		}
		
		
	}
	public void dayReset(Player p){
		setVisited(false);
	}
	
	private void setVisited(boolean b){
		setBool(0, b);
	}
	
	private boolean getVisited(){
		return getBool(0);
	}


	public void isAcceptableTarget(Player owner, Player target, int action) {
		deadCheck(target);
		selfCheck(owner, target);
		allowedAbilities(action, STALK_);
	}

	

	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;

		
		return super.equals(o);
	}
	
}
