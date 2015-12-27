package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.support.Constants;


public class Doctor extends Role {

	public Doctor(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Doctor";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	public static final String NIGHT_PROMPT = "You have the ability to save someone from an attack.";
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}
	
	public void isAcceptableTarget(Player owner, Player target, int action) {
		deadCheck(target);
		allowedAbilities(action, MAIN_ABILITY);
		selfCheck(owner,target);
	}

	
	//private boolean targetHealed;
	
	public static final String SUCCESFULL_HEAL = "Your target was attacked tonight!";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		owner.visit(target);
		//this is only triggered if the lives are less than 0
		if(target.getLives() < Narrator.NORMAL_HEALTH){
			setHealed(true);
			if(n.getRules().doctorKnowsIfTargetIsAttacked)
				owner.addNightFeedback(Event.StringFeedback(SUCCESFULL_HEAL, owner));
		}
		
		//but the person is healed either way
		target.heal(Constants.DOCTOR_HEAL_FLAG);
		Role.event(owner, " healed ", target);
		return true;
	}
	
	public void dayReset(Player p){
		setHealed(false);
	}
	
	public void setHealed(boolean b){
		setBool(0, b);
	}
	
	public boolean getHealed(){
		return getBool(0);
	}
	
	
	
	
	
	
	
	private static final String COMMAND = "Heal";
	
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}
	
}
