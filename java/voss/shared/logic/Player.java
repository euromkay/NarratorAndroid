package voss.shared.logic;

import java.util.ArrayList;
import java.util.Comparator;

import voss.shared.logic.exceptions.IllegalActionException;
import voss.shared.logic.exceptions.NotEqualsException;
import voss.shared.logic.exceptions.PhaseException;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.logic.exceptions.UnknownTeamException;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.ActionTaker;
import voss.shared.logic.support.CommandHandler;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.Equals;
import voss.shared.logic.support.Shuffler;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Blocker;
import voss.shared.roles.Bodyguard;
import voss.shared.roles.Driver;
import voss.shared.roles.Jester;
import voss.shared.roles.Mafioso;
import voss.shared.roles.MassMurderer;
import voss.shared.roles.Role;
import voss.shared.roles.SerialKiller;
import voss.shared.roles.UnsetRole;
import voss.shared.roles.Veteran;
import voss.shared.roles.Vigilante;






public class Player implements ActionTaker{

	private Narrator n;
	public Player(String name, Communicator comm, Narrator n) {
		this.name = name;
		this.comm = comm;
		this.n = n;
		role = new UnsetRole(this);
	}
	
	
	private Communicator comm;
	public void sendMessage(String message){
		comm.sendMessage(message);
		for (NarratorListener nl: n.getListeners()){
			nl.onMessageReceive(this);
		}
	}
	public Player setCommunicator(Communicator comm){
		this.comm = comm;
		return this;
	}
	public void sendMessage(ArrayList<String> message) {
		comm.sendMessage(message);
	}
	public Communicator getCommunicator(){
		return comm;
	}

	
	

	public boolean is(String roleName){
		return getRoleName().equals(roleName);
	}
	
	private int alignment;
	public Team getTeam() {
		return n.getTeam(alignment);
	}
	
	
	private Role role;
	public void setRole(Role r, int alignment){
		role = r;
		this.alignment = alignment;
	}
	public Role getRole(){
		return role;
	}
	public String getDescription(){
		boolean isInProgress = n.isInProgress();
		if(isAlive() && isInProgress || !n.isStarted())
			return name;
		String roleName;
		if (isCleaned() && isInProgress)
			roleName = "?????";
		else
			roleName = getRoleName();
		return name + " (" + roleName + ")";
	}
	public String getRoleName(){
		if(role == null)
			throw new PhaseException("Game hasn't started yet");
		return role.getRoleName();
	}
	public int getAlignment(){
		return alignment;
	}
	
	public Team checkTarget(Player target, int ability) {
		if (ability == Team.KILL_ || ability == Team.SEND_){
			PlayerTargetingException except = null;
			for(Team t: n.getAllTeams()){
				if(t.getMembers().contains(this)){
					try{
						t.isAcceptableTarget(this, target, ability);
						return t;
					}catch (PlayerTargetingException e){
						except = e;
					}
				}
			}
			throw except;
		}
		else{
			role.isAcceptableTarget(this, target, ability);
			return null;
		}
	}
	public boolean isAcceptableTarget(Player target, int ability){
		try{
			checkTarget(target, ability);
			return true;
		}catch (PlayerTargetingException e){
			return false;
		}

	}
	public int getAbilityCount(){
		return role.getAbilityCount();
	}
	public double nightVotePower(PlayerList pl){
		return role.nightVotePower(pl);
	}
	public String getRoleInfo(){
		return role.getRoleInfo();
	}
	public String getNightText(){
		return role.getNightText(n.getAllTeams());
	}
	
	
	public static String getName(Player p, Player target){
		if(p.equals(target))
			return "You";
		else
			return target.getName();
	}

	public String[] getAbilities(){
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(role.getAbilities());
		ArrayList<Team> t = getTeams();
		for(Team team: t){
			for(String s: team.getAbilities())
				if(!list.contains(s))
					list.add(s);
		}
		
		String[] abilities = new String[list.size()];
		for(int i = 0; i < list.size(); i++)
			abilities[i] = list.get(i);
		
		return abilities;
		
	}
	
	public ArrayList<Team> getTeams(){
		ArrayList<Team> list = new ArrayList<Team>();
		for(Team t: n.getAllTeams()){
			if(t.hasMember(this))
				list.add(t);
		}
		return list;
	}
	
