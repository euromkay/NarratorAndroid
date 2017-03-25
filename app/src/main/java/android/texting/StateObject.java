package android.texting;


import java.util.ArrayList;
import java.util.HashMap;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import shared.event.DeathAnnouncement;
import shared.event.Message;
import shared.event.SelectionMessage;
import shared.event.VoteAnnouncement;
import shared.logic.DeathType;
import shared.logic.Member;
import shared.logic.MemberList;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.support.Constants;
import shared.logic.support.Faction;
import shared.logic.support.FactionGrouping;
import shared.logic.support.FactionManager;
import shared.logic.support.RoleTemplate;
import shared.logic.support.action.Action;
import shared.logic.support.action.ActionList;
import shared.logic.support.rules.Rule;
import shared.logic.support.rules.RuleBool;
import shared.logic.support.rules.RuleInt;
import shared.logic.support.rules.Rules;
import shared.roles.Citizen;
import shared.roles.ElectroManiac;
import shared.roles.RandomMember;
import shared.roles.Role;
import shared.roles.Sleepwalker;
import shared.roles.Spy;
import shared.roles.Witch;

public abstract class StateObject {

	public static final String RULES       = "Rules";
	public static final String ROLESLIST   = "RolesList";
	public static final String PLAYERLISTS = "PlayerLists";
	public static final String DAYLABEL    = "DayLabel";
	public static final String GRAVEYARD   = "Graveyard";
	public static final String ROLEINFO    = "RoleInfo";
	public static final String ACTIVETEAMS = "ActiveTeams";
	public static final String ACTIONS     = "Actions";
	public static final String VOTES       = "Votes";
	
	public static final String message = "message";
	public static final String gameID = "gameID";
	
	
	
	private Narrator n;
	private FactionManager fManager; 
	private HashMap<String, Object> extraKeys;
	public boolean showHiddenRoles = false;
	public StateObject(Narrator n, FactionManager fManager){
		states = new ArrayList<String>();
		this.n = n;
		this.fManager = fManager;
		extraKeys = new HashMap<>();
	}
	
	ArrayList<String> states;
	public StateObject addState(String state){
		states.add(state);
		return this;
	}
	
	private void addJRolesList(JSONObject state) throws JSONException{
		JSONArray roles = new JSONArray();
		JSONObject role, subRole;
		JSONArray jPossibleRandoms;
		RandomMember rm;
		for(RoleTemplate r: n.getRolesList()){
			role = new JSONObject();
			role.put(StateObject.roleType, r.getName());
			
			role.put(StateObject.color, r.getColor());
			
			if(r.isRandom()){
				role.put(StateObject.roleID, r.getID());
				rm = (RandomMember) r;
				jPossibleRandoms = new JSONArray();
				
				Member m;
				for(String key: rm.list.keySet()){
					m = rm.list.get(key);
					subRole = new JSONObject();
					subRole.put(StateObject.roleType, m.getName());
					subRole.put(StateObject.color, m.getColor());
					subRole.put(StateObject.key, key);
					jPossibleRandoms.put(subRole);
					
				}
				
				m = rm.getSpawn();
				if(m != null && showHiddenRoles){
					subRole = new JSONObject();
					subRole.put(StateObject.roleType, m.getName());
					subRole.put(StateObject.color, r.getColor());
					role.put(StateObject.spawn, subRole);
				}

				role.put(StateObject.possibleRandoms, jPossibleRandoms);
			}

			roles.put(role);
		}
		state.getJSONArray(StateObject.type).put(StateObject.roles);
		state.put(StateObject.roles, roles);
	}
	
	private void addJDayLabel(JSONObject state) throws JSONException{
		String dayLabel;
		if (!n.isStarted()){
			dayLabel = "Night 0";
		}else if(n.isDay()){
			dayLabel = "Day " + n.getDayNumber();
		}else{
			dayLabel = "Night " + n.getDayNumber();
		}
		state.put("dayNumber", n.getDayNumber());
		state.getJSONArray(StateObject.type).put(StateObject.dayLabel);
		state.put(StateObject.dayLabel, dayLabel);
	}
	
	private ArrayList<Team> shouldShowTeam(Player p){
		ArrayList<Team> teams = new ArrayList<>();
		for(Team t: p.getTeams()){
			if(!t.knowsTeam())
				continue;
			if(t.getMembers().remove(p).getLivePlayers().isEmpty())
				continue;
			teams.add(t);
		}
		return teams;			
	}
	
