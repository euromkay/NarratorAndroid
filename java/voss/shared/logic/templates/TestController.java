package voss.shared.logic.templates;

import voss.shared.ai.Controller;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.support.CommandHandler;
import voss.shared.roles.Framer;


public class TestController extends Controller{

	
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

	
	public Player vote(Player a, Player b) {
		a.vote(b);
		return b;
	}
	public Player skipVote(Player a){
		a.voteSkip();
		return a.getSkipper();
	}

	public void selectHost(Player host) {
		
	}

	public void say(Player p, String string, String key) {
		p.say(string, key);
		
	}

	public void doDayAction(Player p){
		p.doDayAction();
	}

	public void unvote(Player slave) {
		slave.unvote();
	}

	public void removeNightTarget(Player a, String action) {
		a.removeTarget(a.parseAbility(action), true);
		
	}
}
