package voss.shared.ai;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;


public abstract class Controller {

	public abstract void log(String string);

	public abstract void endNight(Player slave);
	public abstract void cancelEndNight(Player slave);

	public abstract void setNightTarget(Player a, Player b, String action);
	public abstract void setNightTarget(Player a, Player b, String action, String teamName);
	public abstract void removeNightTarget(Player a, String action);

	public abstract Player vote(Player slave, Player target);
	public abstract Player skipVote(Player slave);
	public abstract void unvote(Player slave);

	public abstract void selectHost(Player host);

	public abstract void say(Player p, String string);

	public abstract void doDayAction(Player p);
	
	public static Player Translate(Narrator n1, Player trans){
		PlayerList list = n1.getAllPlayers().add(n1.Skipper);
		for(Player p: list){
			if(p.getName().equals(trans.getName())){
				return p;
			}
		}
		return null;
	}
}
