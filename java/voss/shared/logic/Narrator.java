package voss.shared.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import voss.shared.logic.exceptions.IllegalGameSettingsException;
import voss.shared.logic.exceptions.IllegalRoleCombinationException;
import voss.shared.logic.exceptions.PhaseException;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.logic.exceptions.UnknownRoleException;
import voss.shared.logic.exceptions.VotingException;
import voss.shared.logic.listeners.CommandListener;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.ActionTaker;
import voss.shared.logic.support.Alignment;
import voss.shared.logic.support.CommandHandler;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorHandler;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.Equals;
import voss.shared.logic.support.RolePackage;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.logic.support.StringChoice;
import voss.shared.packaging.Packager;
import voss.shared.roles.Agent;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Blackmailer;
import voss.shared.roles.Bodyguard;
import voss.shared.roles.BusDriver;
import voss.shared.roles.Chauffeur;
import voss.shared.roles.Citizen;
import voss.shared.roles.Consort;
import voss.shared.roles.CultLeader;
import voss.shared.roles.Cultist;
import voss.shared.roles.Detective;
import voss.shared.roles.Doctor;
import voss.shared.roles.Escort;
import voss.shared.roles.Executioner;
import voss.shared.roles.Framer;
import voss.shared.roles.Godfather;
import voss.shared.roles.Janitor;
import voss.shared.roles.Jester;
import voss.shared.roles.Lookout;
import voss.shared.roles.Mafioso;
import voss.shared.roles.MassMurderer;
import voss.shared.roles.Mayor;
import voss.shared.roles.RandomRole;
import voss.shared.roles.Role;
import voss.shared.roles.SerialKiller;
import voss.shared.roles.Sheriff;
import voss.shared.roles.UnsetRole;
import voss.shared.roles.Veteran;
import voss.shared.roles.Vigilante;
import voss.shared.roles.Witch;


public class Narrator{

	
	public static final boolean NIGHT_START = false;
	public static final boolean DAY_START = true;
	public static final int NORMAL_HEALTH = 0;
	public static final String KEY = "NARRATOR_KEY";

	public static boolean DEBUG = true;
	public static final boolean EMU = true;
	
	public static Narrator Default(){
		Narrator narrator = new Narrator();
		narrator.defualt();

		return narrator;
	}

	public void defualt(){
		Narrator narrator = this;
		narrator.addTeam(Constants.A_BENIGN).setName("Benign").setPriority(0);;
		narrator.addTeam(Constants.A_TOWN).setName("Town").setPriority(1);
		narrator.addTeam(Constants.A_OUTCASTS).setName("Outcast").setPriority(2);
		narrator.addTeam(Constants.A_CULT).setName("Cult").setPriority(3);
		narrator.addTeam(Constants.A_MAFIA).setName("Mafia").setPriority(4);
		narrator.addTeam(Constants.A_YAKUZA).setName("Yakuza").setPriority(4);
		narrator.addTeam(Constants.A_SK).setName("Serial Killer").setPriority(5);
		narrator.addTeam(Constants.A_ARSONIST).setName("Arsonist").setPriority(6);
		narrator.addTeam(Constants.A_MM).setName("Mass Murderer").setPriority(7);;



		Team cult = narrator.getTeam(Constants.A_CULT);
		cult.setKnowsTeam(true);
		cult.setCanRecruitFrom(false);

		Team arson = narrator.getTeam(Constants.A_ARSONIST);
		arson.setMustBeAliveToWin();

		Team outcasts = narrator.getTeam(Constants.A_OUTCASTS);
		outcasts.setMustBeAliveToWin();

		Team mmTeam = narrator.getTeam(Constants.A_MM);
		mmTeam.setMustBeAliveToWin();

		final String mafDescription = "The informed minority, these roles must eliminate the Town, other Mafia factions, the cult and evil killing neutrals.";
		
		Team maf = narrator.getTeam(Constants.A_MAFIA);
		maf.setKill(true);
		maf.setKnowsTeam(true);
		maf.setDescription(mafDescription);

		Team maf2 = narrator.getTeam(Constants.A_YAKUZA);
		maf2.setKill(true);
		maf2.setKnowsTeam(true);
		maf2.setDescription(mafDescription);

		Team town = narrator.getTeam(Constants.A_TOWN);
		town.addSheriffDetectableTeam(Constants.A_MAFIA);
		town.addSheriffDetectableTeam(Constants.A_MM);
		town.addSheriffDetectableTeam(Constants.A_YAKUZA);
		town.addSheriffDetectableTeam(Constants.A_SK);
		town.addSheriffDetectableTeam(Constants.A_CULT);
		town.addSheriffDetectableTeam(Constants.A_ARSONIST);
		town.setDescription("The uninformed majority, these roles must eliminate all Mafia factions, the cult, and all evil neutrals");

		Team sk = narrator.getTeam(Constants.A_SK);
		sk.setMustBeAliveToWin();

		narrator.setEnemies(town, outcasts);
		narrator.setEnemies(town, maf);
		narrator.setEnemies(town, mmTeam);
		narrator.setEnemies(town, maf2);
		narrator.setEnemies(town, cult);
		narrator.setEnemies(town, sk);
		narrator.setEnemies(town, arson);

		narrator.setEnemies(maf, maf2);
		narrator.setEnemies(maf, mmTeam);
		narrator.setEnemies(maf, sk);
		narrator.setEnemies(maf, cult);
		narrator.setEnemies(maf, arson);

		narrator.setEnemies(mmTeam, maf2);
		narrator.setEnemies(mmTeam, cult);
		narrator.setEnemies(mmTeam, sk);
		narrator.setEnemies(mmTeam, arson);


		narrator.setEnemies(maf2, sk);
		narrator.setEnemies(maf2, cult);
		narrator.setEnemies(maf2, arson);

		narrator.setEnemies(cult, sk);
		narrator.setEnemies(cult, arson);

		narrator.setEnemies(sk, arson);

		narrator.getRules().DAY_START = NIGHT_START;

	}


	
	public Player Skipper;
	public Narrator(){
		addTeam(Constants.A_SKIP).setName("Skip Team");
		Skipper = new Player("Skip Day", new CommunicatorNull(), this);
		Skipper.setRole(new Citizen(Skipper), Constants.A_SKIP);
		events = new EventManager();
		random = new Random();
		setSeed(random.nextLong());

		cListeners = new ArrayList<>();
	}

