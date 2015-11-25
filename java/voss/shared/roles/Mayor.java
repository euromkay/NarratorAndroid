package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.Constants;

public class Mayor extends Role {

	//private int voteCount;
	public Mayor(Player p) {
		super(p);
		setVoteCount(p.getNarrator().getRules().mayorVoteCount);
	}

	private void setVoteCount(int count){
		setInt(0, count);
	}

	public static final String ROLE_NAME = "Mayor";
	public String getRoleName() {
		return ROLE_NAME;
	}

	public String getNightText(Narrator n) {
		return Constants.NO_NIGHT_ACTION;
	}

	public String getRoleInfo(){
		return "You are the leader of the town!  At any point during the day, you can reveal yourself and gain extra votes.";
	}

	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		return list;
	}


	public static final String REVEAL = "reveal";

	public int getAbilityCount(){
		return 0;
	}

	public void isAcceptableTarget(Player p, Player player, int ability) {
		noAcceptableTargets();
	}

	public boolean doNightAction(Player owner, Narrator n) {
		return NoNightActionVisit(owner, n);
	}


	public boolean hasDayAction(){
		return !getRevealed();
	}
	public void doDayAction(Player owner, Narrator n, boolean simulation){
		Event e = new Event();
		e.setCommand(owner, REVEAL);
		
		if(simulation)
			return;
		e.add(owner, " has revealed as the Mayor!");
		n.addEvent(e);
		
		
		
		setRevealed();
		
		Player voteTarget = n.getVoteTarget(owner);

		if (voteTarget != null)
			owner.unvote(simulation);
		
		owner.setVotes(getVoteCount());
		
		if (voteTarget != null)
			owner.vote(voteTarget);
		
		if (n.isDay())
			for (NarratorListener nl: n.getListeners())
				nl.onMayorReveal(owner);
	}
	
	public int getVoteCount(){
		return getInt(0);
	}
	
	private void setRevealed(){
		setBool(0, true);
	}
	
	private boolean getRevealed(){
		return getBool(0);
	}
	
	
	
}
