package voss.shared.ai;

import voss.shared.logic.Player;
import voss.shared.logic.Team;

public class Claim {

	Player accused;
	private Team t;
	Player prosecutor;
	
	public Claim(Player accused, Team t, Player slave) {
		this.accused = accused;
		this.t= t;
		prosecutor = slave;
	}

	//determines if accused was lying
	public boolean believable(Player slave) {
		if(accused.isDead())
			return false;
		
		Team slaveTeam = slave.getTeam();
		if(slaveTeam.knowsTeam() && !slaveTeam.isEnemy(t.getAlignment()))
			return false;
			
		if(slaveTeam.isEnemy(t.getAlignment()))
			return true;
		
		return true;
	}

	//determins if prosecutor is lying
	public boolean outlandish(Player slave) {
		if(prosecutor.isDead())
			return false;

		if(accused.isDead() && !accused.isCleaned() && accused.getTeam() != t){
			return true;
		}
		return false;
	}

}