	private Random random;
	public Random getRandom(){
		return random;
	}
	public void setSeed(long seed){
		this.seed = seed; 
		random.setSeed(seed);
	}
	private long seed;
	public long getSeed(){
		return seed;
	}
	
	
	
	
	
	
	
	
	
	private Rules rules = new Rules();
	public void setRules(Rules rules){
		this.rules = rules;
	}
	public Rules getRules() {
		return rules;
	}
	
	
	
	
	
	

	private PlayerList players = new PlayerList();
	public int getPlayerCount() {
		return players.size();
	}

	public Player addPlayer(String name, Communicator comm) {
		if(gameStarted)
			throw new PhaseException("Cannot add players if game has already started");
		name = name.replace(" ", "");
		
		synchronized (getPlayerByNameLock){
			Player existing = getPlayerByName(name);
			if(existing != null){
				existing.setCommunicator(comm);
				return existing;
			}else{
				Player p = new Player(name, comm, this);
				players.add(p);
				return p;
			}
		}
	}
	public Player addPlayer(String name){
		return addPlayer(name, new CommunicatorNull());
	}
	public void removePlayer(Player p){
		players.remove(p);
	}
	public void removeAllPlayers(){
		players.clear();
	}
	public void removeAllRoles(){
		randomRoles.clear();
		for(Team t: teams.values())
			t.removeRoles();
	}
	public void removePlayer(String name){
		Player rem = null;
		for(Player p: players){
			if(p.getName().equals(name)){
				rem = p;
				break;
			}
		}
		if(rem != null)
			players.remove(rem);
	}
	private Object getPlayerByNameLock = new Object();
	public Player getPlayerByName(String name){
        if (Skipper.getName().equals(name))
        	return Skipper;
        synchronized(getPlayerByNameLock){
		for (Player p: players){
			if(p == null || p.getName() == null)
				System.err.println("WTF?");
			if (p.getName().equals(name)){
				return p;
			}
		}
        }
		return null;
	}
	public PlayerList getAllPlayers(){
		synchronized (players){
			return players.copy();
		}
	}
	public ArrayList<Player> getDeadPlayers() {
		ArrayList<Player> list = new ArrayList<Player>();
		for(Player p: players)
			if(!p.isAlive())
				list.add(p);
		return list;
	}
	
	
	


	
	private HashMap<Integer, Team> teams = new HashMap<Integer, Team>();
	public ArrayList<Team> getAllTeams() {
		ArrayList<Team> team = new ArrayList<Team>();
		for(Team t: teams.values()){
			if(t.getAlignment() != Constants.A_SKIP)
				team.add(t);
		}
		return team;
	}
	public int getNumberOfTeams(){
		return teams.size() - 1;
	}
	public void addRole(String role, int team) {
			  
		if(!Role.isRole(role))
			throw new UnknownRoleException(role + " doesn't exist");
		
		
		Team t = teams.get(team);
		t.addMember(role);	
	}
	public void addRole(RoleTemplate rt){
		if(Role.isRole(rt.getName()))
			addRole(rt.getName(), rt.getColor());
		else
			randomRoles.add((RandomRole) rt);
	}
	
	//need to return it, to chain the names in default
	public Team addTeam(int team){
		Team t = new Team(team, this);
		teams.put(team, t);
		return t;
	}
	public void setEnemies(Team t1, Team t2){
		t1.addEnemy(t2);
		t2.addEnemy(t1);
	}
	
	public void removeRole(RoleTemplate rT){
		if(Role.isRole(rT.getName())){
			Member m = (Member) rT;
			teams.get(m.getColor()).removeMember(m.getName());	
		}else
			randomRoles.remove(rT);
		
			
	}
	public ArrayList<RoleTemplate> getAllRoles(){
		ArrayList<RoleTemplate> list = new ArrayList<RoleTemplate>();
		for(int i: teams.keySet()){
			for(String role: teams.get(i).getAllMembers())
				list.add(new Member(role, i));
		}
		for(RandomRole r: randomRoles)
			list.add(r);
		Collections.sort(list, RoleTemplate.RandomComparator());
		return list;
	}
	public Team getTeam(int team){
		return teams.get(team);
	}
	
	//cross faction randoms
	private ArrayList<RandomRole> randomRoles = new ArrayList<RandomRole>();

	
	
	
	
	
	public PlayerList getTeamMates(int t){
		Team team = teams.get(t);
		return team.getMembers();
	}
	public ArrayList<Player> getRoles(String roleName){
		ArrayList<Player> listToGet = new ArrayList<Player>();
		for(Player p: players){
			if (p.getRoleName().equals(roleName)){
				listToGet.add(p);
			}
		}
		return listToGet;
	}
	
	
	
