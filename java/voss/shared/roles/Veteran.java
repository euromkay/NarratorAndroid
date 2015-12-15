package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;

public class Veteran extends Role {

	public Veteran(Player p) {
		super(p);
		setAlerts(p.getNarrator().getRules().vetAlerts);
	}

	//private int alerts;
	
	private void setAlerts(int i){
		setInt(0, i);
	}
	
	public static final String ROLE_NAME = "Veteran";
	public String getRoleName() {
		return ROLE_NAME;
	}

	public String getNightText(Narrator n) {
		return "Type " + NQuote(getKeyWord()) + " to kill all who visit you tonight";
	}

	
	private static final String NIGHT_PROMPT = "You have the ability to kill all who visit you when you are on alert.";public String getRoleInfo(){
		return NIGHT_PROMPT;
	}

	private int getAlerts(){
		return getInt(0);
	}
	
	
	public String getNightText(ArrayList<Team> teams) {
		int alerts = getAlerts();
		if(alerts == 0)
			return "You cannot go on alert anymore";
		else{
			String message  = "Type " + SQuote(ALERT) + " to go on alert tonight";
			if(alerts <= -1)
				message += "You can go on alert " + alerts + " more times.";
			return message;
		}
	}

	public static final String ALERT = "alert";


	public static final String DEATH_FEEDBACK = "You were killed by a Serial Killer!";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		if (getAlerts() > 0)
			list.add(ALERT);
		return list;
	}

	public void setAction(Player owner, Player target, int ability) {
		if(ability == MAIN_ABILITY){
			Event e = Role.selectionEvent(owner);
			e.setCommand(owner, ALERT, owner.getName());

			e.add(owner, " will go on alert.");
			owner.getNarrator().addEvent(e);
			//owner.getTeam().notifyTeammates(owner, " will go on alert.");
		}else
			owner.getTeam().getSelectionFeedback(owner, target, ability);
		
	}
	
	public void dayReset(Player p){
		p.setImmune(false);
	}

	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		allowedAbilities(ability, MAIN_ABILITY);
		if(getAlerts() == 0)
			Exception("You have no more alerts");
		if(target != owner || target == null)
			Exception("You can't target someone when going on alert");
	}

	
	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		//witched
		if(target != owner && getAlerts() == 0)
			return Role.NoNightActionVisit(owner, n);
		owner.setImmune(true);
		PlayerList people = new PlayerList();
		for(Player pQ: n.getLivePlayers()){
			if(pQ == owner)
				continue;
			if(pQ.isTargeting(owner)){
				pQ.kill(Constants.VETERAN_KILL_FLAG, owner);
				owner.attack(pQ);
				pQ.visit(owner);
				people.add(pQ);
			}
		}
		decrementAlerts();
		Object add;
		if(people.isEmpty())
			add = "no one.";
		else
			add = people;
		
		Event e = new Event();
		e.add(owner, " went on alert, killing ", add);
		e.setPrivate();
		n.addEvent(e);
		return true;
	}
	
	private void decrementAlerts(){
		setAlerts(getAlerts() - 1);
	}
	
	
	
	public static boolean isImmuneVet(Player owner){
		if(!owner.getRoleName().equals(ROLE_NAME))
			return false;
		Player target = owner.getTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		
		Veteran v = (Veteran) owner.getRole();
		if(v.getAlerts() != 0)
			return true;
		return false;
	}

}