	private void addNullRoleInfo(JSONObject state) throws JSONException{
		JSONObject roleInfo = new JSONObject();
		roleInfo.put(StateObject.roleColor, "#FFFFFF");
		roleInfo.put(StateObject.roleName, "");
		roleInfo.put(StateObject.roleBaseName, "");
		roleInfo.put(StateObject.roleDescription, "");
		
		roleInfo.put(StateObject.roleKnowsTeam, false);
		
		state.getJSONArray(StateObject.type).put(StateObject.roleInfo);
		state.put(StateObject.roleInfo, roleInfo);
	}
	
	private void addJRoleInfo(Player p, JSONObject state) throws JSONException{
		if(p == null){
			addNullRoleInfo(state);
			return;
		}
		
		String roleName, baseName, description;
		
		if(p.is(Sleepwalker.class)){
			MemberList mList = p.getNarrator().getPossibleMembers();
			Member m;
			if(mList.contains(Citizen.class)){
				m = mList.getOtherColors(Citizen.class).get(0);
			}else{
				m = mList.getOtherColors(Sleepwalker.class).get(0);
			}
			roleName = m.getName();
			baseName = m.getBaseName();
			description = m.getDescription();
		}else{
			roleName = p.getRoleName();
			baseName = p.getRole().getClass().getSimpleName();
			description = p.getRoleInfo();
		}
		
		JSONObject roleInfo = new JSONObject();
		roleInfo.put(StateObject.roleColor, p.getTeam().getColor());
		roleInfo.put(StateObject.roleName, roleName);
		roleInfo.put(StateObject.roleBaseName, baseName);
		roleInfo.put(StateObject.roleDescription, description);
		roleInfo.put(StateObject.breadCount, p.getRole().getBreadCount());
		
		JSONObject chats = new JSONObject();
		
		String chatName;
		for(Team t: p.getTeams()){
			if(t.knowsTeam()){
				chatName = n.getEventManager().getNightLog(t.getColor(), 0).getName();
				chats.put(chatName, t.getColor());
			}
		}
		if(p.isDead())
			chats.put(n.getEventManager().getDeadChat().getName(), Constants.DEAD_CHAT);
		roleInfo.put("chats", chats);
		
		ArrayList<Team> knownTeams = shouldShowTeam(p);
		boolean displayTeam = !knownTeams.isEmpty();
		roleInfo.put(StateObject.roleKnowsTeam, displayTeam);
		if(displayTeam){
			JSONArray allyList = new JSONArray();
			JSONObject allyObject;
			for(Team group: knownTeams){
				for(Player ally: group.getMembers().remove(p).getLivePlayers()){
					allyObject = new JSONObject();
					allyObject.put(StateObject.teamAllyName, ally.getName());
					allyObject.put(StateObject.teamAllyRole, ally.getRoleName());
					allyObject.put(StateObject.teamAllyColor, group.getColor());
					allyList.put(allyObject);
					if(group.godfatherStatus){
						allyObject.put(StateObject.teamGodfather, true);
					}
				}
				
			}
			roleInfo.put(StateObject.roleTeam, allyList);
		}
		ArrayList<String> roleSpecs = p.getRoleSpecs();
		if(!roleSpecs.isEmpty())
			roleInfo.put("roleSpecs", roleSpecs);
		
		if(p.hasDayAction(Role.INVITATION_ABILITY))
			roleInfo.put("acceptingRecruitment", p.ci.accepted);

		boolean electroExtraCharge = false;
		if(p.is(ElectroManiac.class)){
			ElectroManiac em = (ElectroManiac) p.getRole();
			electroExtraCharge = em.usedDoubleAction == null;
		}
		roleInfo.put("electroExtraCharge", electroExtraCharge);
		
		state.getJSONArray(StateObject.type).put(StateObject.roleInfo);
		state.put(StateObject.roleInfo, roleInfo);
	}
	
	private void addJGraveYard(JSONObject state) throws JSONException{
		JSONArray graveYard = jGraveYard(n);

		state.getJSONArray(StateObject.type).put(StateObject.graveYard);
		state.put(StateObject.graveYard, graveYard);
	}
	