	public int parseAbility(String message){
		int ability = role.parseAbility(message);
		if (ability == Role.INVALID_ABILITY)
			return getTeam().parseAbility(message);
		return ability;
	}
	
	
	public static final int MAX_TARGET_NUMBER = Role.MAIN_ABILITY + 1;
	private Player[] nightTargets = new Player[MAX_TARGET_NUMBER];
	private int submissionTime;
	
	public void setTarget(Player target, int ability) {
		if(!n.canDoNightAction())
			throw new PlayerTargetingException("Night actions are not allowed right now!");
		if(target == null)
			throw new NullPointerException("Can't target nobody");
		Team t = checkTarget(target, ability);
		if(n.isDay())
			throw new PlayerTargetingException("Cant set targets during the day");
		if(n.endedNight(this))
			throw new PlayerTargetingException(getName() + " has already ended the night. Cancel end night to change abilities.");

		if (nightTargets[ability] != null)
			removeTarget(ability, false);//untargets previous person without notificaition, maybe it says 'changes? in logs'
		nightTargets[ability] = target;
		
		if(t != null)
			t.getSelectionFeedback(this, target, ability);
		else{
			t = getTeam();
			role.setAction(this, target, ability);
		}

		
		t.putNightAction(this, ability, target);
		for (NarratorListener nl: n.getListeners()){
			nl.onNightTarget(this, target);
		}
	}
	public boolean alliesWith(Player p) {
		return getTeam().getMembers().contains(p);
	}
	public void setTarget(Player target, int ability, int alignment) {
		setOption(alignment);
		setTarget(target, ability);
	}
	private void setOption(int alignment){
		if(n.getTeam(alignment) == null)
			throw new UnknownTeamException(alignment + "");
		role.setFrame(alignment);
	}
	public void overrideTarget(Player target, int ability){
		nightTargets[ability] = target;
	}
	protected void setNightTargets(Player[] targets){
		nightTargets = targets;
	}
	public Player getTarget(int target){
		return nightTargets[target];
	}
	public Player getHouseTarget(int target){
		Player pl = nightTargets[target];
		if(pl == null)
			return null;
		else
			return pl.house;
	}
	public void removeTarget(int ability, boolean notify){
		Player prev = nightTargets[ability];
		nightTargets[ability] = null;

		Event e = new Event();
		e.setCommand(this, Constants.CANCEL, ability+"");
		if(notify){
			Team t = n.getTeam(alignment);
			if(t.knowsTeam())
				for(Player p: t.getMembers()) {
					e = new Event();
					e.add(this, " reconsidered.");
					e.dontShowPrivate();
					n.addEvent(e);
					p.sendMessage(e.access(p, false));
				}
			else {
				e = new Event();
				e.add(this, " reconsidered.");
				e.dontShowPrivate();
				n.addEvent(e);
			}
			for (NarratorListener nl: n.getListeners()){
				nl.onNightTargetRemove(this, prev);
			}
		}
	}
	public Player[] getNightTargets(){
		return nightTargets;
	}
	
	private boolean busDriven = false;
	private Player house = this;
	public void setHouse(Player p){
		house = p;
		if(!busDriven){
			addNightFeedback(Driver.FEEDBACK);
			busDriven = true;
		}
	}
	public void setAddress(Player p){
		house = p;
	}
	public Player getAddress(){
		return house;
	}
	public boolean busDriven(){
		return busDriven;
	}
	
	private boolean completedNightAction = false;
	public void doNightAction(){
		if(completedNightAction)
			return;
		completedNightAction = role.doNightAction(this, n);
	}
	public void setSubmissionTime(int time){
		submissionTime = time;
	}
	protected boolean didNightAction(){
		return completedNightAction;
	}
	protected void setNightActionComplete(){
		completedNightAction = true;
	}
	
	
	public Player getSkipper(){
		return n.Skipper;
	}
	
	
	//player name
	private String name = "";
	public String getName() {
		String t;
		if(role != null && name.length() == 0) {
			t = role.getRoleName() + "-";
			if(getTeam() != null)
				t += getTeam().getName();
			return t;
		}
		return name;
	}
	public Player setName(String name) {
		this.name = name;
		return this;
	}
	
	
	
	
	public int getVoteCount(){
		return n.getVoteCountOf(this);
	}
	public Player getVoteTarget(){
		return n.getVoteTarget(this);
	}
	public PlayerList getVoters(){
		return n.getVoteListOf(this);
	}
	