	public void runSetupChecks(){
		sizeCheck();
		playerNumberCheck();
		opponentCheck();
		sheriffChecks();
		uniqueChecks();
		randomChecks();
	}
	private void uniqueChecks(){
		ArrayList<Integer> mayors = new ArrayList<>();
		ArrayList<Integer> gf = new ArrayList<>();
		ArrayList<Integer> cl = new ArrayList<>();
		for(Team t: teams.values()){
			for(String s: t.getAllMembers()){
				if(s.equals(Mayor.ROLE_NAME)){
					if(mayors.contains(t.getAlignment()))
						throw new IllegalRoleCombinationException("You cannot have more than one mayor");
					else 
						mayors.add(t.getAlignment());
				}else if(s.equals(Godfather.ROLE_NAME)){
					if(gf.contains(t.getAlignment()))
						throw new IllegalRoleCombinationException("You cannot have more than one gf");
					else 
						gf.add(t.getAlignment());
				}else if(s.equals(CultLeader.ROLE_NAME)){
					if(cl.contains(t.getAlignment()))
						throw new IllegalRoleCombinationException("You cannot have more than one gf");
					else 
						cl.add(t.getAlignment());
				}
			}
					
		}
	}
	private void randomChecks(){
		for(RandomRole r: randomRoles){
			int size = r.getSize();
			if(size <= 1)
				throw new IllegalRoleCombinationException(r.getName() + " can only spawn one role!");
		}
	}
	private void sizeCheck(){
		if(players.size() < 3){
			String message = "You need at THE VERY least 3 players to start a mafia game.";
			throw new IllegalGameSettingsException(message);
		}
	}
	private void opponentCheck(){
		ArrayList<Alignment> total = new ArrayList<Alignment>();
		total.addAll(randomRoles);
		for(Team t: teams.values())
			if(t.getAllMembers().size() != 0)
				total.add(t);
		
		for(int i = 0; i < total.size(); i++){
			Alignment a1 = total.get(i);
			for(int j = i+1; j < total.size(); j++){
				Alignment a2 = total.get(j);
				if(a1.opposes(a2, this))
					return;
			}
				
		}
		
		throw new IllegalGameSettingsException("Lacking Valid Opponents");
	
	}
	private void sheriffChecks(){
		ArrayList<Team> teams_ = new ArrayList<Team>();
		for(Team t: teams.values())
			teams_.add(t);
		
		teamFound:
		for(int i = 0; i < teams.size(); i++){
			Team sheriffTeam = teams_.get(i);
			if(sheriffTeam.has(Sheriff.ROLE_NAME)){
				
				
				//checks whether sherTeam can detect another team that is alive
				enemyChecking:
				for(int j = 0; j < teams_.size(); j++){
					if(j == i) continue enemyChecking;
					Team detectTeam = teams_.get(j);
					if(detectTeam.getAllMembers().size() == 0)
						continue enemyChecking;
					
					if(sheriffTeam.sheriffDetects(detectTeam.getAlignment()))
						continue teamFound;
				}
				
				for(RandomRole r: randomRoles){
					//if the random role spawns a detectable team
					if(sheriffTeam.sheriffDetectsAll(r.getTeams()))
						continue teamFound;
				}
				
				throw new IllegalGameSettingsException("Team" + sheriffTeam.getName() + " is unable to detect.");
			}
				
		}
		
		rTeamFound:
		for(Team t: teams_){
			Member m = new Member(Sheriff.ROLE_NAME, t.getAlignment());
			Team sheriffTeam = t;
			for(RandomRole r: randomRoles){
				if(r.spawns(m)){
					
					rEnemyChecking:
					for(int i = 0; i < teams.size(); i++){
						Team detectTeam = teams_.get(i);
						if(detectTeam.getAllMembers().size() == 0)
							continue rEnemyChecking;
						

						if(sheriffTeam.sheriffDetects(detectTeam.getAlignment()))
							continue rTeamFound;	
								
					}
				
					for(RandomRole s: randomRoles){
						//if the random role spawns a detectable team
						if(sheriffTeam.sheriffDetectsAll(s.getTeams()))
							continue rTeamFound;
					}
				
					throw new IllegalGameSettingsException("Team" + sheriffTeam.getName() + " is unable to detect sheriffs");
				}
			}
		}
		
	}
	private void playerNumberCheck(){
		if(getAllRoles().size() == players.size()){
			return;
		}
		int roleListSize = getAllRoles().size();
		if(roleListSize != players.size()){
			String message;
			if(roleListSize > players.size())
				message = "Not enough players. Remove " + (roleListSize - players.size()) +  " roles or wait for more.";
			else
				message = "Too many players. Add more roles or wait for " + (players.size() - roleListSize) + " more.";
			throw new IllegalGameSettingsException(message);	
		}
	}

	
	