	public static JSONArray jGraveYard(Narrator n) throws JSONException{
		JSONArray graveYard = new JSONArray(), deathTypes;
		
		JSONObject graveMarker;
		String color, roleName, baseName;
		Member m;
		DeathType dt;
		for(Player p: n.getDeadPlayers().sortByDeath()){
			graveMarker = new JSONObject();
			if(p.isCleaned()){
				color = Constants.A_CLEANED;
				roleName = "????";
				baseName = roleName;
			}else if(p.hasSuits()){
				m = n.getPossibleMembers().get(p.suits.get(0));
				color = m.getColor();
				roleName = m.getName();
				baseName = m.getBaseName();
			}else{
				color = p.getTeam().getColor();
				roleName = p.getRoleName();
				baseName = p.getRole().getClass().getSimpleName();
			}
			graveMarker.put(StateObject.roleBaseName, baseName);
			graveMarker.put(StateObject.roleName, roleName);
			graveMarker.put(StateObject.color, color);
			graveMarker.put("name", p.getName());
			graveYard.put(graveMarker);
			
			dt = p.getDeathType();
			graveMarker.put("phase", dt.getPhase());
			graveMarker.put("day", dt.getDeathDay());
			
			deathTypes = new JSONArray();
			for(String[] s: dt.getList()){
				deathTypes.put(s[1]);
			}
			graveMarker.put("deathTypes", deathTypes);
		}
		
		
		return graveYard;
	}
	
	private void addJActiveTeams(JSONObject state) throws JSONException{
		JSONArray activeTeams = new JSONArray();
		
		JSONObject teamObject;
		for(Team t: n.getAllTeams()){
			teamObject = new JSONObject();
			teamObject.put(StateObject.color, t.getColor());
			teamObject.put(StateObject.teamName, t.getName());
			activeTeams.put(teamObject);
		}
		
		state.getJSONArray(StateObject.type).put(StateObject.activeTeams);
		state.put(StateObject.activeTeams, activeTeams);
	}
	
	private void addJActions(Player p, JSONObject state) throws JSONException{
		JSONObject jActions = new JSONObject();
		JSONArray jActionList = new JSONArray();
		if(p != null){
			ActionList actions = p.getActions();
			ArrayList<Action> subset = new ArrayList<>();
			
			String text, command;
			JSONObject jAction;
			SelectionMessage sm;
			JSONArray jPlayerNames;
			Team t;
			for(Action a: actions){
				jAction = new JSONObject();
				sm = new SelectionMessage(p, true, false);
				jPlayerNames = new JSONArray();
				
				subset.clear();
				subset.add(a);
				text = "You will " + sm.add(p.getRole().getPhrase(subset)).access(p, true) + ".";
				jAction.put("text", text);
				
				command = p.reverseParse(a.ability);
				if(command != null)
					jAction.put("command", command.toLowerCase());
				else{
					System.err.println("couldn't reverse this ability");
					System.err.println(p.toString());
				}
				if(p.is(Spy.class) && a.ability == Spy.MAIN_ABILITY){
					t = n.getTeam(a.getOption());
					jPlayerNames.put(t.getName());
				}else{
					for(Player target: a.getTargets())
						jPlayerNames.put(target.getName());
				}
				jAction.put("playerNames", jPlayerNames);
				jAction.put(option, a.getOption());
				jAction.put(option2, a.getOption2());
				
				jActionList.put(jAction);
			}
			
			
			jActions.put("canAddAction", p.getActions().canAddAnotherAction());
		}else{
			jActions.put("canAddAction", false);
		}

		jActions.put("actionList", jActionList);
		
		
		
		state.getJSONArray(StateObject.type).put(StateObject.actions);
		state.put(StateObject.actions, jActions);
	}
	
	private void addJRules(JSONObject state) throws JSONException{
		addJFactions(state);
		JSONObject jRules = new JSONObject();
		Rules rules = n.getRules();
		Rule r;
		JSONObject ruleObject;
		for(String key: rules.rules.keySet()){
			ruleObject = new JSONObject();
			r = rules.getRule(key);
			ruleObject.put("id", r.id);
			ruleObject.put("name", r.name);
			if(r.getClass() == RuleInt.class){
				ruleObject.put("val", ((RuleInt) r).val);
				ruleObject.put("isNum", true);
			}else{
				ruleObject.put("val", ((RuleBool) r).val);
				ruleObject.put("isNum", false);
			}
			jRules.put(r.id, ruleObject);
		}
		String id;
		Rule fRule;
		for(Team t: n.getAllTeams()){
			if(t.getColor().equals(Constants.A_SKIP))
				continue;
			
			for(String fRuleID: Team.FACTION_RULES){
				fRule = t.getRule(fRuleID);
				
				id = t.getColor() + fRuleID;
				ruleObject = new JSONObject();
				ruleObject.put("id", id);
				ruleObject.put("name", fRule.name);
				ruleObject.put("isNum", fRule instanceof RuleInt);
				if(fRule instanceof RuleInt){
					ruleObject.put("val", ((RuleInt) fRule).val);
				}else{
					ruleObject.put("val", ((RuleBool) fRule).val);
				}
				jRules.put(id, ruleObject);
			}
		}
		
		
		state.getJSONArray(StateObject.type).put(StateObject.rules);
		state.put(StateObject.rules, jRules);
	}
	

