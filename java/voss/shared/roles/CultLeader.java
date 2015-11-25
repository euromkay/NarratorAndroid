package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Rules;
import voss.shared.logic.Team;

public class CultLeader extends Role{

	
	public CultLeader(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Cult Leader";
	public String getRoleName() {
		return ROLE_NAME;
	}

	private void setConvCooldown(int i){
		setInt(0, i);
	}
	
	private int getConvCooldown(){
		return getInt(0);
	}
	
	private int getPowerCD(){
		return getInt(1);
	}
	
	public String getRoleInfo(){
		return "You can recruit anyone else into the cult. Expand until you are one with all.";
	}
	
	private void setPowerCD(int i){
		setInt(1, i);
	}

	public String getNightText(ArrayList<Team> t) {
		int remaining = getConvCooldown();
		int powerRemain = getPowerCD();
		
		int mes = Math.max(remaining, powerRemain);
		
		if((remaining <= 0) && (powerRemain <= 0))
			return "Type + " + NQuote(COMMAND)+ "  to recruit a target.";
		
		else
			return "You must wait " + mes + " more nights before you can convert again.";
		
	}

	
	public String getNightPrompt() {
		return "You have the ability to convert somoene to your team.";
	}

	private static final int RECRUIT = MAIN_ABILITY;
	private static final String COMMAND = "Recruit";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}
	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		deadCheck(target);
		selfCheck(owner, target);
		allowedAbilities(ability, RECRUIT);
		
		if(getConvCooldown() > 0)
			Exception("You must wait " + getConvCooldown() + " more days to convert again.");
		
		if(getPowerCD() > 0)
			Exception("You must wait " + getPowerCD() + " more days to convert again.");
	}

	
	public boolean doNightAction(Player owner, Narrator n) {
		Rules r = n.getRules();
		Player target = owner.getHouseTarget(RECRUIT);
		
		if(target == null)
			return false;
		
		owner.visit(target);
		if(target.getTeam().canRecruitFrom()){
			if(getConvCooldown() <= 0 && getPowerCD() <= 0){
				if(r.cultKeepsRoles && target.isPowerRole())
					setPowerCD(r.cultPowerRoleCooldown + 1);
				
				//reseting cooldown since its a successful cooldown
				setConvCooldown(r.cultConversionCooldown + 1);
				
				
				
				target.setTeam(owner.getTeam());
				owner.getTeam().addMember(target);
				if(!n.getRules().cultKeepsRoles)
					target.changeRole(new Cultist(target));
			
				Event e = new Event();
				e.setVisibility(target);
				e.add(target, " converted to the " + owner.getTeam().getName() + ". Your teammates are : ", owner.getTeam().getMembers().getNamesToStringArray(), ".");
				n.addEvent(e);
				
				e = new Event();
				e.setPrivate();
				e.add(owner, " recruited ", target, " to the " + owner.getTeam().getName());
				n.addEvent(e);
			}
		}
		return true;
	}
	
	public void dayReset(Player p){
		if(getConvCooldown() > 0)
			setConvCooldown(getConvCooldown() - 1);
		if(getPowerCD() > 0)
			setPowerCD(getPowerCD() - 1);
	}
}
