package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;

public abstract class Blocker extends Role {

	public Blocker(Player p) {
		super(p);
	}

	public String getRoleInfo() {
		return "You entertain at night. They will not be able to complete their night actions.";
	}


	private static String COMMAND = "Block";
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

	private static final boolean DONT_NOTIFY_TEAMMATES = false;
	public static final String FEEDBACK = "An attractive visitor occupied you're night.  You were unable to do anything else.";
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		String roleName = target.getRoleName();
		boolean isBlocker = roleName.equals(Consort.ROLE_NAME) || roleName.equals(Escort.ROLE_NAME);
		for(int ability = 0; ability < Player.MAX_TARGET_NUMBER; ability++){
			//dont change senders
			if(ability == Team.SEND_)
				continue;
			
			//non main abilities are also roleblocked, for example gun shootings
			if(ability != MAIN_ABILITY){
				target.removeTarget(ability, DONT_NOTIFY_TEAMMATES);
			}else if(Veteran.isImmuneVet(target))
				continue;
			else if(!isBlocker || n.getRules().blockersCanBeBlocked){
				target.removeTarget(ability, DONT_NOTIFY_TEAMMATES);
			}
		}

		target.getBlocked(owner);//visiting taken care of here
		owner.visit(target);
		Event e = new Event();
		e.add(owner, " blocked ", target, ".");
		e.setPrivate();
		n.addEvent(e);
		
		return true;
	}

}