	/* all factions object
	 * 
	 * (faction name)  -> faction object
	 * (faction color) -> faction object
	 * factionName     -> list of faction names
	 * 
	 */
	
	/* single faction object
	 * 
	 * name        -> faction name
	 * color       -> faction color
	 * description -> faction description
	 * isEditable  -> can editFaction
	 */
	
	private void addMembersToJRandomRole(JSONObject jRandomRole, RandomMember rr, HashMap<String, JSONObject> dictionary) throws JSONException{
		JSONArray jMembers = new JSONArray();
		JSONObject jMemb;
		String key;
		for(Member rm: rr.list.values()){
			key = rm.getName() + rm.getColor();
			jMemb = dictionary.get(key);
			if(jMemb == null){
				jMemb = packMember(rm, COMPLEX, dictionary);
				dictionary.put(key, jMemb);
			}
			jMembers.put(jMemb);
		}
		jRandomRole.put("members", jMembers);
	}
	
	private void addRuleTexts(JSONObject jo, Member m) throws JSONException{
		ArrayList<String> jRules = m.getBaseRole().getRuleTexts(n, m.getColor());
		if(jRules.size() > 0)
			jo.put("memberRuleExtras", jRules);
	}
	
	private JSONObject packMember(RoleTemplate rt, boolean complex, HashMap<String, JSONObject> dictionary) throws JSONException{
		JSONObject jMember = new JSONObject();
		jMember.put("name", rt.getName());
		
		jMember.put("color", rt.getColor());
		jMember.put("roleName", rt.getName());
		jMember.put("description", rt.getDescription());
		jMember.put("rules", new JSONArray(rt.getRuleIDs()));
		
		
		if(rt.isRandom()){
			addMembersToJRandomRole(jMember, (RandomMember) rt, dictionary);
		}else{
			Member m = (Member) rt;
			jMember.put("simpleName", m.getSimpleName());
			if(n.isInProgress()){
				if(complex)
					addOtherColors(jMember, m, dictionary);
				addRuleTexts(jMember, m);
			}
		}
		
		return jMember;
	}
	
	private static boolean SIMPLE = false, COMPLEX = true;
	
	private void addOtherColors(JSONObject jo, Member m, HashMap<String, JSONObject> dictionary) throws JSONException{
		JSONArray jArray = new JSONArray();
		Member m2;
		for(Member rt: n.getPossibleMembers().getOtherColors(m.getBaseRole().getClass())){	
			m2 = (Member) rt;	
			jArray.put(packMember(m2, SIMPLE, dictionary));
				
		}
		
		if(jArray.length() > 0){
			jo.put("other", jArray);
		}
	}
	