	public void vote(Player target) {
		synchronized (n){
			if(target == null)
				throw new NullPointerException(this + " voting for null object");
			if(target == n.Skipper)
				voteSkip();
			else if(n.getVoteTarget(this) != target)
				n.vote(this, target);
		}
	}
	public void voteSkip(){
		synchronized(n){
			n.skipVote(this);
		}
	}
	public Player unvote(){
		synchronized(n){
			return n.unVote(this);
		}
	}
	
	public void endNight(){
		synchronized (n){
			Event e = new Event();
			e.setCommand(this, CommandHandler.END_NIGHT);
			e.setPrivate();
			e.dontShowPrivate();
			n.endNight(this);
			n.addEvent(e);
		}
	}
	public void cancelEndNight(){
		synchronized (n){
			Event e = new Event();
			e.setCommand(this, CommandHandler.END_NIGHT);
			e.setPrivate();
			e.dontShowPrivate();
			n.addEvent(e);
			n.cancelEndNight(this);
		}
		
	}
	public boolean endedNight(){
		return n.endedNight(this);
	}
	public void modkill(){
		synchronized (n){
			n.modkill(this);
		}
	}
	private int voteNumber = 1;
	public void setVotes(int voteCount) {
		voteNumber = voteCount;		
	}
	public int getVotePower(){
		return voteNumber;
	}
	
	
	public static final int NULL = -1;
	
	
	/*
	 * role effects
	 */
	private boolean isImmune = false;
	public boolean isImmune() {
		return isImmune;
	}
	public void setImmune(boolean b) {
		isImmune = b;
		lives = 0;
	}
	private boolean jesterVote = false;
	public void votedForJester(boolean b) {
		jesterVote = b;
	}
	public boolean getVotedForJester(){
		return jesterVote;
	}
	private boolean cleaned = false;
	public void setCleaned(boolean b) {
		cleaned = b;
	}
	public boolean isCleaned(){
		return cleaned;
	}
	private boolean blackmailed = false;
	public void setBlackmailed(boolean b){
		blackmailed = b;
	}
	public boolean isBlackmailed(){
		return blackmailed;
	}
	private boolean blocked = false;
	public void getBlocked(Player owner){
		if(blocked)
			return;
		addNightFeedback(Blocker.FEEDBACK);
		blocked = true;
	}
	public boolean isBlocked(){
		return blocked;
	}
	
	private int alignmentStatus = Constants.A_NORMAL;
	public void setFramed(int alignmentStatus) {
		this.alignmentStatus = alignmentStatus;
	}
	public int getFrameStatus(){
		return alignmentStatus;
	}
	
	private int lives = 0;
	public int getLives(){
		return lives;
	}
	
	private ArrayList<Integer> healList = new ArrayList<Integer>();
	public int heal(int role) {
		if(lives < 0){ //meaning he was attacked
			lives++;
			healList.add(role);
			return attackTypeList.remove(0);
		}
		return Constants.NOT_ATTACKED;
	}
	protected int[] getLifeArray(boolean heal){
		ArrayList<Integer> list;
		
		if(heal)
			list = healList;
		else
			list = attackTypeList;
		
		int array[] = new int[list.size()];
		for(int i = 0; i < array.length; i++)
			array[i] = list.get(i);
		return array;
	}
	/*
	 * the attackTypeList keeps track of who attacked the person each night
     * the attackedBy list keeps track of who attacked this person
     * the attack list keeps track of who this person attacked
	 */
	private ArrayList<Integer> attackTypeList = new ArrayList<Integer>();
	private PlayerList attackList = new PlayerList();
	private PlayerList attackedByList = new PlayerList();
	public void kill(int type, Player killer){
		if(!isImmune() || type == Constants.JESTER_KILL_FLAG || (Constants.VETERAN_KILL_FLAG == type && n.getRules().vetAlerts != Rules.UNLIMITED) || (type == Constants.BODYGUARD_KILL_FLAG && !role.getRoleName().equals(Veteran.ROLE_NAME))){
			lives--;
			attackTypeList.add(type);
		}else{
			if(killer == this && isImmune()){
				lives--;
				attackTypeList.add(type);
			}else{
				feedback.add(Constants.NIGHT_IMMUNE_TARGET_FEEDBACK);
			}
		}
	}
	
