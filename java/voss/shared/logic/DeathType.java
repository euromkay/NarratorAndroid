package voss.shared.logic;

import java.util.ArrayList;

import voss.shared.logic.support.Constants;
import voss.shared.packaging.Packager;

public class DeathType {

	private ArrayList<Integer> attacks = new ArrayList<Integer>();
	private boolean lynchDeath;
	private int day;
	public DeathType(boolean phase, int day) {
		lynchDeath = phase;
		this.day = day;
	}

	public DeathType(Packager in) {
		attacks = in.readIntegerList();
		day = in.readInt();
		lynchDeath = in.readBool();

	}
	private PlayerList lynchers;
	public PlayerList getLynchers(){
		return lynchers.copy();
	}
	public void setLynchers(PlayerList lynchers){
		this.lynchers = lynchers;
	}

	public ArrayList<Integer> getList(){
		return attacks;
	}
	
	public void addDeath(int flag) {
		attacks.add(flag);
	}

	public int getDeathDay() {
		return day;
	}
	
	public int size(){
		return attacks.size();
	}

	public int get(int i) {
		return attacks.get(i);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		if(attacks.size() == 1)
			return expand(attacks.get(0));
		for(int i = 0; i < attacks.size(); i++){
			if(i == attacks.size() - 1)
				sb.append(" and ");
			sb.append(expand(attacks.get(i)));
			sb.append(", ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.deleteCharAt(sb.length() - 1);
		sb.append(".");
		return sb.toString();
	}

	private String expand(int i){
		switch (i){
			case Constants.ARSON_KILL_FLAG:
				return "charred to death";
			case Constants.A_YAKUZA:
				return "at the bottom of a lake" ;
			case Constants.A_MAFIA:
				return "in a bomb-rigged car";
			case Constants.BODYGUARD_KILL_FLAG:
				return "in a midnight duel";
			case Constants.VIGILANTE_KILL_FLAG:
				return "full of the bullets of a dark knight";
			case Constants.VETERAN_KILL_FLAG:
				return "in a hundred pieces, by a military grenade";
			case Constants.SK_KILL_FLAG:
				return "full of stab-wounds";
			case Constants.JESTER_KILL_FLAG:
				return "hanging by a rope, an apparent suicide";
			case Constants.MASS_MURDERER_FLAG:
				return "with their guts plastered to the ceiling";
				
		}
		throw new Error("unknown kill flag " + i);
	}

	public void writeToPackage(Packager p) {
		p.write(attacks);
		p.write(day);
		p.write(lynchDeath);
	}

	public boolean isLynch() {
		return lynchDeath;
	}

	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		
		DeathType dt = (DeathType) o;
		
		if(!attacks.equals(dt.attacks))
			return false;
		
		if(day != dt.day)
			return false;
		
		return lynchDeath == dt.lynchDeath;
	}

	
}
