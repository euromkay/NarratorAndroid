package voss.shared.logic;

import voss.shared.packaging.Packager;

/*
 * to add a new rule,
 * 1. make an instance variable,
 * 2. put it in the save file
 * 3. add it into save
 * 4. put it in the load file
 * 5. make sure it has a place in the GUI
 */

public class Rules{
	// Game Settings
	public int DAY_LENGTH = 180;
	public int NIGHT_LENGTH = 60;
	public int DISCUSSION_LENGTH = 30;
	public int TRIAL_LENGTH = 30;

	public boolean DAY_START = Narrator.NIGHT_START;
	// Town Rules
	// Sheriff Rules
	
	public static final int UNLIMITED = -1;

	// Doctor Rules
	public boolean doctorCanHealSelf = false;
	public boolean doctorKnowsIfTargetIsAttacked = true;

	public int vigilanteShots = UNLIMITED;
	
	public int vetAlerts = 3;

	public int mayorVoteCount = 3;
	
	public boolean blockersCanBeBlocked = false;
	
	// Evil Neutral Rules
	public boolean exeuctionerImmune = true;
	public boolean exeuctionerWinImmune = false;
	
	public boolean witchLeavesFeedback = false;

	// Serial Killer
	public boolean serialKillerIsInvulnerable = true;

	public boolean arsonInvlunerable = true;
	public boolean arsonDayIgnite = true;
	
	public boolean mmInvulnerable = true;
	public int mmSpreeDelay = 0;
	
	//cult rules
	public boolean cultKeepsRoles = true;
	public boolean cultLeaderCanOnlyRecruit = false;
	public int cultPowerRoleCooldown = UNLIMITED;
	public int cultConversionCooldown = UNLIMITED;
	public boolean cultImplodesOnLeaderDeath = true;
	
	public boolean gfInvulnerable = true;
	public boolean gfUndetectable = true;
	


	public Rules() {}

	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		Rules r = (Rules) o;
		if(DAY_LENGTH != r.DAY_LENGTH)
			return false;
		if(NIGHT_LENGTH != r.NIGHT_LENGTH)
			return false;
		if(DISCUSSION_LENGTH != r.DISCUSSION_LENGTH)
			return false;
		if(TRIAL_LENGTH != r.TRIAL_LENGTH)
			return false;
		if(DAY_START != r.DAY_START)
			return false;
		
		if(doctorCanHealSelf != r.doctorCanHealSelf)
			return false;
		if(doctorKnowsIfTargetIsAttacked != r.doctorKnowsIfTargetIsAttacked)
			return false;
		if(blockersCanBeBlocked != r.blockersCanBeBlocked)
			return false;
		if(vigilanteShots != r.vigilanteShots)
			return false;
		if(vetAlerts != r.vetAlerts)
			return false;
		if(r.mayorVoteCount != mayorVoteCount)
			return false;
		
		if(exeuctionerImmune != r.exeuctionerImmune)
			return false;
		if(exeuctionerWinImmune != r.exeuctionerWinImmune)
			return false;
		if(witchLeavesFeedback != r.witchLeavesFeedback)
			return false;
		
		if(serialKillerIsInvulnerable != r.serialKillerIsInvulnerable)
			return false;
		
		if(mmInvulnerable != r.mmInvulnerable)
			return false;
		if(mmSpreeDelay != r.mmSpreeDelay)
			return false;
		

		if(cultImplodesOnLeaderDeath != r.cultImplodesOnLeaderDeath)
			return false;
		if(cultKeepsRoles != r.cultKeepsRoles)
			return false;
		if(cultLeaderCanOnlyRecruit != r.cultLeaderCanOnlyRecruit)
			return false;
		if(cultPowerRoleCooldown != r.cultPowerRoleCooldown)
			return false;
		if(cultConversionCooldown != r.cultConversionCooldown)
			return false;
		
		if(arsonInvlunerable != r.arsonInvlunerable)
			return false;
		if(arsonDayIgnite != r.arsonDayIgnite)
			return false;
		
		if(gfInvulnerable != r.gfInvulnerable)
			return false;
		if(gfUndetectable != r.gfUndetectable)
			return false;
		
		return true;
	}
	

	
	
	public Rules(Packager in) {
		arsonDayIgnite = in.readBool();
		arsonInvlunerable = in.readBool();
		blockersCanBeBlocked = in.readBool();
		cultConversionCooldown = in.readInt();
		cultImplodesOnLeaderDeath = in.readBool();
		cultKeepsRoles = in.readBool();
		cultLeaderCanOnlyRecruit = in.readBool();
		cultPowerRoleCooldown = in.readInt();
		DAY_LENGTH = in.readInt();
		DAY_START = in.readBool();
		DISCUSSION_LENGTH = in.readInt();
		doctorCanHealSelf = in.readBool();
		doctorKnowsIfTargetIsAttacked = in.readBool();
		exeuctionerImmune = in.readBool();
		exeuctionerWinImmune = in.readBool();
		gfInvulnerable = in.readBool();
		gfUndetectable = in.readBool();
		mayorVoteCount = in.readInt();
		mmInvulnerable = in.readBool();
		mmSpreeDelay = in.readInt();
		NIGHT_LENGTH = in.readInt();
		serialKillerIsInvulnerable = in.readBool();
		TRIAL_LENGTH = in.readInt();
		vetAlerts = in.readInt();
		vigilanteShots = in.readInt();
		witchLeavesFeedback = in.readBool();	
	}

	public void writeToPackage(Packager dest) {
		dest.write(arsonDayIgnite);
		dest.write(arsonInvlunerable);
		dest.write(blockersCanBeBlocked);
		dest.write(cultConversionCooldown);
		dest.write(cultImplodesOnLeaderDeath);
		dest.write(cultKeepsRoles);
		dest.write(cultLeaderCanOnlyRecruit);
		dest.write(cultPowerRoleCooldown);
		dest.write(DAY_LENGTH);
		dest.write(DAY_START);
		dest.write(DISCUSSION_LENGTH);
		dest.write(doctorCanHealSelf);
		dest.write(doctorKnowsIfTargetIsAttacked);
		dest.write(exeuctionerImmune);
		dest.write(exeuctionerWinImmune);
		dest.write(gfInvulnerable);
		dest.write(gfUndetectable);
		dest.write(mayorVoteCount);
		dest.write(mmInvulnerable);
		dest.write(mmSpreeDelay);
		dest.write(NIGHT_LENGTH);
		dest.write(serialKillerIsInvulnerable);
		dest.write(TRIAL_LENGTH);
		dest.write(vetAlerts);
		dest.write(vigilanteShots);
		dest.write(witchLeavesFeedback);
		dest.signal("\n");
	}

	
	
	
	
}