	public void attack(Player target){
		target.attackedByList.add(this);
		attackList.add(target);
	}
	protected void setAttackedByList(PlayerList attackedByList){
		this.attackedByList = attackedByList;
	}
	protected void setAttackList(PlayerList attackList){
		this.attackList = attackList;
	}
	public PlayerList getAttackedByList(){
		return attackedByList;
	}
	public PlayerList getAttackList(){
		return attackList;
	}
	
	private DeathType deathType;
	
	public DeathType getDeathType(){
		return deathType;
	}
	private void setDead(int flag){
		setDead();
		deathType.addDeath(flag);
		deathType.setDay(n.getDayNumber());
	}
	public void setLynchDeath(int dayNumber){
		setDead(Constants.LYNCH_FLAG);
	}
	
	public void modKillHelper() {
		setDead(Constants.MODKILL_FLAG);
	}
	
	
	
	
	
	public Narrator getNarrator(){
		return n;
	}
	
	
	
	protected void onDayReset(){
		nightTargets = new Player[MAX_TARGET_NUMBER];
		role.dayReset(this);
		jesterVote = false;
		busDriven = false;
		blocked = false;
		feedback.clear();
		visitors.clear();
		visits.clear();
		completedNightAction = false;
		submissionTime = Narrator.UNSUBMITTED;
		house = this;
		alignmentStatus = Constants.A_NORMAL;
		attackList.clear();
		attackedByList.clear();
	}
	protected void onNightReset(){
		role.nightReset();
	}
	
	private ArrayList<String> feedback = new ArrayList<String>();
	public void addNightFeedback(String s){
		feedback.add(s);
	}
	public void sendNightFeedback() {
		//still alive
		boolean alive = (lives >= Narrator.NORMAL_HEALTH);
		
		if(alive){
			//you were attacked and healed
			for(Integer role: healList){
				String mess = determineHealFeedback(role);
				feedback.add(mess);
			}
		}
		else{
			//you weren't
			deathType = new DeathType(n.isDay());
			for(int type: attackTypeList){
				String killFeedback = determineKillFeedback(type);
				feedback.add(killFeedback);
				deathType.addDeath(type);
			}
			
		}	
				
		//dead
		Shuffler.shuffle(feedback, n.getRandom());
		sendMessage(feedback);
	}
	private static String determineKillFeedback(int role){
		if(role == Constants.A_MAFIA || role == Constants.A_YAKUZA)
			return Mafioso.DEATH_FEEDBACK;
		
		else if(role == Constants.SK_KILL_FLAG)
			return SerialKiller.DEATH_FEEDBACK;
		
		else if(role == Constants.JESTER_KILL_FLAG)
			return Jester.DEATH_FEEDBACK;
		
		else if(role == Constants.VIGILANTE_KILL_FLAG)
			return Vigilante.DEATH_FEEDBACK;
		
		else if(role == Constants.VETERAN_KILL_FLAG)
			return Veteran.DEATH_FEEDBACK;
		
		else if(role == Constants.BODYGUARD_KILL_FLAG)
			return Bodyguard.DEATH_TARGET_FEEDBACK;
		
		else if(role == Constants.MASS_MURDERER_FLAG)
			return MassMurderer.DEATH_FEEDBACK;
		else if (role == Constants.ARSON_KILL_FLAG)
			return Arsonist.DEATH_FEEDBACK;
		throw new IllegalArgumentException("The person cannot kill! " + role);
		
	}
	private static String determineHealFeedback(int role){
		if(role == Constants.DOCTOR_HEAL_FLAG)
			return Constants.HEAL_FEEDBACK_DOCTOR;
		else if(role == Constants.BODYGUARD_KILL_FLAG)
			return Bodyguard.SAVING_FEEDBACK;
		throw new IllegalArgumentException(role + ": cannot heal!");
		
	}
	
	
	
	
	private PlayerList visits = new PlayerList();
	protected void setVisits(PlayerList visits){
		this.visits = visits;
	}
	protected void setVisitors(PlayerList visitors){
		this.visitors = visitors;
	}
	private PlayerList visitors = new PlayerList();
	public void visit(Player target) {
		target.getVisitedBy(this);
		if(!visits.contains(target))
			visits.add(target);
	}
	public void getVisitedBy(Player owner){
		if(!visits.contains(owner))
			visitors.add(owner);
	}
	public PlayerList getVisitors(){
		PlayerList list = PlayerList.clone(visitors);
		list.shuffle(n.getRandom());
		return list;
	}
	public PlayerList getVisits(){
		PlayerList list = PlayerList.clone(visits);
		list.shuffle(n.getRandom());
		return list;
	}
	