	private void addJFactions(JSONObject state) throws JSONException{
		JSONArray fMembers, blacklisted, allies, enemies, factionNames = new JSONArray();
		JSONObject jFaction, jRT, allyInfo, jFactions = new JSONObject();
		String key;
		ArrayList<FactionGrouping> fGroup = new ArrayList<>();
		MemberList possibleRoles = n.getPossibleMembers();
		HashMap<String, JSONObject> factionRoleMap = new HashMap<>();
		
		if(!n.isStarted())
			for(Faction f: fManager.factions){
				//garuntees that i'm not adding the random/neutral faction unless the game's started
				if(f.getTeam() != null || !n.isStarted())
					fGroup.add(f);
			}
		for(Team t: n.getAllTeams()){
			if(n.isStarted() || fManager.getFaction(t.getColor()) == null)
				fGroup.add(t);
		}
		
		for(FactionGrouping f: fGroup){
			if(n.getTeam(f.getColor()) != null)
				f = n.getTeam(f.getColor());
			jFaction = new JSONObject();

			jFaction.put("color", f.getColor());
			jFaction.put("name", f.getName());
			jFaction.put("description", f.getDescription());
			jFaction.put("isEditable", f.isEditable(fManager));
			jFaction.put("sheriffDetectables", f.getSheriffDetectables());

			fMembers = new JSONArray();
			for(RoleTemplate rt: f.getMembers(fManager, possibleRoles)){
				key = rt.getName() + rt.getColor();
				jRT = factionRoleMap.get(key);
				if(jRT == null){
					jRT = packMember(rt, COMPLEX, factionRoleMap);
					
					factionRoleMap.put(key, jRT);
					jFactions.put(rt.getName() + rt.getColor(), jRT);
				}

				fMembers.put(jRT);
			}
			jFaction.put("members", fMembers);
			
			blacklisted = new JSONArray();
			for(Role rt: f.getUnavailableRoles(fManager)){
				jRT = new JSONObject();
				jRT.put("name", rt.getRoleName());
				jRT.put("simpleName", rt.getClass().getSimpleName());//really should only be using this field, since these are UNAVAILABLE
				blacklisted.put(jRT);
			}
			jFaction.put("blacklisted", blacklisted);
			jFaction.put("rules", f.getRules(fManager));
			
			allies = new JSONArray();
			enemies = new JSONArray();
			for(Team t: n.getAllTeams()){
				if(t == f || f instanceof Faction)
					continue;
				allyInfo = new JSONObject();
				allyInfo.put("color", t.getColor());
				allyInfo.put("name", t.getName());
				if(t.isEnemy((Team) f))
					enemies.put(allyInfo);
				else
					allies.put(allyInfo);
			}
			jFaction.put("allies", allies);
			jFaction.put("enemies", enemies);
			
			
			if(fManager.getFaction(f.getColor()) != null)
				factionNames.put(f.getColor());
			
			jFactions.put(f.getColor(), jFaction);
		}
		
		for(RoleTemplate rt: n.getRolesList()){
			key = rt.getName() + rt.getColor();
			jRT = factionRoleMap.get(key);
			if(jRT == null){
				jRT = packMember(rt, COMPLEX, factionRoleMap);
				
				factionRoleMap.put(key, jRT);
				jFactions.put(rt.getName() + rt.getColor(), jRT);
			}
		}
		
		jFactions.put(StateObject.factionNames, factionNames);
		
		state.getJSONArray(StateObject.type).put(StateObject.factions);
		state.put(StateObject.factions, jFactions);
	}
	
	private String getDescription(Player p){
        if (p.isAlive()) 
            return p.getName();        
        if (p.isCleaned()) 
            return p.getName() + " - ????";
        else 
            return p.getDescription();
	}
	
	private String getColor(Player p){
		if(p.isDead() && !p.isCleaned())
			return p.getColor();
		
		return "#FFFFFF";
	}
	
	private void addVoteMovements(JSONObject j, int i) throws JSONException{
		JSONArray movements = new JSONArray(), deads;
		
		VoteAnnouncement va;
		DeathAnnouncement da;
		JSONObject vote;
		for(Message m: n.getEventManager().getDayChat(i).getEvents()){
			if(m instanceof VoteAnnouncement){
				va = (VoteAnnouncement) m;
				
				vote = new JSONObject();
				vote.put("voter", va.voter);
				vote.put("voted", va.voted);
				vote.put("prev", va.prev);
				if(va.voteCount != 1)
					vote.put("power", va.voteCount);
				movements.put(vote);
			}else if(m instanceof DeathAnnouncement){
				da = (DeathAnnouncement) m;
				if(da.dead == null || da.dead.isEmpty())
					continue;
				
				deads = new JSONArray();
				for(Player d: da.dead){
					deads.put(d.getName());
				}
				movements.put(deads);
			}
		}
		
		j.put("movements", movements);
	}
	
