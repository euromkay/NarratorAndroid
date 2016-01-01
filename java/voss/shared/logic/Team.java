package voss.shared.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import voss.shared.logic.Event;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.logic.support.ActionTaker;
import voss.shared.logic.support.Alignment;
import voss.shared.logic.support.Equals;
import voss.shared.logic.support.StringChoice;
import voss.shared.packaging.Packager;
import voss.shared.roles.Role;

public class Team implements Alignment, ActionTaker{

	
	private String name = " ";
	public String getName(){
		return name;
	}
	public Team setName(String name){
		this.name = name;
		return this;
	}

	private String description;
	public void setDescription(String description){
		this.description = description;
	}
	public String getDescription(){
		return description;
	}
	
	private boolean canRecruitFrom = true;
	
	public void setCanRecruitFrom(boolean b){
		canRecruitFrom = b;
	}
	
	private boolean winsWithOwnTeam = true;
	
	
	public boolean hasTeamAbility() {
		return canKill;
	}
	
	
	private int winPriority;
	public boolean winsOver(Team t){
		if(winPriority == t.winPriority)
			return winsWithOwnTeam == t.winsWithOwnTeam;
			
		//if my priority
		else
			return winPriority > t.winPriority;
	}
	protected void setPriority(int i){
		winPriority = i;
	}
	public int getPriority(){
		return winPriority;
	}

	private Narrator n;
	private int teamColor;
	public Team(int color, Narrator n){
		teamColor = color;
		this.n = n;
	}
	public int getAlignment(){
		return teamColor;
	}
	
	
	private ArrayList<String> list = new ArrayList<String>();
	public void addMember(String m){
		list.add(m);
		Collections.sort(list);
	}
	public void removeMember(String role){
		list.remove(role);
	}
	public void removeRoles(){list.clear();}
	public ArrayList<String> getAllMembers(){
		return list;
	}
	public boolean has(String role){
		return list.contains(role);
	}
	
	private boolean knowsTeam = false;
	public void setKnowsTeam(boolean b){
		knowsTeam = b;
	}
	public boolean knowsTeam(){
		return knowsTeam;
	}

	private boolean solo = false;
	public void setSolo(){
		solo = true;
	}
	public boolean isSolo(){
		return solo;
	}
	
	private boolean canKill = false;
	public void setKill(boolean b){
		//if team can kill, they need to know their teammates
		if(b)
			knowsTeam = true;
		
		canKill = b;
	}
	public boolean canKill(){
		return canKill;
	}
	
	public String getNightPrompt() {
		if(canKill)
			return "Type " + Role.NQuote(SEND) + " to vote on who gets to do the kill and " + Role.NQuote(KILL) + " to pick your kill target.";
		return null;
	}
	
	private boolean mustBeAliveToWin = false;
	public boolean getAliveToWin() {
		return mustBeAliveToWin;
	}
	public void setMustBeAliveToWin(){
		mustBeAliveToWin = true;
	}
	
	private ArrayList<Integer> enemies = new ArrayList<Integer>();
	protected void addEnemy(Team t2){
		enemies.add(t2.teamColor);
	}
	protected void addEnemy(int enemyColor){
		enemies.add(enemyColor);
	}
	public ArrayList<Integer> getEnemyTeams(){
		return enemies;
	}
	public boolean isEnemy(int team){
		return enemies.contains(team);
	}
	public boolean isEnemy(Team t){
		return isEnemy(t.getAlignment());
	}
	
	
	
	
	
	private ArrayList<Integer> sheriffDetectables = new ArrayList<Integer>();
	public boolean sheriffDetects(int enemyTeam) {
		return sheriffDetectables.contains(enemyTeam);
	}
	public void addSheriffDetectableTeam(int team){
		sheriffDetectables.add(team);
	}
	public void removeSheriffDetectableTeam(int team){
		sheriffDetectables.remove(Integer.valueOf(team));
	}
	public boolean sheriffDetectsAll(int[] teams) {
		for(int team: teams)
			if(!sheriffDetectables.contains(team))
				return false;
		return true;
	}
	protected ArrayList<Integer> getSheriffDetectables(){
		return sheriffDetectables;
	}
	
	
	
	private PlayerList members = new PlayerList();
	public void addMember(Player player) {
		members.add(player);
	}
	public void removeMember(Player p) {
		members.remove(p);
	}
	public PlayerList getMembers(){
		return members.copy();
	}
	public int size(){
		return members.size();
	}
	public boolean isAlive(){
		if(members.isEmpty())
			return false;
		for(Player p: members){
			if(p.isAlive() && p.getTeam() == this)
				return true;
		}
		return false;
	}
	
