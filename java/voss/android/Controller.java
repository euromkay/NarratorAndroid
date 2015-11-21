package voss.android.ai;

import voss.logic.Narrator;
import voss.logic.Player;

/**
 * Created by Michael on 8/24/2015.
 */
public interface Controller {
    Narrator getNarrator();

	void log(String string);

	void endNight(Player slave);
	void cancelEndNight(Player slave);

	void setNightTarget(Player a, Player b, String action);
	void setNightTarget(Player a, Player b, String action, String teamName);

	void vote(Player slave, Player target);
	void skipVote(Player slave);

	void selectHost(Player host);

	void say(Player p, String string);
}