	private void addJVotes(JSONObject state) throws JSONException{
		if(!n.isDay())
			return;
		JSONArray voteCounts = new JSONArray(), alivePlayers;;
		
		JSONObject dayRecap;
		for(int i = 1; i <= n.getDayNumber(); i++){
			dayRecap = new JSONObject();
			alivePlayers = new JSONArray();
			addVoteMovements(dayRecap, i);
			
			for(Player p: n.getAllPlayers()){
				if(p.isAlive())
					alivePlayers.put(p.getName());
				else if(p.getDeathDay() >= i){
					alivePlayers.put(p.getName());
				}
			}
			
			dayRecap.put("alivePlayers", alivePlayers);
			
			/*finalScore = new JSONArray();
			votingPlayers = new PlayerList();
			if(i != n.getDayNumber()){
				voteCounts.put(dayRecap);
				continue;
			}
			
			for(Player p: n.getLivePlayers().add(n.Skipper)){
				voters = new JSONArray();
				finalScoreObject = new JSONObject();
				
				playersVotingX = p.getVoters();
				votingPlayers.add(playersVotingX);
				
				if(playersVotingX.isEmpty()){
					continue;
				}	
				finalScoreObject.put("name", p.getName());
				for(Player voter: playersVotingX){
					voters.put(voter.getName());
				}
				finalScoreObject.put("voters", voters);
				finalScoreObject.put("toLynch", n.getMinLynchVote() - p.getVoteCount());
				finalScore.put(finalScoreObject);
			}
			playersVotingX = n.getLivePlayers().remove(votingPlayers);
			if(!playersVotingX.isEmpty()){
				finalScoreObject = new JSONObject();
				voters = new JSONArray();
				finalScoreObject.put("name", "Not Voting");
				for(Player voter: playersVotingX){
					voters.put(voter.getName());
				}
				finalScoreObject.put("voters", voters);
				finalScoreObject.put("toLynch", -1);
				finalScore.put(finalScoreObject);
			}
			dayRecap.put(StateObject.finalScore, finalScore);*/
			
			voteCounts.put(dayRecap);
		}
		

		state.getJSONArray(StateObject.type).put(StateObject.voteCounts);
		state.put(StateObject.voteCounts, voteCounts);
	}
	
	private JSONArray getJPlayerArray(PlayerList input, PlayerList[] selected, String type) throws JSONException{
		JSONArray arr = new JSONArray();
		if(input.isEmpty())
			return arr;
		PlayerList allPlayers = n.getAllPlayers();
		
		JSONObject jo;
		JSONArray jArray;
		for(Player pi: input){
			jo = new JSONObject();
			jo.put(StateObject.playerName, pi.getName());
			jo.put(StateObject.playerIndex, allPlayers.indexOf(pi) + 1);

			jArray = new JSONArray();
			for(int i = 0; i < selected.length; i++){
				PlayerList select = selected[i];
				if(pi.in(select)){
					jArray.put(i);
				}
			}
			jo.put(StateObject.playerSelectedColumn, jArray);
			
			jo.put(StateObject.playerActive, isActive(pi));
			jo.put(StateObject.playerDescription, getDescription(pi));
			jo.put(StateObject.playerColor, getColor(pi));
			if(n.isStarted()){
				if(pi.getVoters() != null){
					jo.put(StateObject.playerVote, pi.getVoters().size());
				}
				if(n.isNight()){
					jo.put(StateObject.endedNight, pi.endedNight());
				}
			}
			jo.put("isComputer", pi.isComputer());
			jo.put(StateObject.endedNight, pi.endedNight());
			arr.put(jo);
		}
			
		
		
		return arr;
	}
	public abstract boolean isActive(Player p);
	