	private boolean gameStarted = false;
	public void startGame(){
		if(gameStarted)
			throw new Error("Cannot start game again.");

		runSetupChecks();
		gameStarted = true;

		ArrayList<RoleTemplate> list = getAllRoles();
		if (!DEBUG) {
			players.sortByName();
			Collections.shuffle(list);
		}
		assignRoles(list);
		
		//assigns targets to possible executioners
		for(Player p: players)
			Executioner.check(p, this);

		players.sortByName();
		
		if(rules.DAY_START){
			dayNumber = 1;
			startDay(new PlayerList());
		}else{
			dayNumber = 0;
			startNight(null);
		}
		
	}
	private void assignRoles(ArrayList<RoleTemplate> list){
		for(int i = 0; i < players.size(); i++){
			Player player = players.get(i);
			RoleTemplate m = list.get(i);
			RolePackage packag = m.getRole(player);
			player.setRole(packag.getRole(), packag.getTeam());
			Team team = teams.get(player.getAlignment());
			
			team.addMember(player);
			Event e = new Event();
			e.dontShowPrivate();
			e.setVisibility(player);
			e.add(player);
			e.add(" are a " + player.getRoleName() + ".");
			addEvent(e);
		}
	}
	public Role convertRoles(String name, Player p){
		return Role.CREATOR(name, p);
    }
	public boolean hasRole(String roleName, int team){
		for(Player p: players)
			try{
				if(p.is(roleName) && p.getTeam().getAlignment() == team)
					return true;
			}catch(PhaseException e){}
		return false;
	}
	
	
	private int dayNumber = -1;
	public int getDayNumber() {
		return dayNumber;
	}
	private boolean isDay = true;
	private static final boolean DAY_TIME = true;
	private static final boolean NIGHT_TIME = false;
	public boolean isDay(){
		return isDay && isInProgress();
	}
	public boolean isNight(){
		return !isDay && isInProgress();
	}	

	
	private void startNight(PlayerList lynchedList){
		
		if(!isDay)
			throw new IllegalStateException("It's already night");
		isDay = NIGHT_TIME;
		if(!isInProgress())
			return;
		
		nightList.clear();

		for (Player p: players){
			p.onNightReset();
		}
		Event e = new Event();
		e.add("\nNight " + dayNumber + "\n");
		addEvent(e);

		for(NarratorListener nl: listeners){
			nl.onNightStart(lynchedList);
		}
	}
	
	
	
	//people who have completed their night actions
	private PlayerList nightList = new PlayerList();
	private int key = 0;
	protected void endNight(Player p){
		if(!canDoNightAction())
			throw new PlayerTargetingException(p.getDescription() + "cannot end night.  It is daytime!");
		
		if(!nightList.contains(p))
			nightList.add(p);

		p.setSubmissionTime(key++);
		
		if(everyoneCompletedNightActions())
			endNight();
		else{
			for(NarratorListener nL: listeners)
				nL.onEndNight(p);
		}
	}
	protected void cancelEndNight(Player p){
		nightList.remove(p);
		for(NarratorListener nL: listeners)
			nL.onCancelEndNight(p);
	}
	protected boolean endedNight(Player p){
		return nightList.contains(p);
	}

	protected boolean canDoNightAction() {
		if(!gameStarted)
			return false;

		boolean gameStillGoing = isInProgress();
		return gameStillGoing && !isDay;
	}

	public boolean everyoneCompletedNightActions() {
		if(isDay != NIGHT_TIME)
			throw new IllegalStateException("It is not night time");
				
		if(players.size() < nightList.size())
			throw new IllegalStateException("Something is wrong with the input of night actions");
		
		
		return getLivePlayers().size() == nightList.size();
	}
	
	public PlayerList getLivePlayers(){
		return players.getLivePlayers();
	}
	
	
	

	
	
	
	
