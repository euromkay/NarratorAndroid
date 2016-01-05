package voss.shared.ai;

import java.util.ArrayList;

import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.support.Constants;
import voss.shared.roles.Detective;

public class Claim {

	PlayerList accused;
	private ArrayList<Team> teams;
	Player prosecutor;
	
	public Claim(PlayerList accused, ArrayList<Team> t, Player slave) {
		this.accused = accused;
		this.teams= t;
		prosecutor = slave;
	}

	//determines if accused was lying
	public boolean believable(Player slave) {//slave is the evaluator of the claim
		if(accused.getLivePlayers().isEmpty())
			return false;
		
		if(slave == prosecutor)
			return true;
		
		Team slaveTeam = slave.getTeam();
		int friend = 0;
		int enemy = 0;
		for(Team t: teams){
			if(slaveTeam.isEnemy(t.getAlignment()))
				enemy++;
			else
				friend++;
		}
		
		return friend <= enemy;
	}

	//determins if prosecutor is lying
	public boolean outlandish(Player slave) {
		if(prosecutor.isDead())
			return false;

		
		if(accused.getDeadPlayers().isEmpty())
			return false;
		
		for(Player acc: accused.getDeadPlayers()){
			if(acc.isCleaned())
				return false;
			if(teams.contains(acc.getTeam()))
				return false;
		}
		//i did not find a dead person whos team was a team that the prosecutor claimed
		return true;
	}

}
