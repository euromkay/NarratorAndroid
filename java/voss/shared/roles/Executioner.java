package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.exceptions.UnsupportedMethodException;
import voss.shared.logic.support.StringChoice;

public class Executioner extends Role{

	public static final String ROLE_NAME = "Executioner";
	public static final String DEATH_FEEDBACK = "You killed yourself because your target died.";
	public String getRoleName(){
		return ROLE_NAME;
	}
	
	
	public Executioner(Player p){
		super(p);
		if(p.getNarrator().getRules().exeuctionerImmune)
			p.setImmune(true);
	}
	
	public String getRoleInfo() {
		return "Your sole purpose in life is to get your target killed. Do it.";
	}


	public void setAction(Player owner, Player player, int team) {
		throw new UnsupportedMethodException();
	}

	public void isAcceptableTarget(Player owner, Player target, int action) {
		noAcceptableTargets();
	}
	public int getAbilityCount(){
		return 0;
	}
	public boolean doNightAction(Player owner, Narrator n) {
		return NoNightActionVisit(owner, n);
	}
	
	//private int targetID;
	public Player getTarget(Player exec){
		PlayerList allPlayers = exec.getNarrator().getAllPlayers();
		allPlayers.sortByName();
		int indexOfExec = allPlayers.indexOf(exec);
		int position = indexOfExec - getInt(0);
		
		if (position < 0)
			allPlayers.toString();
		
		return allPlayers.get(position);
	}
	


	public void setTarget(Player exec, Player target){
		PlayerList allPlayers = exec.getNarrator().getAllPlayers();
		allPlayers.sortByName();
		int difference = allPlayers.indexOf(exec) - allPlayers.indexOf(target);
		setInt(0, difference);
	}
	
	//private boolean winner = false;
	public boolean isWinner(Player p, Narrator n){
		return getBool(0);
	}
	public void setWon(){
		setBool(0, true);
	}
	
	
	
	
	
	
	
	
	
	
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass()){
			return false;
		}
		
		return super.equals(o);
	}
	
	


	public ArrayList<String> getAbilities() {
		return new ArrayList<String>();
	}


	public static void check(Player exec, Narrator n) {
		if(!exec.getRoleName().equals(Executioner.ROLE_NAME))
			return;
		PlayerList players = n.getAllPlayers().remove(exec);
		Player target = players.getRandom(n.getRandom());
		
		Executioner role = (Executioner) exec.getRole();
		role.setTarget(exec, target);
		Event e = new Event();
		
		StringChoice sc = new StringChoice(exec);
		sc.add(exec, "Your");
		e.add(sc);
		
		sc = new StringChoice("'s");
		sc.add(exec, "");
		
		e.add(sc, " target is ", target, ".");
		e.setVisibility(exec);
		n.addEvent(e);
	}
	
	public boolean isPowerRole() {
		return false;
	}
}
