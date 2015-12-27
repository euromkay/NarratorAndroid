package voss.shared.roles;

import java.util.ArrayList;
import java.util.Arrays;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.IllegalActionException;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.logic.exceptions.UnknownRoleException;
import voss.shared.logic.exceptions.UnsupportedMethodException;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.HTString;
import voss.shared.logic.support.StringChoice;
import voss.shared.packaging.Packager;


public abstract class Role{
	
	public Role(Player p){}
	
	public static final int TERTIARY_ABILITY = 2;
	public static final int MAIN_ABILITY = 4;
	public static final int NIGHT_KILL = 0;
	public static final int NIGHT_SEND = 1;
	public static final int SECONDARY_ABILITY = 3;
	public static final int INVALID_ABILITY = -1;
	public static final int VOTE = -2;
	public static final int DAY_ACTION = -3;
	public static final boolean PIERCING = true;

	public abstract String getRoleName();
	/******/
	public void setAction(Player owner, Player target, int ability){
		Team t = owner.getTeam();
		if(getAbilityCount() == 0 || ability != MAIN_ABILITY)
			t.getSelectionFeedback(owner, target, ability);
		else if(target.equals(owner.getSkipper())){
			throw new UnsupportedMethodException("");
			//t.notifyTeammates(owner, " won't " + getKeyWord() + " anyone.");
		}else{
			Event e = Role.selectionEvent(owner);
			
			StringChoice sc = new StringChoice(owner);
			sc.add(owner, "You");
			
			e.setCommand(owner, getKeyWord(), target.getName());
			
			e.add(sc, " will " + getKeyWord().toLowerCase() + " ", target, ".");
			owner.getNarrator().addEvent(e);
			if(t.knowsTeam()){
				for(Player teamMember: t.getMembers()){
					teamMember.sendMessage(e);
				}
			}
			else
				owner.sendMessage(e);
			//t.notifyTeammates(owner, " will " + getKeyWord() + " " + target, getKeyWord());
		}

	}
	public String getNightText(ArrayList<Team> t){
		if(getAbilityCount() == 0){
			return Constants.NO_NIGHT_ACTION;
		}
		return "Type " + NQuote(getKeyWord().toLowerCase()) + " to " + getKeyWord().toLowerCase() + " this person.";
	}
	public abstract String getRoleInfo();
	public abstract ArrayList<String> getAbilities();//must be put in order
	public int parseAbility(String message){
		int i = MAIN_ABILITY;
		for (String s: getAbilities()){
			if(s.equalsIgnoreCase(message))
				return i;
			i--;
		}
		return INVALID_ABILITY;
	}
	public String getKeyWord(){
		return getAbilities().get(0);
	}
	/******/
	public double nightVotePower(PlayerList p){
		return 0.6;// the three fifths compromise
	}
	public int getAbilityCount(){
		return 1;
	}
	public abstract void isAcceptableTarget(Player owner, Player target, int ability);
			//return t.getSelectionFeedback(owner, target, ability);
	
	
	
	public abstract boolean doNightAction(Player owner, Narrator n);
	
	
	public boolean hasDayAction(){
		return false;
	}
	public void doDayAction(Player owner, Narrator n){
		throw new IllegalActionException();
	}
	
	
	public static void Kill(Player owner, Player target, int flag, Narrator n){	
		if(owner.isDetectable())
			owner.visit(target);
		Event e = new Event();
		e.add(owner, " attacked ", target, ".");
		e.setPrivate();
		n.addEvent(e);
		target.kill(flag, owner);
		owner.attack(target);
	}
	public static void Recruit(Player sender, Player target, int teamColor, Narrator n) {
		sender.visit(target);
		Event e = new Event();
		e.add(sender, " recruited ", target, ".");
		e.setPrivate();
		n.addEvent(e);
		
		e = Event.StringFeedback("You've been recruited into the ", target);
		e.add(new HTString(sender.getTeam().getName(), sender.getTeam().getAlignment()));
		e.add(".");
		target.addNightFeedback(e);
		
	}

	public static void Exception(String s){
		throw new PlayerTargetingException(s);
	}

	public void deadCheck(Player p){
		if(p.isDead())
			Exception(getRoleName() + " can't use this ablity on dead targets. " + p + " is dead.");
	}

	public void noAcceptableTargets(){
		Exception("You don't have any role abilities");
	}

	public void allowedAbilities(int check, int ... allowedAbilities){
		for(int i: allowedAbilities){
			if (i == check)
				return;
		}
		Exception("Invalid ability");
	}

	public void selfCheck(Player p, Player q){
		if (p == q)
			Exception("You can't target yourself.");
	}
	
	
	