	private EventManager events;
	public String getEvents(String access, boolean HTML){
		if(access.equals(Event.PRIVATE) || !isInProgress())
			return events.access(Event.PRIVATE, HTML);
		return events.access(access, HTML);
	}
	public String getHappenings(){
		return getEvents(Event.PRIVATE, false);
	}
	public String getEvents(Player p, boolean HTML){
		return getEvents(p.getName(), HTML);
	}
	private ArrayList<CommandListener> cListeners;
	public void addEvent(Event e){
		events.add(e);

	}
	public ArrayList<Event> getEvents(){
		return events.getEvents();
	}
	public ArrayList<CommandListener> getCommandListeners(){
		return cListeners;
	}
	public ArrayList<String> getCommands(){
		return events.getCommands();
	}
	
	
	private void endNight(){
		Event f = new Event();
		f.add("\n");
		addEvent(f);
		
		voteList.clear();
		voterList.clear();
		
		//removes extra targets
		for(Player p: players){
			if(!p.isAlive())
				continue;
			
			Player[] target = p.getNightTargets();
			if(p.getAbilityCount() == 1){
				if(target[Role.MAIN_ABILITY] != null && null != target[Team.KILL_])
					if(target[Team.SEND_] == p)
						target[Role.MAIN_ABILITY] = null;
					else
						target[Team.KILL_] = null;
			}else if(p.getAbilityCount() == 2){
				if((target[Role.MAIN_ABILITY] != null && target[Role.SECONDARY_ABILITY] != null) && null != target[Team.KILL_])
					if(target[Team.SEND_] == p)
						target[Role.MAIN_ABILITY] = null;
					else
						target[Team.KILL_] = null;
			}
			p.setBlackmailed(false);
		}
		for(Team t: teams.values())
			t.determineNightAction();
		
		doNightAction(Witch.ROLE_NAME);
		doNightAction(BusDriver.ROLE_NAME, Chauffeur.ROLE_NAME);
		doNightAction(Escort.ROLE_NAME, Consort.ROLE_NAME);
		
		doNightAction(Arsonist.ROLE_NAME);
		
		kills();
		heals();
		
		PlayerList newDeadList = setDead();
		
		cultCheck();
		
		doNightAction(Janitor.ROLE_NAME);
		doNightAction(Blackmailer.ROLE_NAME);
		doNightAction(Framer.ROLE_NAME);
		doNightAction(CultLeader.ROLE_NAME);
		doNightAction(Sheriff.ROLE_NAME);

		doNightAction(Citizen.ROLE_NAME, Mayor.ROLE_NAME, Executioner.ROLE_NAME, Jester.ROLE_NAME, Mafioso.ROLE_NAME);
		stalkings();
	
		
		//changing of roles
		for(Player p: players)
			p.resolveRoleChange();

		
		for(Player p: players.getLivePlayers())
			p.sendNightFeedback();

		
		//executioner changes
		for(Player p: getRoles(Executioner.ROLE_NAME)){
			Executioner exec = (Executioner) p.getRole();
			for(Player newDead : getDeadList(dayNumber)){
				if(newDead.getDeathType().isLynch())
					continue;
				if(newDead.equals(exec.getTarget(p))){
					p.setRole(new Jester(p), p.getAlignment());
					
					//Event jester = new JesterEvent(p);
				}
			}
		}
		
		
		//ending
		nightList.clear();
		dayNumber++;
	
		for(Team t: teams.values())
			t.reset();
	
		for(Player dead: newDeadList){
			Event e = new Event();
			e.dontShowPrivate();
			e.add(dead, " was found ", dead.getDeathType().toString());
			addEvent(e);
		}
		
		startDay(newDeadList);
	}
	protected static int UNSUBMITTED = Integer.MAX_VALUE; 
	private void kills(){
		ArrayList<ActionTaker> killList = new ArrayList<ActionTaker>();
		killList.addAll(getRoles(Veteran.ROLE_NAME));
		killList.addAll(getRoles(SerialKiller.ROLE_NAME));
		killList.addAll(getRoles(Vigilante.ROLE_NAME));
		killList.addAll(getRoles(MassMurderer.ROLE_NAME));
		killList.addAll(getRoles(Arsonist.ROLE_NAME));
		for(Team killers: teams.values()){
			if(killers.canKill())
				killList.add(killers);
		}
		Collections.sort(killList, Player.SubmissionTime);
		
		
		for(ActionTaker killer: killList){
			killer.doNightAction();
		}
		
		guards();
		
		
		PlayerList jesterList = new PlayerList();
		//jester kills
		for(Player p: players)
			if(p.getVotedForJester())
				jesterList.add(p);
		jesterList.sortByName();
		if(jesterList.size() > 0){
			Player lolKilled = jesterList.getRandom(random);
			lolKilled.votedForJester(false);
			lolKilled.kill(Constants.JESTER_KILL_FLAG, lolKilled);
		}
		
	}
	private void cultCheck(){
		if(rules.cultImplodesOnLeaderDeath)
			return;
		
		for(Player p: getDeadPlayers()){
			if(p.getRoleName().equals(CultLeader.ROLE_NAME)){
				if(!p.isAlive() && p.getDeathDay() == dayNumber){
					//change a cultist into a cult leader
					Team t = p.getTeam();
					PlayerList cults = t.getMembers().shuffle(random);
				
					for(Player cult: cults){
						if(cult.isAlive() && cult.getRoleName().equals(Cultist.ROLE_NAME)){
							cult.changeRole(new CultLeader(cult));
							//addToHappenings(new CultChange(cult));
							//addToHappenings(cult.getName() + " has become the Cult Leader");
							break;
						}
						
					}
				}
			}
		}
	}
	private void guards(){
		ArrayList<Player> bgs = getRoles(Bodyguard.ROLE_NAME);
		Collections.sort(bgs, Player.SubmissionTime);
		
		boolean noOneCompleted = false;
		while(!noOneCompleted){
			noOneCompleted = true;
			for(Player bg: bgs){
				bg.doNightAction();
				if(bg.didNightAction()){
					bgs.remove(bg);
					noOneCompleted = false;
					break;
				}
			}
		}
	}
	private void heals(){
		
		ArrayList<Player> healers = getRoles(Doctor.ROLE_NAME);
		//should garuntee that all the healers have targets
		
		ArrayList<Player> injured = new ArrayList<Player>();
		
		while(!healers.isEmpty()){
			Player healer = healers.get(0);
			
			//if the healer isn't alive
			if(healer.getLives() < NORMAL_HEALTH){
				injured.add(healer);
				healers.remove(healer);
			}
			
			else{
				//healer does the heal
				healer.doNightAction();
				
				Player healerTarget = healer.getHouseTarget(Role.MAIN_ABILITY);
				
				//injured only contains doctors
				if(injured.contains(healerTarget)){
					injured.remove(healerTarget);
					healers.add(healerTarget);
				}
				healers.remove(healer);
			}
		}
		
	
	}
	private void doNightAction(String ... roles){
		ArrayList<Player> actioners = new ArrayList<Player>();
		for(String role: roles)
			actioners.addAll(getRoles(role));
		Collections.sort(actioners, Player.SubmissionTime);
		for(Player p: actioners){
			if(p.isAlive())
				p.doNightAction();
		}
	}
	private void stalkings(){
		for(@SuppressWarnings("unused") int i: new int[]{1, 2}){
			doNightAction(Lookout.ROLE_NAME);
			doNightAction(Detective.ROLE_NAME);
			doNightAction(Agent.ROLE_NAME);
		}
		
	}
	
	private PlayerList setDead(){
		PlayerList newDead = new PlayerList();
		for(Player p: players)
			if(p.isAlive() && p.getLives() < NORMAL_HEALTH){
				p.setDead();
				newDead.add(p);
				p.sendNightFeedback();
			}

		return newDead;
	}			
	
	

	
	public PlayerList getDeadList(int dayNumber) {
		PlayerList deadList = new PlayerList();
		for(Player p: getDeadPlayers()){
			if(p.getDeathDay() == dayNumber)
				deadList.add(p);
		}
		return deadList;
	}
	public int getDeadSize() {
		return getDeadPlayers().size();
	}
	
	public int getLiveSize(){
		return getLivePlayers().size();
	}

	
	
	
	private boolean canVote;
	public void startDay(PlayerList newDead){
		isDay = DAY_TIME;
		if(!isInProgress())
			return;


		Event e = new Event();
		e.add("\nDay " + dayNumber + "\n");
		addEvent(e);

		voterList.clear();
		voteList.clear();
		
		for(Player p: getLivePlayers()){
			p.onDayReset();
			voteList.put(p, new PlayerList());
		}
		
		
		voteList.put(Skipper, new PlayerList());
		
		canVote = true;

		for(NarratorListener nl: listeners)
			nl.onDayStart(newDead);

	}

