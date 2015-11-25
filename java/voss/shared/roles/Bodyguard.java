package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.support.Constants;

public class Bodyguard extends Role {

	

	public Bodyguard(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Bodyguard";
	
	public String getRoleName() {
		return ROLE_NAME;
	}

	public String getRoleInfo() {
		return "You guard people from death.  If they are attacked, you will kill the attacker but also die in the process.";
	}

	private static final String COMMAND = "Guard";
	public static final String SAVING_FEEDBACK = "You were saved by a bodyguard!";
	public static final String DEATH_TARGET_FEEDBACK = "You were killed by a bodyguard that was protecting your target";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(COMMAND);
		return list;
	}
	
	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		allowedAbilities(ability, MAIN_ABILITY);
		deadCheck(target);
		selfCheck(owner, target);
	}

	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		owner.visit(target);
		if(!getProtected()){
			Role.event(owner, " protected ", target);
			setProtected(false);
		}
		
		
		PlayerList attackedBy = target.getAttackedByList();
		if(attackedBy.isEmpty())
			return false;
		Player attacker = attackedBy.removeLast(0);
		//int attackType = Constants.NOT_ATTACKED;
		
		for(Player p: attacker.getAttackList())
			p.heal(Constants.BODYGUARD_KILL_FLAG);
		owner.kill(Constants.BODYGUARD_KILL_FLAG, owner);
		attacker.kill(Constants.BODYGUARD_KILL_FLAG, owner);
		attacker.attack(owner);
		
		return true;
	}
	
	private boolean getProtected(){
		return getBool(0);
	}
	private void setProtected(boolean b){
		setBool(0, b);
	}

	
	
	

}