	//players that did vote for someone to send to kill
	private PlayerList nightVote = new PlayerList();
	
	//players that put in their night action kill
	private PlayerList nightTarget = new PlayerList();
	

	public static final int SEND_ = Role.NIGHT_SEND;
	public static final int KILL_ = Role.NIGHT_KILL;
	public static final Team NOT_SUSPICIOUS = null;
	public void getSelectionFeedback(Player owner, Player target, int ability) {
		Event e = Role.selectionEvent(owner);

		StringChoice sc = new StringChoice(owner);
		sc.add(owner, "You");
		
		e.add(sc);
		e.setVisibility(members);
		if(ability == KILL_  && canKill){
			e.add(" will kill ", target, ".");
			
			e.setCommand(owner, KILL, target.getName());
			
			n.addEvent(e);

			if(knowsTeam()){
				for(Player teamMember: getMembers()){
					teamMember.sendMessage(e.access(teamMember, false));
				}
			}
		}else if(ability == SEND_ && (canKill)){
			e.add(" voted to send ", target);
			Player currSender = determineSender();
			if(currSender == null || currSender.equals(n.Skipper)){
				e.add(" but since there's currently no majority, no one is being sent out.");
			}else if(currSender != target){
				e.add(" but ", currSender, " is still being sent out to do the killing.");
			}else{
				e.add(".");
			}
			e.setCommand(owner, SEND, target.getName());
			n.addEvent(e);
			
			

			if(knowsTeam()){
				for(Player teamMember: getMembers()){
					teamMember.sendMessage(e.access(teamMember, false));
				}
			}
		}else
			throw new PlayerTargetingException(owner + " - " + ability);

	}
	
	public static final String KILL = "Kill";
	public static final String SEND = "Send";
	public int parseAbility(String message){
		if(message.equalsIgnoreCase(KILL))
			return Team.KILL_;
		else if(message.equalsIgnoreCase(SEND))
			return Team.SEND_;
		else
			return Role.INVALID_ABILITY;
	}
	
	public void putNightAction(Player owner, int ability, Player target){
		if(!canKill)
			return;
		
		if(ability == SEND_ && owner.isBlackmailed() && target != owner)
			throw new PlayerTargetingException("Blackmailed people can't suggest kills for the mafia");
		
		if(ability == KILL_){
			if(target == null)
				nightTarget.remove(owner);
			else{
				if(!nightTarget.contains(owner))
					nightTarget.add(owner);
			}
				
		}
		
		if(ability == SEND_){
			if(target != null){
				if(!nightVote.contains(owner))
					nightVote.add(owner);
			}
			else
				nightVote.remove(owner);
		}
		
		
	}
	
	private Player sender;
	
	public void determineNightAction(){
		if(hasTeamAbility())
			sender = determineSender();
	}
	public Player getSender(){
		return sender;
	}
	
	private Player determineSender(){
		if(!isAlive())
			return null;
		
		HashMap<Player, Double> tallies = new HashMap<Player, Double>();
		
		if(members.size() == 1){
			return members.get(0);
		}
			
		else{	
		
			for(Player member: members){
				double power = member.nightVotePower(members);
			
				Player prospect = member.getTarget(SEND_); 
				if(prospect == null){
					continue;
				}
				double prev = 0;
				Double temp = tallies.get(prospect);
				if(temp != null)
					prev = temp;
			
				tallies.put(prospect, prev + power);
			}
		}
		
		
		boolean tie = false;
		double max = -1;
		double max2 = -1;//second place
		
		Player killer = null;
		
		for(Player prospect: tallies.keySet()){
			double numOfVotes = tallies.get(prospect);

			if(numOfVotes > max){
				max2 = max;
				max = numOfVotes;
				killer = prospect;
				tie = false;
			}
			
			else if(max == numOfVotes)
				tie = true;
			
		}
		
		if(max == -1)
			return n.Skipper;
		if(max2 == -1)
			max2++;
		if(max2 == max)
			return n.Skipper;
		
		if(tie)
			return n.Skipper;
		
		return killer;
		
	
	}
	
	public int getSubmissionTime(){
		if(sender == null)
			return Narrator.UNSUBMITTED;
		else
			return sender.getSubmissionTime();
	}
	
	public void reset() {
		nightVote.clear();
		nightTarget.clear();
		sender = null;
	}
	
	
	