	//voterList maps player to whoever that player is voting
	private HashMap<Player, Player> voterList = new HashMap<>();
	//voteList maps player to whoever is voting the player
	private HashMap<Player, PlayerList> voteList = new HashMap<>();
	private void voteCheck(Player voter, Player target){
		if(!canVote)
			throw new VotingException("Voting booths are closed!");
		isDayCheck();

		if(voter == null)
			throw new NullPointerException("Someone has to be a voter");
		if(voter == target)
			throw new VotingException("Cannot vote for one's self");
		if(target == null)
			throw new NullPointerException("target was null");

		if(target == Skipper)
			throw new VotingException("Skipper cannot vote");
		
		if(!voter.isAlive())
			throw new VotingException("Dead players cannot vote.  " + voter.getDescription() + " is dead.");
		if(!target.isAlive())
			throw new VotingException("Dead players cannot be voted");

		if(voter.isBlackmailed() && !target.equals(Skipper))
			throw new VotingException(voter + ", who is blackmailed, is trying to vote " + target);

		if(voterList.get(voter) == target)
			throw new VotingException(voter + " is already voting this person.");
	}

	//garunteeing target isn't null, and not an unvote
	void vote(Player voter, Player target) {
		voteCheck(voter, target);

		Player prevTarget = null;
		Event e = new Event();
		e.add(voter);
		
		e.setCommand(voter, CommandHandler.VOTE, target.getName());
		
		//can't revote
		int toLynch = getMinLynchVote() - (getVoteCountOf(target) + 1);
		if(voterList.containsKey(voter)){
			prevTarget = unVoteHelper(voter);
			e.add(" changed ", new StringChoice("their").add(voter, "your")," vote from ", prevTarget, " to ", target, numberOfVotesNeeded(toLynch));

			addVoteHelper(voter, target);

			addEvent(e);
			for(NarratorListener nl: listeners){
				nl.onChangeVote(voter, target, prevTarget, toLynch);
			}
		}else{
			e.add(" voted for ", target, numberOfVotesNeeded(toLynch));

			addVoteHelper(voter, target);

			addEvent(e);
			for(NarratorListener nl: listeners)
				nl.onVote(voter, target, toLynch);

		}
		
		checkVote();
	}
	public static String numberOfVotesNeeded(int difference){
        return "  (L - " + difference + ")";
    }
	private void addVoteHelper(Player voter, Player target){
		voterList.put(voter, target);

		PlayerList list = voteList.get(target);
		list.add(voter);

	}
	Player unVote(Player voter) {
		if(!voter.isAlive())
			throw new VotingException("Dead players can't vote or unvote.");
		
		if(!voterList.containsKey(voter))
			throw new VotingException(voter + " isn't voting anyone right now.");

		Event e = new Event();
		e.setCommand(voter, CommandHandler.SKIP_VOTE);
		
		Player prevTarget = unVoteHelper(voter);

		int difference = getMinLynchVote() - getVoteCountOf(Skipper);
		
		if(prevTarget == Skipper)
			e.add(" decided against skipping the lynch" + numberOfVotesNeeded(difference));
		else
			e.add(voter, " unvoted ", prevTarget, numberOfVotesNeeded(difference));
		addEvent(e);

		for(NarratorListener nl: listeners){
			nl.onUnvote(voter, prevTarget, difference);
		}

		return prevTarget;
	}
	private Player unVoteHelper(Player voter){
		Player prevTarget = getVoteTarget(voter);
		voterList.remove(voter);

		PlayerList list = voteList.get(prevTarget);
		list.remove(voter);
		return prevTarget;
	}

	void skipVote(Player p){
		if(p == Skipper)
			throw new VotingException("Skipper cannot vote");
		isDayCheck();
		Player prevTarget = voterList.get(p);
		
		Event e = new Event();
		e.setCommand(p, CommandHandler.SKIP_VOTE);
		
		e.add(p);
		if(prevTarget == Skipper)
			throw new VotingException(p + " is already skipping the day");
		
		if(prevTarget != null)
			unVoteHelper(p);
		

		addVoteHelper(p, Skipper);
		int difference = getMinLynchVote() - getVoteCountOf(Skipper);
		
		if(prevTarget != null)
			e.add(" decided against lynching ", prevTarget, " and instead wants to skip the day " + numberOfVotesNeeded(difference));
		else
			e.add(" voted to skip the day.");
	
		addEvent(e);

		for(NarratorListener nL: listeners){
			nL.onVote(p, Skipper, difference);
		}
		checkVote();
	}

	public void checkVote(){
		isDayCheck();
		//check if target has enough votes to close the booths

		PlayerList lynched = new PlayerList();
		for (Player target: players.getLivePlayers().add(Skipper)) {
			if (getVoteCountOf(target) >= getMinLynchVote()) {
				canVote = false;
				lynched.add(target);
				if (!Skipper.equals(target))
					target.setLynchDeath(dayNumber);

				PlayerList voters = new PlayerList();
				for (Player voter : voteList.get(target)) {
					voters.add(voter);
				}
				//jester check
				if (target.is(Jester.ROLE_NAME)) {
					for (Player p : voters) {
						p.votedForJester(true);
					}
				}

				for (Player exec : getLivePlayers()) {
					if (exec.is(Executioner.ROLE_NAME)) {
						Executioner r = (Executioner) exec.getRole();
						if (r.getTarget(exec).equals(target))
							r.setWon();
					}
				}

				//add to records

			}
		}
		if (lynched.size() > 0) {//no one was lynched
			if(lynched.size() > 1){
				lynched.remove(Skipper);
			}
			endDay(lynched);
		}
	}
	