	private JSONArray getJPlayerArray(PlayerList input, String type) throws JSONException{
		return getJPlayerArray(input, new PlayerList[]{new PlayerList()}, type);
	}
	private JSONArray getJPlayerArray(PlayerList input, Player p, String type) throws JSONException{
		PlayerList list = new PlayerList();
		if(p != null)
			list.add(p);
		return getJPlayerArray(input, new PlayerList[]{list}, type);
	}
	private void addJPlayerLists(JSONObject state, Player p) throws JSONException{
		JSONObject playerLists = new JSONObject();
		playerLists.put(StateObject.type, new JSONArray());
		
		if(n.isStarted() && p != null){
			if(n.isDay){
				PlayerList votes;
				if(n.isInProgress())
					votes = n.getLivePlayers();
				else
					votes = n.getAllPlayers();
				votes.sortByName();
				JSONArray names = getJPlayerArray(votes, p.getVoteTarget(), "Vote");
				playerLists.put("Vote", names);
				playerLists.getJSONArray(StateObject.type).put("Vote");
	
				PlayerList acceptableTargets;
				for(String s_ability: p.getDayActions()){
					int ability = p.parseAbility(s_ability);
					acceptableTargets = p.getAcceptableTargets(ability);
					if(acceptableTargets == null)//triggered by arson burn by having null acceptable targets
						continue;
					if(acceptableTargets.size() == 1 && acceptableTargets.getFirst() == p)
						continue;
					if(acceptableTargets.isEmpty() && ability == Role.MAIN_ABILITY)
						continue;
					names = getJPlayerArray(acceptableTargets, new PlayerList[]{p.getTargets(ability)}, s_ability);
					playerLists.put(s_ability, names);
					playerLists.getJSONArray(StateObject.type).put(s_ability);
				}
			
			}else{
				if(n.isInProgress() && p.isAlive()){
					ArrayList<String> abilities = p.getAbilities();
					ArrayList<String> possibleOptions;
					for(String s_ability: abilities){
						int ability = p.parseAbility(s_ability);
						PlayerList acceptableTargets = p.getAcceptableTargets(ability);
						JSONArray names;
						if(acceptableTargets == null){
							names = new JSONArray();
						}else if(ability == Role.MAIN_ABILITY && p.is(Spy.class)){
							names = new JSONArray();
						}else{
							if(acceptableTargets.isEmpty())
								continue;
							
							if(s_ability.equals(Witch.Control)){
								Player control = p.getTargets(Role.MAIN_ABILITY).getFirst();
								Player target = p.getTargets(Role.MAIN_ABILITY).getLast();
								PlayerList controlList = control == null ? new PlayerList() : Player.list(control);
								PlayerList targetList = target == null ? new PlayerList() : Player.list(target);
								names = getJPlayerArray(acceptableTargets, new PlayerList[]{targetList, controlList}, s_ability);;
							}else{
								names = getJPlayerArray(acceptableTargets, new PlayerList[]{p.getTargets(ability)}, s_ability);
							}
						}
						if(ability == Role.MAIN_ABILITY){
							possibleOptions = p.getOptions();
							if(!possibleOptions.isEmpty())
								playerLists.put("options", new JSONArray(possibleOptions));
						}
						playerLists.put(s_ability, names);
						playerLists.getJSONArray(StateObject.type).put(s_ability);
					}
					if(playerLists.getJSONArray(StateObject.type).length() == 0){
						JSONArray names = getJPlayerArray(new PlayerList(), "None");
						playerLists.put("You have no acceptable night actions tonight!", names);
						playerLists.getJSONArray(StateObject.type).put("You have no acceptable night actions tonight!");
					}
				}else if(n.isInProgress()){
					playerLists.put("The Living", getJPlayerArray(n.getLivePlayers().sortByName(), new PlayerList[]{new PlayerList()}, "The Living"));
					playerLists.getJSONArray(StateObject.type).put("The Living");
				}else{
					playerLists.put("Game Over", getJPlayerArray(n.getAllPlayers(), new PlayerList[]{new PlayerList()}, "Game Over"));
					playerLists.getJSONArray(StateObject.type).put("Game Over");
				}
			}
		}else{
			JSONArray names = getJPlayerArray(n.getAllPlayers(), "Lobby");
			playerLists.put("Lobby", names);
			playerLists.getJSONArray(StateObject.type).put("Lobby");
		}

		if(n.isInProgress()){
			PlayerList infoList = new PlayerList();
			for (Player pi : n.getAllPlayers()) {

				infoList.add(pi);
				
			}
			playerLists.put("info", getJPlayerArray(infoList, "info"));
		}
		
		state.getJSONArray(StateObject.type).put(StateObject.playerLists);
		state.put(StateObject.playerLists, playerLists);
	}

	public abstract JSONObject getObject() throws JSONException;
	public abstract void write(Player p, JSONObject jo);
	
	public JSONObject send(Player p){
		try{
			JSONObject obj = getObject();
			for(String state: states){
				if(state.equals(RULES))
					addJRules(obj);
				else if(state.equals(ROLESLIST))
					addJRolesList(obj);
				else if(state.equals(PLAYERLISTS))
					addJPlayerLists(obj, p);
				else if(state.equals(DAYLABEL))
					addJDayLabel(obj);
				else if(state.equals(GRAVEYARD))
					addJGraveYard(obj);
				else if(state.equals(ROLEINFO))
					addJRoleInfo(p, obj);
				else if(state.equals(ACTIVETEAMS))
					addJActiveTeams(obj);
				else if(state.equals(ACTIONS))
					addJActions(p, obj);
				else if(state.equals(VOTES))
					addJVotes(obj);
				
			}
			for(String key: extraKeys.keySet()){
				obj.put(key, extraKeys.get(key));
			}
			
			if(p != null)
				write(p, obj);
			return obj;
		}catch(JSONException e){
			e.printStackTrace();
		}
		return null;
	}
	
