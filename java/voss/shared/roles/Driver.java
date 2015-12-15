package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;

public abstract class Driver extends Role{
	

	public Driver(Player p) {
		super(p);
	}

	private static final String TEXT1 = "swap1";
	private static final String TEXT2 = "swap2";
	
	public String getNightText(ArrayList<Team> t) {
		return "Type "+NQuote(TEXT1)+" to pick your first target or "+NQuote(TEXT2)+" to pick up the second.  These must be done seperately." ;
	}


	public String getRoleInfo() {
		return "You can pick up any two people to drive around.  Any action that affects one will instead affect the other.";
	}

	public static String TARGET1 = "Pickup 1";
	public static String TARGET2 = "Pickup 2";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(TEXT1);
		list.add(TEXT2);
		return list;
	}

	
	public int parseAbility(String message) {
		if(message.equalsIgnoreCase(TARGET1) || message.equalsIgnoreCase(TEXT1))
			return SECONDARY_ABILITY;
		if(message.equalsIgnoreCase(TARGET2) || message.equalsIgnoreCase(TEXT2))
			return MAIN_ABILITY;
		return INVALID_ABILITY;
	}


	public int getAbilityCount(){
		return 2;
	}

	public void isAcceptableTarget(Player owner, Player target, int ability) {
		deadCheck(target);
		allowedAbilities(ability, MAIN_ABILITY, SECONDARY_ABILITY);
		if(ability == MAIN_ABILITY && owner.getTarget(SECONDARY_ABILITY) == target){
			driverException();
		}
		if(ability == SECONDARY_ABILITY && owner.getTarget(MAIN_ABILITY) == target){
			driverException();
		}
	}

	private void driverException(){
		Exception("You're already picking this person up.");
	}



	public boolean isTargeting(Player owner, Player target) {
		if(owner.getHouseTarget(MAIN_ABILITY) == target)
			return true;
		if(owner.getHouseTarget(SECONDARY_ABILITY) == target)
			return true;
		return false;
	}

	public void setAction(Player owner, Player target, int ability) {
		Team t = owner.getTeam();
		if(ability != MAIN_ABILITY && ability != SECONDARY_ABILITY){
			t.getSelectionFeedback(owner, target, ability);
			return;
		}
		Player t1 = owner.getTarget(MAIN_ABILITY);
		Player t2 = owner.getTarget(SECONDARY_ABILITY);
		//swaps
		if(t1 == null){
			Player temp;
			temp = t1;
			t1 = t2;
			t2 = temp;
		}
		Event e = Role.selectionEvent(owner);
		e.add(owner);
		
		if(ability == MAIN_ABILITY)
			e.setCommand(owner, TEXT2, target.getName());
		else
			e.setCommand(owner, TEXT1, target.getName());
		
		if(t1 == null){
			t.getSelectionFeedback(owner, target, ability);
			return;
		}
		
		if(t2 == null)
			e.add(" will pick up ", t1, ".");
		else
			e.add(" will switch ", t1, " and ", t2, ".");
		
		
		owner.getNarrator().addEvent(e);
	}

	public static final String FEEDBACK = "You were bus driven";
	public boolean doNightAction(Player bd, Narrator n) {
		//these are the intended targets. making sure they aren't null
		Player t1 = bd.getTarget(MAIN_ABILITY);
		Player t2 = bd.getTarget(SECONDARY_ABILITY);
		if(t1 == null || t2 == null)
			return false;
		
		if(t1 == t2)
			return Role.NoNightActionVisit(bd, n);
		
		Player address1 = t1.getAddress();
		Player address2 = t2.getAddress();
		
		t1.setHouse(address2);
		t2.setHouse(address1);//feedback here
		
		bd.visit(t1);
		bd.visit(t2);
		
		Event e = new Event();
		e.add(bd, " switched ", t1, " and ", t2, ".");
		e.setPrivate();
		n.addEvent(e);
		
		
		return true;
	}

	
	
	
	
}
