package voss.android.ai;

import voss.android.day.DayManager;
import voss.logic.Narrator;
import voss.logic.Player;
import voss.logic.support.CommandHandler;
import voss.roles.Framer;


public class TestController implements Controller{

	
	private Narrator n;
	public TestController(Narrator n){
		this.n = n;
	}

	public Narrator getNarrator() {
		return n;
	}


	public void log(String string) {
	}


	public void endNight(Player a) {
		a.endNight();
	}

	public void cancelEndNight(Player slave){
		slave.cancelEndNight();
	}

	public void setNightTarget(Player a, Player b, String action) {
		a.setTarget(b, a.parseAbility(action));
	}

	public void setNightTarget(Player a, Player b, String action, String teamName){
		a.setTarget(b, a.parseAbility(action), CommandHandler.parseTeam(teamName, n));
	}

	public void setFrame(Player a, Player b, String teamName) {
		a.setTarget(b, Framer.MAIN_ABILITY, CommandHandler.parseTeam(teamName, n));
	}

	
	public void vote(Player a, Player b) {
		a.vote(b);
	}
	public void skipVote(Player a){
		a.voteSkip();
	}

	public void selectHost(Player host) {
		
	}

	public void say(Player p, String string) {
		p.say(string);//, simulations);
		
	}

}