	public void doDayAction() {
		if(!role.hasDayAction())
			throw new IllegalActionException(this + " has no day action!");
		if(n.isNight())
			throw new PhaseException("Day actions can only be submitted during the day");
		role.doDayAction(this, n);
	}
	public boolean hasDayAction() {
		return role.hasDayAction();
	}
	
	
	public boolean isAlive() {
		return deathType == null;
	}
	public boolean isDead(){
		return !isAlive();
	}
	public int getDeathDay(){
		return deathType.getDeathDay();
	}
	private boolean winner = false;
	public boolean isWinner() {
		return winner;
	}
	protected void determineWin(){
		winner = role.isWinner(this, n);		
	}
	
	
	public static Comparator<ActionTaker> SubmissionTime = new Comparator<ActionTaker>() {
		public int compare(ActionTaker p1, ActionTaker p2) {
			return p1.getSubmissionTime() - p2.getSubmissionTime();
		}

	};
	public static Comparator<Player> NameSort = new Comparator<Player>() {
		public int compare(Player p1, Player p2) {
			return p1.getName().compareTo(p2.getName());
		}

	};
	public int getSubmissionTime(){
		return submissionTime;
	}
	
	public int describeContents() {
		return 0;
	}
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		Player p = (Player) o;
		
		if(alignment != p.alignment)
			return false;
		if(alignmentStatus != p.alignmentStatus)
			return false;
		if(notEqual(attackTypeList, p.attackTypeList))
			return false;
		if(blackmailed != p.blackmailed)
			return false;
		if(blocked != p.blocked)
			return false;
		if(busDriven != p.busDriven)
			return false;
		if(cleaned != p.cleaned)
			return false;
		//if(notEqual(comm, p.comm))
			//return false;
		if(detectable != p.detectable)
			return false;
		if(doused != p.doused)
			return false;
		if(Equals.notEqual(deathType, p.deathType))
			return false;
		if(notEqual(feedback, p.feedback))
			return false;
		if(notEqual(healList, p.healList))
			return false;
		if(jesterVote != p.jesterVote)
			return false;
		if(lives != p.lives)
			return false;
		//if(isComputer() != p.isComputer)
			//return false;
		if(notEqual(name, p.name))
			return false;
		if(nightTargets == null){
			if(p.nightTargets != null)
				return false;
		}else{
			if(nightTargets.length != p.nightTargets.length)
				return false;
			for(int i = 0; i < nightTargets.length; i++){
				Player t1 = nightTargets[i];
				Player t2 = p.nightTargets[i];
				
				if(t1 == null){
					if(t2 != null)
						return false;
				}else{
					if(t2 == null)
						return false;
					if(!t1.name.equals(t2.name))
						return false;
				}
			}
				
		}
			
		if(notEqual(pendingRole, p.pendingRole))
			return false;
		if(notEqual(role, p.role))
			return false;		
		if(winner != p.winner)
			return false;
		
		

