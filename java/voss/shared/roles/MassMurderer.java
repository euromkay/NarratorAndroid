package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;

public class MassMurderer extends Role {


	public MassMurderer(Player p) {
		super(p);
		if(p.getNarrator().getRules().mmInvulnerable)
			p.setImmune(true);
	}

	

	public static final String ROLE_NAME = "Mass Murderer";
	public String getRoleName() {
		return ROLE_NAME;
	}

	private static final String SELECTION_PROMPT = " will murder all targets at ";
	public void setAction(Player owner, Player target, int ability, boolean simulation) {
		if(ability != MAIN_ABILITY){
			owner.getTeam().getSelectionFeedback(owner, target, ability, simulation);
			return;
		}
		Event e = Role.selectionEvent(owner);
		e.setCommand(owner, COMMAND, target.getName());
		if(simulation)
			return;
		e.add(owner, SELECTION_PROMPT, target, ".");
		owner.getNarrator().addEvent(e);
			//owner.getTeam().notifyTeammates(owner, SELECTION_PROMPT + target.getName() + ".");
	}

	public String getNightText(ArrayList<Team> t){
		if (getCooldown() == 0)
			return super.getNightText(t);
		else
			return "You must wait a night to kill people again.";
	}
	
	public String getRoleInfo() {
		return "You have the ability to kill everyone who visits your night target.";
	}


	private static final String COMMAND = "Spree";
	public static final String DEATH_FEEDBACK = "You were killed by a Mass Murderer";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}

	
	public int parseAbility(String message, boolean isDay){
		if(message.equalsIgnoreCase(COMMAND) && !isDay)
			return MAIN_ABILITY;
		else
			return INVALID_ABILITY;
	}
	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		deadCheck(owner);
		allowedAbilities(ability, MAIN_ABILITY);
		if (getCooldown() > 1)
			Exception("You have to wait another night until you can murder people again.");
	}

	private int getCooldown(){
		return getInt(0);
	}
	
	private void setCoolDown(int day){
		setInt(0, day);
	}
	private void decrementCooldown(){
		setCoolDown(getCooldown() - 1);
	}
	//private int cooldown;
	
	public boolean doNightAction(Player owner, Narrator n) {
		decrementCooldown();
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		
		if(getCooldown() > 0){
			return NoNightActionVisit(owner, n);
		}
		owner.visit(target);
		
		int peopleAttacked = 0;
		
		PlayerList killed = new PlayerList();
		for(Player pQ: n.getLivePlayers()){
			if(pQ == owner)
				continue;
			if(pQ.isTargeting(target)){
				pQ.kill(Constants.MASS_MURDERER_FLAG, owner);
				owner.attack(pQ);
				pQ.visit(target);
				killed.add(pQ);
				peopleAttacked++;
			}
		}
		
		if(target.isAtHome()){
			target.kill(Constants.MASS_MURDERER_FLAG, owner);
			owner.attack(target);
			killed.add(target);
			peopleAttacked++;
		}
		
		Object add;
		if(killed.isEmpty())
			add = "no one";
		else
			add = killed;
		if(peopleAttacked > 1)
			setCoolDown(n.getRules().mmSpreeDelay + 1);
		
		Event e = new Event();
		e.add(owner, " camped out at ", target, "'s house killing ", add,  ".");
		e.setPrivate();
		n.addEvent(e);
		return true;
	}
	
	
}
