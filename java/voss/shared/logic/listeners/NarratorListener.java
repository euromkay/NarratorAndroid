package voss.shared.logic.listeners;

import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;

public interface NarratorListener {

	void onNightStart(PlayerList lynched);
	
	void onDayStart(PlayerList newDead);

	void onEndGame();


	void onMayorReveal(Player mayor);

	void onArsonDayBurn(Player arson, PlayerList burned);


	void onVote(Player voter, Player target, int voteCount);
	void onUnvote(Player voter, Player prev, int voteCountToLynch);
	void onChangeVote(Player voter, Player target, Player prevTarget, int toLynch);

	

	void onNightTarget(Player owner, Player target);
	void onNightTargetRemove(Player owner, Player prev);

	void onEndNight(Player p);
	void onCancelEndNight(Player p);

	void onMessageReceive(Player owner);

	void onModKill(Player bad);
}