		return true;
	}
	private static boolean notEqual(Object o, Object p){
		return Equals.notEqual(o, p);
	}
	
	public int hashCode(){
		return name.hashCode();
	}
	
	public String toString(){
		return getName() + "(" + getRoleName() + ")";
	}
	
	
	
	
	
	
	
	
	
	
	
	

	
	
	public static Player[] array(Player ... target) {
		Player[] array = new Player[target.length];
		for(int i = 0; i < target.length; i++)
			array[i] = target[i];
		return array;
	}
	public static PlayerList list(Player ... target){
		PlayerList list = new PlayerList();
		for(Player p: target){
			list.add(p);
		}
		return list;
	}
	
	
	public static void print(ArrayList<PlayerList> perms){
		for(PlayerList list: perms){
			for(Player p: list)
				System.out.print(p.getDescription() + " ");
			System.out.println();
		}
	}
	public static String cleanup(String message) {
		int lastComma = message.lastIndexOf(",");
		if(lastComma == -1)
			return message;
		else
			return message.substring(0, lastComma);
	}
	public boolean isTargeting(Player target) {
		if(role.isTargeting(this, target))
			return true;
		if(getTeam().isTargeting(this, target))
			return true;
		return false;
	}
	public boolean isAtHome() {
		if(!role.isTargeting(this, null))
			return false;
		if(getTeam().getSender() == this)
			return false;
		return true;
	}
	public void sendTeamTextPrompt() {
		getTeam().sendNightTextPrompt(this);
	}
	
	private static void error(String s){ 
		throw new NotEqualsException(s);
	}
	
	//not kept up anymore
	public static void assertEqual(Player p1, Player p2){
		if(p1.alignment != p2.alignment)
			error("alignments not equal");
		if(p1.alignmentStatus != p2.alignmentStatus)
			error("alignment status not equal");
		if(Player.notEqual(p1.attackTypeList, p2.attackTypeList))
			error("attack type list not equal");
		if(p1.blackmailed != p2.blackmailed)
			error("blackmailed not equal");
		if(p1.blocked != p2.blocked)
			error("blocked not equal");
		if(p1.busDriven != p2.busDriven)
			error("busdriven not equal");
		if(p1.cleaned != p2.cleaned)
			error("cleaned not equal");
		if(Player.notEqual(p1.comm, p2.comm))
			error("comm not equal");
		if(Player.notEqual(p1.doused, p2.doused))
			error("doused not equal");
		if(Player.notEqual(p1.deathType, p2.deathType))
			error("deathtype not equal");
		if(Player.notEqual(p1.feedback, p2.feedback))
			error("feedback not equal");
		if(Player.notEqual(p1.healList, p2.healList))
			error("heallist not equal");
		if(p1.jesterVote != p2.jesterVote)
			error("jestervote not equal");
		if(p1.isComputer() != p2.isComputer)
			error("isCOmputer not equal");
		if(p1.lives != p2.lives)
			error("lives not equal");
		if(notEqual(p1.name, p2.name))
			error("name not equal");
		if(p1.nightTargets == null){
			if(p2.nightTargets != null)
				error("nighttargets not null");
		}else{
			if(p1.nightTargets.length != p2.nightTargets.length)
				error("night target lengths not equal");
			for(int i = 0; i < p1.nightTargets.length; i++){
				Player t1 = p1.nightTargets[i];
				Player t2 = p2.nightTargets[i];
				
				if(t1 == null){
					if(t2 != null)
						error("p2 night target not null");
				}else{
					if(t2 == null)
						error("p2 night target null");
					if(!t1.name.equals(t2.name))
						error("night target " + i + " not the same");
				}
			}
				
		}
			
	
		if(notEqual(p1.role, p2.role))
			error("role not equal");
		if(p2.winner != p1.winner)
			error("winner not equal");
	}
	
	
	public void setTeam(Team team) {
		alignment = team.getAlignment();
		
	}
	public static String DescriptionList(PlayerList members) {
		String s = "";
		for(Player p: members){
			s += p.getDescription() + ", ";
		}
		return cleanup(s);
	}
	
	private Role pendingRole;
	public void changeRole(Role r) {
		if(n.isNight())
			pendingRole = r;
		else{
			role = r;
			sendMessage("You are now a " + role.getRoleName() + ".");
		}
	}
	public void resolveRoleChange(){
		if(pendingRole != null){
			addNightFeedback("You are now a " + pendingRole.getRoleName() + ".");
			role = pendingRole;
			pendingRole = null;
		}
	}
	public static PlayerList emptyList() {
		return Player.list();
	}
	public static Player[] emptyArray() {
		return new Player[0];
	}
	public void setDead() {
		deathType = new DeathType(n.isDay());
		getTeam().removeMember(this);
		
	}
	public boolean isPowerRole() {
		return role.isPowerRole();
	}
	
	private boolean doused = false;
	public void setDoused(boolean b) {
		doused = b;
		
	}
	public boolean isDoused() {
		return doused;
	}
	
	private boolean detectable = true;
	public boolean isDetectable() {
		return detectable;
	}
	public void setDetectable(boolean b){
		detectable = b;
	}


	private boolean isComputer = false;
	public boolean isComputer(){
		return isComputer;
	}
	public void setComputer(){
		isComputer = true;
		comm = new CommunicatorNull();
	}
	public void say(String message) {
		synchronized(n){
			n.talk(this, message);
		}
	}
}