	public void send(PlayerList players) throws JSONException {
		for(Player p: players){
			send(p);
		}
	}

	public StateObject addKey(String key, Object val) {
		extraKeys.put(key, val);
		return this;
	}
	
	
	public static final String hostPublic = "hostPublic";
	public static final String joinPublic = "joinPublic";
	
	public static final String guiUpdate   = "guiUpdate";
	public static final String chatReset   = "chatReset";
	public static final String messageType = "messageType";

	public static final String dayLabel = "dayLabel";
	
	public static final String type = "type";
	
	public static final String playerLists = "playerLists";

	public static final String requestGameState = "requestGameState";
	public static final String requestChat = "requestChat";

	public static final String roles = "roles";
	public static final String roleType = "roleType";
	public static final String color = "color";

	public static final String activeTeams ="activeTeams";
	
	public static final String roleInfo = "roleInfo";
	public static final String roleColor = "roleColor";
	public static final String roleName = "roleName";
	public static final String roleBaseName = "roleBaseName";
	public static final String roleTeam = "roleTeam";
	public static final String roleDescription = "roleDescription";
	public static final String roleKnowsTeam = "roleKnowsTeam";
	public static final String breadCount = "breadCount";
	
	public static final String teamName      = "teamName";
	public static final String teamAllyColor = "teamAllyColor";
	public static final String teamMembers   = "teamMembers";
	public static final String teamAllyName  = "teamAllyName";
	public static final String teamAllyRole  = "teamAllyRole";
	public static final String teamGodfather = "teamGodfather";
	
	public static final String possibleRandoms = "possibleRandoms";
	public static final String spawn           = "spawn";
	public static final String roleID          = "roleID";
	public static final String setRandom       = "setRandom";
	public static final String key             = "key";

	public static final String gameStart = "gameStart";

	public static final String isDay        = "isDay";
	public static final String isObserver   = "isObserver";
	public static final String hasDayAction = "hasDayAction";

	public static final String showButton = "showButton";

	public static final String endedNight = "endedNight";
	public static final String endNight = "endNight";
	
	public static final String graveYard = "graveYard";
	
	public static final String isHost = "isHost";
	public static final String isFinished = "isFinished";
	public static final String addRole = "addRole";
	public static final String removeRole = "removeRole";
	public static final String startGame = "startGame";
	public static final String hiddenRandoms = "hiddenRandoms";
	public static final String host = "host";
	public static final String timer = "timer";
	
	public static final String rules = "rules";
	public static final String ruleChange = "ruleChange";
	public static final String ruleID = "ruleID";

	public static final String playerName = "playerName";
	public static final String playerIndex = "playerIndex";
	public static final String playerVote = "playerVote";
	public static final String skipVote = "skipVote";
	public static final String playerSelectedColumn = "playerSelectedColumn";
	public static final String isSkipping = "isSkipping";
	public static final String playerActive = "playerActive";
	public static final String playerColor = "playerColor";
	public static final String playerDescription = "playerDescription";

	public static final String factions = "factions";
	public static final String factionNames = "factionNames";
	
	public static final String leaveGame = "leaveGame";

	public static final String deleteTeam = "deleteTeam";
	public static final String removeTeamAlly = "removeTeamAlly";
	public static final String removeTeamEnemy = "removeTeamEnemy";
	public static final String enemy = "enemy";
	public static final String ally = "ally";
	public static final String addTeamRole = "addTeamRole";
	public static final String removeTeamRole = "removeTeamRole";
	public static final String simpleName = "simpleName";
	
	public static final String actions = "actions";
	
	public static final String submitAction = "submitAction";
	public static final String oldAction    = "oldAction";
	public static final String newAction    = "newAction";
	public static final String targets      = "targets";
	public static final String command      = "command";
	public static final String option       = "option";
	public static final String option2      = "option2";
	public static final String cancelAction = "cancelAction";
	
	public static final String voteCounts   = "voteCounts";
	public static final String finalScore   = "finalScore";
}
