package android.texting;


import java.util.ArrayList;
import java.util.HashMap;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import shared.event.SelectionMessage;
import shared.logic.Member;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.support.Constants;
import shared.logic.support.Faction;
import shared.logic.support.FactionManager;
import shared.logic.support.RoleTemplate;
import shared.logic.support.action.Action;
import shared.logic.support.rules.Rule;
import shared.logic.support.rules.RuleBool;
import shared.logic.support.rules.RuleInt;
import shared.logic.support.rules.Rules;
import shared.roles.RandomMember;
import shared.roles.Role;
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
		for(RoleTemplate r: n.getAllRoles()){
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
		
		JSONObject roleInfo = new JSONObject();
		roleInfo.put(StateObject.roleColor, p.getTeam().getColor());
		roleInfo.put(StateObject.roleName, p.getRoleName());
		roleInfo.put(StateObject.roleBaseName, p.getRole().getClass().getSimpleName());
		roleInfo.put(StateObject.roleDescription, p.getRoleInfo());
		roleInfo.put(StateObject.breadCount, p.getRole().getBread());
		
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
				}
				
			}
			roleInfo.put(StateObject.roleTeam, allyList);
		}

		state.getJSONArray(StateObject.type).put(StateObject.roleInfo);
		state.put(StateObject.roleInfo, roleInfo);
	}
	
	private void addJGraveYard(JSONObject state) throws JSONException{
		JSONArray graveYard = jGraveYard(n);

		state.getJSONArray(StateObject.type).put(StateObject.graveYard);
		state.put(StateObject.graveYard, graveYard);
	}
	
	public static JSONArray jGraveYard(Narrator n) throws JSONException{
		JSONArray graveYard = new JSONArray();
		
		JSONObject graveMarker;
		String color;
		for(Player p: n.getDeadPlayers().sortByDeath()){
			graveMarker = new JSONObject();
			if(p.isCleaned())
				color = Constants.A_CLEANED;
			else
				color = p.getTeam().getColor();
			graveMarker.put(StateObject.color, color);
			graveMarker.put(StateObject.roleName, p.getDescription());
			graveMarker.put("name", p.getName());
			graveYard.put(graveMarker);
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
			ArrayList<Action> actions = p.getActions().actions, subset = new ArrayList<>();
			

			String text;
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
				jAction.put("command", p.reverseParse(a.ability).toLowerCase());
				
				if(p.is(Spy.class) && a.ability == Spy.MAIN_ABILITY){
					t = n.getTeam(a.getOption());
					jPlayerNames.put(t.getName());
				}else{
					for(Player target: a.getTargets())
						jPlayerNames.put(target.getName());
				}
				jAction.put("playerNames", jPlayerNames);
				
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
		for(Team t: n.getAllTeams()){
			if(t.getColor().equals(Constants.A_SKIP))
				continue;
			
			id = t.getColor() + "kill";
			ruleObject = new JSONObject();
			ruleObject.put("name", "Has Faction kill");
			ruleObject.put("id", id);
			ruleObject.put("isNum", false);
			ruleObject.put("val", t.canKill());
			jRules.put(id, ruleObject);
			
			id = t.getColor() + "identity";
			ruleObject = new JSONObject();
			ruleObject.put("name", "Knows who allies are");
			ruleObject.put("id", id);
			ruleObject.put("isNum", false);
			ruleObject.put("val", t.knowsTeam());
			jRules.put(id, ruleObject);
			
			id = t.getColor() + "liveToWin";
			ruleObject = new JSONObject();
			ruleObject.put("name", "Must be alive to win");
			ruleObject.put("id", id);
			ruleObject.put("isNum", false);
			ruleObject.put("val", t.getAliveToWin());
			jRules.put(id, ruleObject);
			
			id = t.getColor() + "godfather";
			ruleObject = new JSONObject();
			ruleObject.put("name", "Godfather Status");
			ruleObject.put("id", id);
			ruleObject.put("isNum", false);
			ruleObject.put("val", t.godfatherStatus);
			jRules.put(id, ruleObject);
			
			id = t.getColor() + "priority";
			ruleObject = new JSONObject();
			ruleObject.put("name", "Win priority");
			ruleObject.put("id", id);
			ruleObject.put("val", t.getPriority());
			ruleObject.put("isNum", true);
			jRules.put(id, ruleObject);
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
	
	private void addMembersToJRandomRole(JSONObject jRandomRole, RandomMember rr) throws JSONException{
		JSONArray jMembers = new JSONArray();
		JSONObject jMemb;
		for(Member rm: rr.list.values()){
			jMemb = new JSONObject();
			jMemb.put("name", rm.getName());
			jMemb.put("color", rm.getColor());
			
			jMembers.put(jMemb);
		}
		jRandomRole.put("members", jMembers);
	}
	
	private void addRuleTexts(JSONObject jo, Member m) throws JSONException{
		JSONArray jmRules = new JSONArray();
		for(String ruleID: m.getRuleIDs()){
			jmRules.put(n.getRules().getRule(ruleID).toString());
		}
		if(jmRules.length() > 0)
			jo.put("memberRuleExtras", jmRules);
	}
	
	private JSONObject packMember(Member m) throws JSONException{
		JSONObject jMember = new JSONObject();
		jMember.put("simpleName", m.getSimpleName());
		jMember.put("color", m.getColor());
		jMember.put("roleName", m.getName());
		return jMember;
	}
	
	private void addOtherColors(JSONObject jo, Member m) throws JSONException{
		JSONArray jArray = new JSONArray();
		Member m2;
		ArrayList<String> seenColors = new ArrayList<String>();
		seenColors.add(m.getColor());
		for(Member rt: n.getAllRoles().getMembers()){	
			m2 = (Member) rt;
			if(m2.getSimpleName().equals(m.getSimpleName()) && !seenColors.contains(m2.getColor())){
				seenColors.add(m2.getColor());
				
				jArray.put(packMember(m2));
			}	
		}
		
		if(jArray.length() > 0){
			jo.put("other", jArray);
		}
	}
	
	private void addJFactions(JSONObject state) throws JSONException{
		JSONArray fMembers, blacklisted, allies, enemies, factionNames = new JSONArray(), availableClasses;
		JSONObject jFaction, jRT, allyInfo, jFactions = new JSONObject();
		ArrayList<String> availableClassesBacker;
		ArrayList<String> teamsWithoutVisibleFactions = new ArrayList<>();
		for(Faction f: fManager.factions){
			jFaction = new JSONObject();
			fMembers = new JSONArray();
			blacklisted = new JSONArray();
			availableClasses = new JSONArray();
			availableClassesBacker = new ArrayList<>();
		
			jFaction.put("name", f.getName());
			factionNames.put(f.getName());
			jFaction.put("color", f.getColor());
			jFaction.put("description", f.getDescription());
			jFaction.put("isEditable", f.isEditable);
			
			
			
			
			for(RoleTemplate rt: f.members){
				jRT = new JSONObject();
				jRT.put("name", rt.getName());
				jRT.put("description", rt.getDescription());
				jRT.put("color", rt.getColor());
				if(fManager.getFaction(rt.getColor()) == null && !teamsWithoutVisibleFactions.contains(rt.getColor())){
					teamsWithoutVisibleFactions.add(rt.getColor());
				}
				jRT.put("rules", new JSONArray(rt.getRuleIDs()));
				for(String class_name: rt.getClasses()){
					if(!availableClassesBacker.contains(class_name)){
						availableClassesBacker.add(class_name);
						availableClasses.put(class_name);
					}
				}
				jRT.put("class_type", rt.getClasses());
				if(!rt.isRandom()){
					jRT.put("simpleName", ((Member) rt).getSimpleName());
					if(n.isInProgress()){
						addOtherColors(jRT, (Member) rt);
						addRuleTexts(jRT, (Member) rt);
					}
				}else{
					addMembersToJRandomRole(jRT, (RandomMember) rt);
				}
				jFactions.put(rt.getName() + rt.getColor(), jRT);
				fMembers.put(jRT);
			}
			jFaction.put("class_names", availableClasses);
			jFaction.put("members", fMembers);
			for(Member rt: f.unavailableRoles){
				jRT = new JSONObject();
				jRT.put("name", rt.getName());
				jRT.put("simpleName", rt.getSimpleName());
				blacklisted.put(jRT);
			}
			jFaction.put("blacklisted", blacklisted);
			
			if(!n.isStarted())
				jFaction.put("rules", f.getRules());
			else
				jFaction.put("rules", Faction.getRules(f.getTeam(), n));
			
			jFactions.put(f.getName(), jFaction);
			jFactions.put(f.getColor(), jFaction);
			
			Team fTeam = f.getTeam();
			if(fTeam == null)
				continue;

			allies = new JSONArray();
			enemies = new JSONArray();
			for(Team t: n.getAllTeams()){
				if(t.getName().equals(Constants.A_SKIP))
					continue;
				if(t == fTeam)
					continue;
				allyInfo = new JSONObject();
				allyInfo.put("color", t.getColor());
				allyInfo.put("name", t.getName());
				if(t.isEnemy(fTeam))
					enemies.put(allyInfo);
				else
					allies.put(allyInfo);
			}
			jFaction.put("allies", allies);
			jFaction.put("enemies", enemies);
			
		}
		Team forgottenTeam;
		ArrayList<String> seenRoles;
		for(String teamColor: teamsWithoutVisibleFactions){
			forgottenTeam = n.getTeam(teamColor);
			if(forgottenTeam == null){
				continue;
			}

			jFaction = new JSONObject();
			jFaction.put("name", forgottenTeam.getName());
			jFaction.put("color", forgottenTeam.getColor());
			jFaction.put("description", forgottenTeam.getDescription());
			jFaction.put("rules", Faction.getRules(forgottenTeam, n));
			enemies = new JSONArray();
			for(Team t: n.getAllTeams()){
				if(t.getName().equals(Constants.A_SKIP))
					continue;
				if(t == forgottenTeam)
					continue;
				allyInfo = new JSONObject();
				allyInfo.put("color", t.getColor());
				allyInfo.put("name", t.getName());
				if(t.isEnemy(forgottenTeam))
					enemies.put(allyInfo);
			}
			jFaction.put("enemies", enemies);
			
			fMembers = new JSONArray();
			seenRoles = new ArrayList<>();
			for(Member m: n.getAllRoles().getMembers()){
				if(m.getColor().equals(teamColor) && !seenRoles.contains(m.getName())){
					seenRoles.add(m.getName());
					fMembers.put(packMember(m));
				}
			}
			jFaction.put("members", fMembers);

			jFactions.put(teamColor, jFaction);
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
	
	private void addJVotes(JSONObject state) throws JSONException{
		if(!n.isDay())
			return;
		JSONArray voteCounts = new JSONArray(), voters, finalScore = new JSONArray();
		
		JSONObject dayRecap, finalScoreObject;
		PlayerList playersVotingX, votingPlayers;
		for(int i = 1; i <= n.getDayNumber(); i++){
			dayRecap = new JSONObject();
			
			finalScore = new JSONArray();
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
			dayRecap.put(StateObject.finalScore, finalScore);
			
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
					votes = n.getLivePlayers().remove(p);
				else
					votes = n.getAllPlayers().remove(p);
				if(p.isDead())
					votes.clear();
				JSONArray names = getJPlayerArray(votes, p.getVoteTarget(), "Vote");
				playerLists.put("Vote", names);
				playerLists.getJSONArray(StateObject.type).put("Vote");
				if(p.hasDayAction()){
					PlayerList acceptableTargets;
					for(String s_ability: p.getDayAbility()){
						int ability = p.parseAbility(s_ability);
						acceptableTargets = p.getAcceptableTargets(ability);
						if(acceptableTargets.size() == 1 && acceptableTargets.getFirst() == p)
							continue;
						if(acceptableTargets.isEmpty())
							continue;
						names = getJPlayerArray(acceptableTargets, new PlayerList[]{p.getTargets(ability)}, s_ability);
						playerLists.put(s_ability, names);
						playerLists.getJSONArray(StateObject.type).put(s_ability);
					}
				}
			}else{
				if(n.isInProgress()){
					String[] abilities = p.getAbilities();
					ArrayList<String> possibleOptions;
					for(String s_ability: abilities){
						int ability = p.parseAbility(s_ability);
						PlayerList acceptableTargets = p.getAcceptableTargets(ability);
						JSONArray names;
						if(ability == Role.MAIN_ABILITY && p.is(Spy.class)){
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
	public static final String cancelAction = "cancelAction";
	
	public static final String voteCounts   = "voteCounts";
	public static final String finalScore   = "finalScore";
}