	protected int getVoteCountOf(Player voted){
		isDayCheck();
		int count = 0;
		PlayerList voters = voteList.get(voted);
		if(voters == null)
			return 0;
		for (Player voter: voters)
			count += voter.getVotePower();
		return count;
	}
	public int getMinLynchVote(){
		isDayCheck();
		int minVote = voteList.size() - 1;//accounts for the extra person out of skip
		minVote /= 2;
		minVote ++;
		return minVote;
	}
	protected Player getVoteTarget(Player voter){
		return voterList.get(voter);
	}
	protected PlayerList getVoteListOf(Player target){
		return voteList.get(target).copy();
	}
	private void isDayCheck(){
		if(isDay == NIGHT_TIME)
			throw new IllegalStateException("Cannot vote during the night");
	}
	
	public Object commandLock = new Object();
	protected void talk(Player p, String message){
		if(p.isBlackmailed())
			return;
		Event e = new Event();
		if (isNight()) {
			e.setVisibility(p.getTeam().getMembers());
		}
		e.add(p, ": ", message);
		addEvent(e);
		//e.setCommand(p, CommandHandler.SAY, message);
		for(NarratorListener nl: listeners){
			nl.onMessageReceive(p);
		}
	}
	
	
	public void endDay(PlayerList lynchedTargets) {
		if(!isDay)
			throw new IllegalStateException("It was already nighttime");

		if(lynchedTargets.contains(Skipper)) {
			Event e = new Event();
			e.add("\nDay was skipped!\n");
			addEvent(e);
		}else {
			boolean first = true;
			for (Player lynched : lynchedTargets) {
				Event e = new Event();
				if(first) {
					e.add("\n");
					first = false;
				}
				e.add(lynched, " was lynched by ", voteList.get(lynched));
				addEvent(e);
			}
		}
		canVote = false;
		
		for(Player p: getLivePlayers())
			p.setCleaned(false);

		cultCheck();
		startNight(lynchedTargets);
		
	}

	
	private ArrayList<NarratorListener> listeners = new ArrayList<NarratorListener>();
	public void addListener(NarratorListener nL){
		listeners.add(nL);
	}
	public void addListener(CommandListener nL){
		synchronized (commandLock){
			cListeners.add(nL);
		}
	}
	public void removeListener(NarratorListener nL){
		listeners.remove(nL);
	}
	public void removeListener(CommandListener cL){
		synchronized (commandLock){
			cListeners.remove(cL);
		}
	}
	
	public ArrayList<NarratorListener> getListeners(){
		return listeners;
	}


	private Object progSync = new Object();
	public synchronized boolean isInProgress(){
		synchronized(progSync){
		if (win != null)
			return false;
		
		if (players.size() == 0){
			return false;
		}
		
		if(players.get(0).is(UnsetRole.ROLE_NAME)){
			return false;
		}
		
		for(Team t: teams.values()){
			if(!t.isAlive())
				continue;
				
			for(int enemyKey: t.getEnemyTeams()){
				Team enemyTeam = teams.get(enemyKey);
				if(enemyTeam.isAlive()){
					if(!isDay || getLivePlayers().size() >= 3)
						return true;
				}
			}
		}
		if(win == null)
			determineWinners();
		
		for(NarratorListener nL: listeners)
			nL.onEndGame();
		
		return false;
		}
	}
	private void determineWinners(){
		ArrayList<Player> winningPlayers = new ArrayList<Player>();
		
		for(Player p: players){
			p.determineWin();
			if(p.isWinner()){
				winningPlayers.add(p);
			}
		}
		
		
		win = winMessage(winningPlayers); 
		
		for(Player p: players)
			p.sendMessage(win.access(Event.PUBLIC, false));
	}
	
	private Event win;
	public Event getWinMessage(){
		return win;
	}

	public static Event winMessage(ArrayList<Player> winners){
		Event e = new Event();
		
		if(winners.size() == 0){
			e.add(Constants.NO_WINNER);
			return e;
		}
		for(int i = 0; i < winners.size() - 1; i++){
			e.add(winners.get(i));
			e.add(", ");
		}
		
		if(winners.size() != 1)
			e.add("and ");
		
		e.add(winners.get(winners.size() - 1));
		
		e.add(" has won!");
		
		return e;
	}
	
	
	public void changeRole(Player p, String role, int team) {
		Team t = getTeam(p.getAlignment());
		t.removeMember(p);
		Role r = convertRoles(role, p);
		if(r == null)
			throw new NullPointerException();
		p.setRole(r, team);
		t = getTeam(team);
		t.addMember(p);;
		
	}

	protected void modkill(Player player) {
		//dayTime modkill
		if(isDay){
			removeFromVotes(new PlayerList(player));
			player.modKillHelper();
			checkVote();
		}else{
			player.modKillHelper();
			if(everyoneCompletedNightActions())
				endNight();
		}
		
		for(CommandListener cl: cListeners)
			cl.onCommand(player + CommandHandler.MODKILL);
		
		for(NarratorListener nl: listeners)
			nl.onModKill(player);
		
	}
	
	public void removeFromVotes(PlayerList resets){
		PlayerList[] objects = new PlayerList[voterList.size()];
		for(PlayerList list: voteList.values().toArray(objects)){
			list.remove(resets);
		}
		for (Player p: resets){
			p.setDead();
			voteList.remove(p);
			voterList.remove(p);
		}

	}