	public boolean opposes(Alignment a2, Narrator n){
		for(int teamNumb: a2.getTeams()){
			if(!(enemies.contains(teamNumb)))
				return false;
		}
		return true;
			
	}
	
	public int[] getTeams(){
		return new int[]{teamColor};
	}
	
	
	public boolean isPlayer() {
		return false;
	}
	
	public void doNightAction() {
		if(sender != null)
		if(canKill){
			Player target = sender.getHouseTarget(Team.KILL_);
			if(target != null && !sender.didNightAction()){
				Role.Kill(sender, target, teamColor, n);
				sender.setNightActionComplete();
			}
		}
	}
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		if (!canKill)
			exception("You can't kill in a team.");

		owner.getRole().deadCheck(target);

		if(ability == KILL_)
			return;

		//testing sending
		if(owner.isBlackmailed() && owner != target)
			exception("If you're blackmailed, you can only vote to send yourself for the kill.");

		if(!members.contains(target) && n.Skipper != target)
			exception("You can only send an ally for the kill.");
		/*

		if(canKill && target.isAlive()){
			if(ability == SEND_){
				if(owner.isBlackmailed()){
					if(owner == target)
						return true;
					else
						return false;
				}
				return owner.alliesWith(target) || target.equals(n.Skipper);
			}else if(ability == KILL_)
				return true;
		}
		return false;*/
	}

	private void exception(String s){
		Role.Exception(s);
	}
	
	
	
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		Team t = (Team) o;
		
		if(canKill != t.canKill)
			return false;
		if(canRecruitFrom != t.canRecruitFrom)
			return false;
		if(notEqual(enemies, t.enemies))
			return false;
		if(knowsTeam != t.knowsTeam)
			return false;
		if(notEqual(list, t.list))
			return false;
		if(notEqual(members, t.members))
			return false;
		if(notEqual(nightTarget, t.nightTarget))
			return false;
		if(notEqual(nightVote, t.nightVote))
			return false;
		if(sender != t.sender)
			return false;
		if(notEqual(sheriffDetectables, t.sheriffDetectables))
			return false;
		if(solo != t.solo)
			return false;
		if(teamColor != t.teamColor)
			return false;
		if(mustBeAliveToWin != t.mustBeAliveToWin)
			return false;
		if(winPriority != t.winPriority)
			return false;
		if(winsWithOwnTeam != t.winsWithOwnTeam)
			return false;
		
		
		return true;
	}
	
	private boolean notEqual(Object o, Object p){
		return Equals.notEqual(o, p);
	}
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		if(canKill){
			list.add("Kill");
			list.add("Send");
		}
		return list;
	}
	public boolean isTargeting(Player owner, Player target) {
		if(!canKill)
			return false;
		if(canKill){
			if(getSender() == owner && target == owner.getHouseTarget(KILL_))
				return true;
		}
		return false;
	}
	
	public String toString(){
		return name;
	}
	public void sendNightTextPrompt(Player player) {
		String message = getNightPrompt();
		if(message == null)
			return;
		else
			player.sendMessage(message);
	}
	
	public void writeToPackage(Packager p) {
		p.signal("writing team " + name + "\n");
		p.write(canKill);
		p.write(canRecruitFrom);

		p.signal("enemies:");
		p.write(enemies);

		p.signal("knowsTeam");
		p.write(knowsTeam);
		p.write(list);
		p.write(members);
		//writing narrator
		p.write(mustBeAliveToWin);
		p.write(name);
		p.write(nightTarget);
		p.write(nightVote);
		p.write(sender);
		p.write(sheriffDetectables);
		p.write(solo);
		p.write(teamColor);
		p.write(winPriority);
		p.write(winsWithOwnTeam);
		p.signal("\n\n");
	}

	
	public Team(Packager in, Narrator n){
		canKill = in.readBool();
		canRecruitFrom = in.readBool();
		enemies = in.readIntegerList();
		knowsTeam = in.readBool();
		list = in.readStringList();
		members = in.readPlayers(n);
		mustBeAliveToWin = in.readBool();
		this.n = n;
		name = in.readString();
		nightTarget = in.readPlayers(n);
		nightVote = in.readPlayers(n);
		sender = in.readPlayer(n);
		sheriffDetectables = in.readIntegerList();
		solo = in.readBool();
		teamColor = in.readInt();
		winPriority = in.readInt();
		winsWithOwnTeam = in.readBool();
	}
	public boolean hasMember(Player p) {
		return members.contains(p);
	}
	public boolean canRecruitFrom() {
		return canRecruitFrom;
	}
	
}
