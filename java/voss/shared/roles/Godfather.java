package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Rules;


public class Godfather extends Role{
	
	public static final String ROLE_NAME = "Godfather";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	
	public Godfather(Player p){
		super(p);
		Rules r = p.getNarrator().getRules();
		if(r.gfInvulnerable)
			p.setImmune(true);
		if(r.gfUndetectable)
			p.setDetectable(false);
	}
	
	public static final String NIGHT_PROMPT = "You are the leader of your team!  You can override who is sent to kill";
	public String getRoleInfo() {
		return NIGHT_PROMPT;
	}


	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		noAcceptableTargets();
	}

	public int getAbilityCount(){
		return 0;
	}

	public static final String DEATH_FEEDBACK = "You were killed by the mafia!";
	public boolean doNightAction(Player owner, Narrator n) {
		return NoNightActionVisit(owner, n);
	}


	public ArrayList<String> getAbilities() {
		return new ArrayList<String>();
	}

	

	public double nightVotePower(PlayerList members){
		double d = 0;
		members = PlayerList.clone(members);
		members.remove(this);
		for(Player p: members){
			d += p.nightVotePower(members);
		}
		
		return d-0.1;
	}
}