	public void writeToPackage(Packager dest, CommunicatorHandler ch) {
		dest.write(seed + "");
		rules.writeToPackage(dest);

		dest.write(players.size());
		for(Player p: players){
			dest.write(p.getName());
			p.getCommunicator().writeToParcel(dest, ch);
			dest.write(p.isComputer());
		}

		dest.write(teams.size());
		for(Team t: teams.values()){
			dest.write(t.getAlignment());
			dest.write(t.getName());
			dest.write(t.getPriority());
			dest.write(t.knowsTeam());
			dest.write(t.canRecruitFrom());
			dest.write(t.getAliveToWin());
			dest.write(t.canKill());
			dest.write(t.getSheriffDetectables());
			dest.write(t.getEnemyTeams());
		}
		
		dest.write(teams.size());
		for(Team t: teams.values()){
			dest.write(t.getAlignment());
			dest.write(t.getAllMembers());
		}

		dest.write(randomRoles.size());
		for(RandomRole r: randomRoles){
			r.writeToPackage(dest);
		}

		dest.write(gameStarted);

		if(gameStarted)
			events.writeToPackage(dest);
	}
	
	public Narrator(Narrator n){
		this();
		seed = n.seed; 
		rules = new Rules(n.rules);

		for(Player oldP: n.players){
			addPlayer(oldP.getName(), oldP.getCommunicator().copy());
			if(oldP.isComputer())
				oldP.setComputer();
		}
		
		Team t;
		for (Team oldTeam: n.getAllTeams()){
			t = new Team(oldTeam.getAlignment(), this);
			t.setName(oldTeam.getName());
			t.setPriority(oldTeam.getPriority());
			t.setKnowsTeam(oldTeam.knowsTeam());
			t.setCanRecruitFrom(oldTeam.canRecruitFrom());
			if(oldTeam.getAliveToWin())
				t.setMustBeAliveToWin();
			t.setKill(oldTeam.canKill());
			
			for(int alignment: oldTeam.getSheriffDetectables()){
				t.addSheriffDetectableTeam(alignment);
			}
			for(int aligment: oldTeam.getEnemyTeams()){
				t.addEnemy(aligment);
			}
			
			teams.put(t.getAlignment(), t);
			for(String m: oldTeam.getAllMembers()){
				addRole(new Member(m, oldTeam.getAlignment()));
			}
		}

		
		for(RandomRole r: n.randomRoles) {
			randomRoles.add(new RandomRole(r));
		}

		if(n.gameStarted)
			startGame();

		CommandHandler commander = new CommandHandler(this);

		for(String command: n.getCommands()) {
			commander.parseCommand(command);
		}
	}
	
	public Narrator(Packager in, CommunicatorHandler ch){
		this();
		setSeed(Long.parseLong(in.readString()));
		rules = new Rules(in);

		int size = in.readInt();
		Player p;
		HashMap<Player, Communicator> players_to_comms = new HashMap<>();
		for(int i = 0; i < size; i++){
			p = addPlayer(in.readString());
			players_to_comms.put(p, ch.getComm(in, this));
			p.setCommunicator(new CommunicatorNull());
			if(in.readBool())
				p.setComputer();
		}
		
		size = in.readInt();
		Team t;
		for (int i = 0; i < size; i++){
			t = new Team(in.readInt(), this);
			t.setName(in.readString());
			t.setPriority(in.readInt());
			t.setKnowsTeam(in.readBool());
			t.setCanRecruitFrom(in.readBool());
			if(in.readBool())
				t.setMustBeAliveToWin();
			t.setKill(in.readBool());
			
			ArrayList<Integer> sheriffDetectables = in.readIntegerList();
			for(int alignment: sheriffDetectables){
				t.addSheriffDetectableTeam(alignment);
			}
			ArrayList<Integer> enemies = in.readIntegerList();
			for(int aligment: enemies){
				t.addEnemy(aligment);
			}
			
			teams.put(t.getAlignment(), t);
		}

		size = in.readInt();
		int color;
		ArrayList<String> list;
		for(int i = 0; i < size; i++){
			color = in.readInt();
			list = in.readStringList();
			for(String name: list){
				addRole(new Member(name, color));
			}
		}

		size = in.readInt();
		for(int i = 0; i < size; i++) {
			randomRoles.add(new RandomRole(in));
		}

		if(!in.readBool()){
			for(Player comm_less: players_to_comms.keySet()){
				comm_less.setCommunicator(players_to_comms.get(comm_less));
			}
			return;
		}
		startGame();

		CommandHandler commander = new CommandHandler(this);

		size = in.readInt();
		for(int i = 0; i < size; i++) {
			commander.parseCommand(in.readString());
		}

		for(Player comm_less: players_to_comms.keySet()){
			comm_less.setCommunicator(players_to_comms.get(comm_less));
		}
	}

	
	
	
	
	
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(getClass() != o.getClass())
			return false;
		
		Narrator n = (Narrator) o;
		if(notEqual(Skipper, n.Skipper))
			return false;
		if(notEqual(players, n.players))
			return false;
		if(notEqual(teams, n.teams))
			return false;
		if(notEqual(rules, n.rules))
			return false;
		//if(key != n.key)
			//return false;
		if(gameStarted != n.gameStarted)
			return false;
		if(dayNumber != n.dayNumber)
			return false;
		if(isDay != n.isDay)
			return false;
		if(notEqual(nightList, n.nightList))
			return false;
		if(canVote != n.canVote)
			return false;
		if(notEqual(voterList, n.voterList))
			return false;
		if(notEqual(voteList, n.voteList))
			return false;
		if(notEqual(win, n.win))
			return false;
		//if(notEqual(events, n.events))
			//return false;
				
		return true;
	}
	
	private boolean notEqual(Object o, Object p){
		return Equals.notEqual(o, p);
	}

	public boolean isStarted() {
		return gameStarted;
	}
	
	

}