	protected static boolean NoNightActionVisit(Player owner, Narrator n) {
		Player target;
		if(owner.getRoleName().equals(Witch.ROLE_NAME))
			target = owner.getTarget(Witch.VICTIM);
		else
			target = owner.getHouseTarget(MAIN_ABILITY);
		if(target == null)
			return false;
		owner.visit(target);
		Role.event(owner,  " visited ", target);
		return true;
	}
	public boolean isWinner(Player p, Narrator n){
		Team t = n.getTeam(p.getAlignment());
		
		if (!p.isAlive() && t.getAliveToWin()){
			return false;
		}
		
		if(t.isAlive()){
			for(int teamKey: t.getEnemyTeams()){
				Team enemyTeam = n.getTeam(teamKey);
				
				if(enemyTeam.isAlive() && enemyTeam.winsOver(t))
					return false;
			}
			return true;
		}
		return false;
	}
	

	
	
	
	
	
	
	
	

	
	

	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		return true;
		
	}
	
	
	public void writeToPackage(Packager pack){
		pack.write(getRoleName());
		pack.write(bools.length);
		for(boolean b: bools)
			pack.write(b);
		pack.write(ints);
	}
	
	/*public static Role CREATOR(Player p, Rules r) {
		return CREATOR(role, in, Board.class, r);
	}*/
	public static <T> Role CREATOR(Player p, Packager pack){
		try{
			Role r = CREATOR(pack.readString(), p);
		
			r.bools = new boolean[pack.readInt()];
			for(int i = 0; i < r.bools.length; i++){
				r.bools[i] = pack.readBool();
			}
			r.ints = pack.readIntArray();
		
			return r;
		}catch(UnknownRoleException e){
			pack.finish();
			throw new UnknownRoleException (e.getLocalizedMessage());
		}
		
	}
	
	public static <T> Role CREATOR(String role, Player p){
		switch(role){
		case Agent.ROLE_NAME:
			return new Agent(p);
		case Arsonist.ROLE_NAME:
			return new Arsonist(p);
		case Blackmailer.ROLE_NAME:
			return new Blackmailer(p);
		case Bodyguard.ROLE_NAME:
			return new Bodyguard(p);
		case BusDriver.ROLE_NAME:
			return new BusDriver(p);
		case Chauffeur.ROLE_NAME:
			return new Chauffeur(p);
		case Citizen.ROLE_NAME:
			return new Citizen(p);
		case Consort.ROLE_NAME:
			return new Consort(p);
		case Cultist.ROLE_NAME:
			return new Cultist(p);
		case CultLeader.ROLE_NAME:
			return new CultLeader(p);
		case Detective.ROLE_NAME:
			return new Detective(p);
		case Doctor.ROLE_NAME:
			return new Doctor(p);
		case Escort.ROLE_NAME:
			return new Escort(p);
		case Executioner.ROLE_NAME:
			return new Executioner(p);
		case Framer.ROLE_NAME:
			return new Framer(p);
		case Godfather.ROLE_NAME:
			return new Godfather(p);
		case Janitor.ROLE_NAME:
			return new Janitor(p);
		case Jester.ROLE_NAME:
			return new Jester(p);
		case Lookout.ROLE_NAME:
			return new Lookout(p);
		case Mafioso.ROLE_NAME:
			return new Mafioso(p);
		case MassMurderer.ROLE_NAME:
			return new MassMurderer(p);
		case Mayor.ROLE_NAME:
			return new Mayor(p);
		case SerialKiller.ROLE_NAME:
			return new SerialKiller(p);
		case Sheriff.ROLE_NAME:
			return new Sheriff(p);
		case Veteran.ROLE_NAME:
			return new Veteran(p);
		case Vigilante.ROLE_NAME:
			return new Vigilante(p);
		case Witch.ROLE_NAME:
			return new Witch(p);
			
		}
		throw new UnknownRoleException(role); 
	}

	private static final String[] ROLES = {
			Agent.ROLE_NAME,
			Arsonist.ROLE_NAME,
			Blackmailer.ROLE_NAME,
			Bodyguard.ROLE_NAME,
			BusDriver.ROLE_NAME,
			Chauffeur.ROLE_NAME,
			Citizen.ROLE_NAME,
			Consort.ROLE_NAME,
			Cultist.ROLE_NAME,
			CultLeader.ROLE_NAME,
			Detective.ROLE_NAME,
			Doctor.ROLE_NAME,
			Escort.ROLE_NAME,
			Executioner.ROLE_NAME,
			Framer.ROLE_NAME,
			Godfather.ROLE_NAME,
			Janitor.ROLE_NAME,
			Jester.ROLE_NAME,
			Lookout.ROLE_NAME,
			Mafioso.ROLE_NAME,
			MassMurderer.ROLE_NAME,
			Mayor.ROLE_NAME,
			SerialKiller.ROLE_NAME,
			Sheriff.ROLE_NAME,
			Veteran.ROLE_NAME,
			Vigilante.ROLE_NAME,
			Witch.ROLE_NAME
			};
	
	public static boolean isRole(String role) {
		return Arrays.asList(ROLES).contains(role);
	}
	public void setFrame(int alignment) {}
	public boolean isTargeting(Player owner, Player target) {
		if(owner.getHouseTarget(MAIN_ABILITY) == target)
			return true;
		return false;
	}
	
	private boolean[] bools = new boolean[4];
	public void setBool(int i, boolean b) {
		bools[i] = b;
	}
	public boolean getBool(int i) {
		return bools[i];
	}
	public void dayReset(Player p) {
		
	}
	public void nightReset(){
		
	}
	private int[] ints = new int[2];
	public void setInt(int access, int value) {
		ints[access] = value;
	}
	public int getInt(int i) {
		return ints[i];
	}
	public boolean isPowerRole() {
		return true;
	}
	

	
	
	

	public static String NQuote(String s){
		return SQuote(s + " name");
	}
	
	public static String SQuote(String s){
        return "\'" + s + "\'";
    }
	
	public static void event(Player owner, String string, Player target) {
		Event e = new Event();
		try{
			e.add(owner, string, target, ".");
		}catch(NullPointerException ef){
			throw ef;
		}
		e.setPrivate();
		owner.getNarrator().addEvent(e);
		
	}
	public static Event selectionEvent(Player owner) {
		Event e = new Event();
		if(owner.getTeam().knowsTeam())
			e.setVisibility(owner.getTeam().getMembers());
		else
			e.setVisibility(owner);
		return e;
	}
	

	
}