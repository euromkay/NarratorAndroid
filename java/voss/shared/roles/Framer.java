package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;

public class Framer extends Role {


	public Framer(Player p) {
		super(p);
	}

	public static final String ROLE_NAME = "Framer";
	public String getRoleName() {
		return ROLE_NAME;
	}


	public String getNightText(ArrayList<Team> allTeams) {
		String teams = "";
		for(Team t: allTeams){
			teams += t.getName()+", ";
		}
		return "Type " + SQuote("frame name team") + " to frame a target." +
		       "These are the list of teams: " + teams.substring(0, teams.length()-2).replace(" ", "");
	}


	public String getRoleInfo() {
		return "You have the ability to change how people look to sheriffs for the night.";
	}
	
	public static final String FRAME = "Frame";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(FRAME);
		return list;
	}


	public void setAction(Player owner, Player target, int ability){
		Team t = owner.getTeam();
		if(getAbilityCount() == 0 || ability != MAIN_ABILITY)
			t.getSelectionFeedback(owner, target, ability);
		else if(target.equals(owner.getSkipper())){
			//t.notifyTeammates(owner, " won't " + getKeyWord() + " anyone.");
		}else {
			Team parse = owner.getNarrator().getTeam(getAlignment());
			Event e = Role.selectionEvent(owner);
			e.add(owner, " will " + getKeyWord().toLowerCase() + " ", target);
			if (parse != null){
				e.setCommand(owner, getKeyWord(), target.getName(), parse.getName());
				e.add(" as ", parse.getName());
			}
			e.add(".");
			owner.getNarrator().addEvent(e);

		}
	}


	public static final int FRAME_ = MAIN_ABILITY;
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		deadCheck(target);
		allowedAbilities(ability, MAIN_ABILITY);
		selfCheck(owner, target);
	}

	//private int alignment;
	public void setFrame(int alignment) {
		setInt(0, alignment);
	}

	public boolean doNightAction(Player owner, Narrator n) {
		Player target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		target.setFramed(getAlignment());
		owner.visit(target);
		Event e = new Event();
		e.setPrivate();
		e.add(owner, " framed ", target, " as an ", n.getTeam(getAlignment()).getName());
		n.addEvent(e);
		return true;
	}
	
	public int getAlignment(){
		return getInt(0);
	}

	
}
