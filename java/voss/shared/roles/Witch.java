package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.event.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.support.StringChoice;

public class Witch extends Role {

	public static final String ROLE_NAME = "Witch";
	public String getRoleName() {
		return ROLE_NAME;
	}
	
	public Witch(Player p){
		super(p);
	}

	public int getAbilityCount(){
		return 2;
	}

	public String getNightText(ArrayList<Team> t) {
		return "Type \'control (name)\' to choose your victim and type target \'name\' to change their target";
	}

	public String getRoleInfo() {
		return "You have the ability to change someone else's action target.";
	}

	public void setAction(Player owner, Player target, int ability) {
		if (ability != VICTIM & ability != VICTIM_TARGET){
			owner.getTeam().getSelectionFeedback(owner, target, ability);
			return;
		}
		Event e = new Event();
		StringChoice sc = new StringChoice(owner);
		sc.add(owner, "You");
		e.add(sc);
		
		
			
			
		
		Player victim = owner.getTarget(VICTIM);
		Player newTarget = owner.getTarget(VICTIM_TARGET);
		//String message;
		
		if(victim != null && newTarget != null){
			e.add(" will make ", victim, " target ", newTarget);

		}else if(victim == null){
			e.add(" will make the victim target ", newTarget);
			e.dontShowPrivate();
		}else{//(newTarget == null )
			e.add(" will controll ", victim);
			e.dontShowPrivate();
		}
		if(ability == VICTIM)
			e.setCommand(owner, Control, target.getName());
		else
			e.setCommand(owner, Target, target.getName());
		e.add(".");
		
		Event.AddSelectionFeedback(e, owner);
		 //owner.getTeam().notifyTeammates(owner, message);
	}


	public static final String WITCH_FEEDBACK = "You were witched!";
	public void isAcceptableTarget(Player owner, Player target, int action) {
		deadCheck(target);
		allowedAbilities(action, VICTIM, VICTIM_TARGET);
		if(action == VICTIM && target == owner)
			Exception("You can't control yourself.");
	}

	public boolean doNightAction(Player witch, Narrator n) {
		Player victim = witch.getHouseTarget(VICTIM);
		if(victim == null)
			return false;
		Player newTarget = witch.getTarget(VICTIM_TARGET);//bus drivers don't affect intended targets
		if(newTarget == null){
			witch.overrideTarget(victim, MAIN_ABILITY);
			NoNightActionVisit(witch, n);
			return true;
		}
		
		boolean actionChanged = false;
		if(victim.getTeam().getSender() == victim && !completedMainAction(victim)){
			victim.overrideTarget(newTarget, Team.KILL_);
			actionChanged = true;
		}
		if(!actionChanged){
			if(victim.getAbilityCount() == 3 && victim.getTarget(MAIN_ABILITY) == null && victim.getTarget(Role.SECONDARY_ABILITY) == null)
				victim.overrideTarget(newTarget, Role.TERTIARY_ABILITY);
			else
				victim.overrideTarget(newTarget, MAIN_ABILITY);
		}
		witch.visit(victim);
		if(n.getRules().witchLeavesFeedback)
			victim.addNightFeedback(Event.StringFeedback(WITCH_FEEDBACK, victim));
		
		Event e = new Event();
		e.add(witch, " changed ", victim, "\'s target to ", newTarget, ".");
		e.setPrivate();
		n.getEventManager().getNightLog(null, n.getDayNumber()).add(e);
		return true;
	}
	private boolean completedMainAction(Player victim){
		if(victim.getAbilityCount() == 1)
			return victim.getTarget(MAIN_ABILITY) != null;
		if(victim.getAbilityCount() == 2)
			return (victim.getTarget(MAIN_ABILITY) != null && victim.getTarget(SECONDARY_ABILITY) != null);
		return false;	
			
	}

	public static final int VICTIM = SECONDARY_ABILITY;
	public static final int VICTIM_TARGET = MAIN_ABILITY; 
	public static final String Control = "Control";
	public static final String Target = "Target";
	public int parseAbility(String message, boolean isDay) {
		if(!isDay){
		if(message.equalsIgnoreCase(Control))
			return VICTIM;
		if(message.equalsIgnoreCase(Target))
			return VICTIM_TARGET;
		}
		return INVALID_ABILITY;
	}

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(Target);
		list.add(Control);
		return list;
	}
	
	public boolean isTargeting(Player owner, Player target) {
		if(owner.getTarget(VICTIM) == target)
			return true;
		return false;
	}
}