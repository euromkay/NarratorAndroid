package voss.shared.roles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import voss.shared.logic.Member;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.IllegalRoleCombinationException;
import voss.shared.logic.support.Alignment;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.Equals;
import voss.shared.logic.support.RolePackage;
import voss.shared.logic.support.RoleTemplate;
import voss.shared.packaging.Packager;

public class RandomRole extends RoleTemplate implements Alignment{
	
	private String name;
	
	public RandomRole(String name, int color){
		this.name = name;
		this.color = color;
	}
	public String getName(){
		return name;
	}

	private int color;
	
	private HashMap<String, Member> list = new HashMap<String, Member>();
	
	public void addMember(Member m){
		if(m.getWeight() == 0)
			return;
		

		//if this is a new team being added
		if(!canRandomTeam(m.getColor()))
			alignmentCount++;

		
		
		list.put(m.toString(), m);
	}
	public void removeMember(Member m){

		list.remove(m.getName());
		if(!canRandomTeam(m.getColor()))
			alignmentCount--;
	}
	public boolean spawns(Member name){
		return list.containsKey(name.toString());
	}
	
	private int alignmentCount = 0;
	public boolean hasMultiAlignment() {
		return alignmentCount > 1;
	}
	
	public boolean canRandomTeam(int team){
		for(Member m: list.values()){
			if(m.getColor() == team)
				return true;
		}
		return false;
	}
	public int getSize() {
		return list.size();
	}
	
	public int getAlignment(){
		if(hasMultiAlignment())
			throw new Error("Has multiple alignments");
		else
			return list.values().iterator().next().getColor();
	}
	
	
	
	public RolePackage getRole(Player p){
		if(list.size() == 0)
			throw new IllegalRoleCombinationException("No roles in this random slot");
		Narrator n = p.getNarrator();
		ArrayList<Member> rList = new ArrayList<>();
		for(Member m: list.values()){
			if(m.getName().equals(Mayor.ROLE_NAME) && n.hasRole(Mayor.ROLE_NAME, m.getColor()))
				continue;
			if(m.getName().equals(Godfather.ROLE_NAME) && n.hasRole(Godfather.ROLE_NAME, m.getColor()))
				continue;
			if(m.getName().equals(CultLeader.ROLE_NAME) && n.hasRole(CultLeader.ROLE_NAME, m.getColor()))
				continue;
			
			for (int i = 0; i < m.getWeight(); i++)
				rList.add(m);
		}

		int choice = n.getRandom().nextInt(rList.size());
		Member member =  rList.get(choice);
		
			
		Role r = Role.CREATOR(member.getName(), p);
		return new RolePackage(r, member.getColor());
		
	}
	
	
	
	
	
	
	public boolean opposes(Alignment a2, Narrator n){
		for(Member m: list.values()){
			Team myTeam = n.getTeam(m.getColor());
			
			int[] enemyNumber = a2.getTeams();
			
			for(int theirTeam: enemyNumber)
				if(!myTeam.isEnemy(theirTeam))
					return false;
		}
		
		return true;
			
	}
	
	public int[] getTeams(){
		LinkedHashSet<Integer> teams = new LinkedHashSet<Integer>();
		for(Member m: list.values())
			teams.add(m.getColor());
			
		int[] returnVal = new int[teams.size()];
		Object[] array = teams.toArray();
		for(int i = 0; i < teams.size(); i++)
			returnVal[i] = (Integer) array[i];
		
		return returnVal;
	}
	
	
	
	
	
	
	private static final int 
	CITIZEN_WEIGHT = 1,
	
	SHERIFF_WEIGHT = 5,
	LOOKOUT_WEIGHT = 2,
	DETECTIVE_WEIGHT = 2,
	
	DOCTOR_WEIGHT = 2,
	BUSDRIVER_WEIGHT = 1,
	ESCORT_WEIGHT = 2,
	BODYGUARD_WEIGHT = 2,
	
	VETERAN_WEIGHT = 2,
	VIGILANTE_WEIGHT = 3,
	
	MAYOR_WEIGHT = 1,
	
	MAFIOSO_WEIGHT = 3,
	JANITOR_WEIGHT = 1,
	BLACKMAILER_WEIGHT = 1,
	AGENT_WEIGHT = 1,
	FRAMER_WEIGHT = 2,
	CHAUFFEUR_WEIGHT = 1,
	CONSORT_WEIGHT = 1,
	GF_WEIGHT = 2,
	
	
	SK_WEIGHT = 3,
	ARSON_WEIGHT = 4,
	MM_WEIGHT = 5,
	
	JESTER_WEIGHT = 4, 
	EXECUTIONER_WEIGHT = 3,
	
	CULTIST_WEIGHT = 2,
	CULTIST_LEADER_WEIGHT = 1,
	WITCH_WEIGHT = 4;
	
	
	
	public static RandomRole TownRandom(){
		RandomRole rm = new RandomRole(Constants.TOWN_RANDOM_ROLE_NAME, Constants.A_TOWN);
		rm.addMember(new Member(Citizen.ROLE_NAME,   Constants.A_TOWN, CITIZEN_WEIGHT));
		rm.addMember(new Member(Sheriff.ROLE_NAME,   Constants.A_TOWN, SHERIFF_WEIGHT));
		rm.addMember(new Member(Doctor.ROLE_NAME,    Constants.A_TOWN, DOCTOR_WEIGHT));
		rm.addMember(new Member(Lookout.ROLE_NAME,   Constants.A_TOWN, LOOKOUT_WEIGHT));
		rm.addMember(new Member(Detective.ROLE_NAME, Constants.A_TOWN, DETECTIVE_WEIGHT));
		rm.addMember(new Member(BusDriver.ROLE_NAME, Constants.A_TOWN, BUSDRIVER_WEIGHT));
		rm.addMember(new Member(Escort.ROLE_NAME,    Constants.A_TOWN, ESCORT_WEIGHT));
		rm.addMember(new Member(Vigilante.ROLE_NAME, Constants.A_TOWN, VIGILANTE_WEIGHT));
		rm.addMember(new Member(Mayor.ROLE_NAME,     Constants.A_TOWN, MAYOR_WEIGHT));
		rm.addMember(new Member(Bodyguard.ROLE_NAME, Constants.A_TOWN, BODYGUARD_WEIGHT));
		rm.addMember(new Member(Veteran.ROLE_NAME,   Constants.A_TOWN, VETERAN_WEIGHT));
		
		return rm;
	}
	
	
	public static RandomRole TownInvestigative(){
		RandomRole rm = new RandomRole(Constants.TOWN_INVESTIGATIVE_ROLE_NAME, Constants.A_TOWN);
		rm.addMember(new Member(Sheriff.ROLE_NAME,   Constants.A_TOWN, SHERIFF_WEIGHT));
		rm.addMember(new Member(Lookout.ROLE_NAME,   Constants.A_TOWN, LOOKOUT_WEIGHT));
		rm.addMember(new Member(Detective.ROLE_NAME, Constants.A_TOWN, DETECTIVE_WEIGHT));
		
		return rm;
	} 
	public static RandomRole TownProtective(){
		RandomRole rm = new RandomRole(Constants.TOWN_PROTECTIVE_ROLE_NAME, Constants.A_TOWN);
		rm.addMember(new Member(Doctor.ROLE_NAME,    Constants.A_TOWN, DOCTOR_WEIGHT));
		rm.addMember(new Member(BusDriver.ROLE_NAME, Constants.A_TOWN, BUSDRIVER_WEIGHT));
		rm.addMember(new Member(Escort.ROLE_NAME,    Constants.A_TOWN, ESCORT_WEIGHT));
		rm.addMember(new Member(Bodyguard.ROLE_NAME, Constants.A_TOWN, BODYGUARD_WEIGHT));
		
		
		return rm;
	} 
	
	public static RandomRole TownKilling() {
		RandomRole rm = new RandomRole(Constants.TOWN_KILLING_ROLE_NAME, Constants.A_TOWN);
		rm.addMember(new Member(Vigilante.ROLE_NAME, Constants.A_TOWN, VIGILANTE_WEIGHT));
		rm.addMember(new Member(Bodyguard.ROLE_NAME, Constants.A_TOWN, BODYGUARD_WEIGHT));
		rm.addMember(new Member(Veteran.ROLE_NAME,   Constants.A_TOWN, VETERAN_WEIGHT));
		
		
		return rm;
	}
	
	public static RandomRole TownGovernment(){
		RandomRole rm = new RandomRole(Constants.TOWN_GOVERNMENT_ROLE_NAME, Constants.A_TOWN);
		rm.addMember(new Member(Mayor.ROLE_NAME, Constants.A_TOWN, MAYOR_WEIGHT));
		
		
		return rm;
	}
	
	public static RandomRole MafiaRandom(){
		return randomMaf(Constants.MAFIA_RANDOM_ROLE_NAME, Constants.A_MAFIA);
	}
	
	private static RandomRole randomMaf(String roleName, int team){
		RandomRole rm = new RandomRole(roleName, team);
		rm.addMember(new Member(Mafioso.ROLE_NAME,     team, MAFIOSO_WEIGHT));
		rm.addMember(new Member(Janitor.ROLE_NAME,     team, JANITOR_WEIGHT));
		rm.addMember(new Member(Agent.ROLE_NAME,       team, AGENT_WEIGHT));
		rm.addMember(new Member(Consort.ROLE_NAME,     team, CONSORT_WEIGHT));
		rm.addMember(new Member(Blackmailer.ROLE_NAME, team, BLACKMAILER_WEIGHT));
		rm.addMember(new Member(Chauffeur.ROLE_NAME,   team, CHAUFFEUR_WEIGHT));
		rm.addMember(new Member(Framer.ROLE_NAME,      team, FRAMER_WEIGHT));
		rm.addMember(new Member(Godfather.ROLE_NAME,   team, GF_WEIGHT));
		
		return rm;
	}

	public static RandomRole YakuzaRandom(){
		return randomMaf(Constants.YAKUZA_RANDOM_ROLE_NAME, Constants.A_YAKUZA);
	}
	
	public static RandomRole NeutralRandom(){
		RandomRole rm = new RandomRole(Constants.NEUTRAL_RANDOM_ROLE_NAME, Constants.A_NEUTRAL);
		rm.addMember(new Member(Executioner.ROLE_NAME, Constants.A_BENIGN, EXECUTIONER_WEIGHT));
		rm.addMember(new Member(Jester.ROLE_NAME, Constants.A_BENIGN, JESTER_WEIGHT));
		
		rm.addMember(new Member(Witch.ROLE_NAME, Constants.A_OUTCASTS, WITCH_WEIGHT));
		rm.addMember(new Member(Cultist.ROLE_NAME, Constants.A_CULT, CULTIST_WEIGHT));
		rm.addMember(new Member(CultLeader.ROLE_NAME, Constants.A_CULT, CULTIST_LEADER_WEIGHT));
		
		rm.addMember(new Member(MassMurderer.ROLE_NAME, Constants.A_MM, MM_WEIGHT));
		rm.addMember(new Member(SerialKiller.ROLE_NAME, Constants.A_SK, SK_WEIGHT));
		rm.addMember(new Member(Arsonist.ROLE_NAME, Constants.A_ARSONIST, ARSON_WEIGHT));
		
		
		return rm;
	}
	
	public static RandomRole NeutralEvilRandom() {
		RandomRole rm = new RandomRole(Constants.NEUTRAL_RANDOM_ROLE_NAME, Constants.A_NEUTRAL);
		rm.addMember(new Member(Witch.ROLE_NAME, Constants.A_OUTCASTS, WITCH_WEIGHT));
		rm.addMember(new Member(Cultist.ROLE_NAME, Constants.A_CULT, CULTIST_WEIGHT));
		rm.addMember(new Member(CultLeader.ROLE_NAME, Constants.A_CULT, CULTIST_LEADER_WEIGHT));
		rm.addMember(new Member(MassMurderer.ROLE_NAME, Constants.A_MM, MM_WEIGHT));
		rm.addMember(new Member(SerialKiller.ROLE_NAME, Constants.A_SK, SK_WEIGHT));
		rm.addMember(new Member(Arsonist.ROLE_NAME, Constants.A_ARSONIST, ARSON_WEIGHT));
		
		return rm;
	}
	
	public static RandomRole AnyRandom(){
		RandomRole rm = new RandomRole(Constants.ANY_RANDOM_ROLE_NAME, Constants.A_RANDOM);
		rm.addMember(new Member(Citizen.ROLE_NAME, Constants.A_TOWN, CITIZEN_WEIGHT));
		rm.addMember(new Member(Sheriff.ROLE_NAME, Constants.A_TOWN, SHERIFF_WEIGHT));
		rm.addMember(new Member(Doctor.ROLE_NAME, Constants.A_TOWN, DOCTOR_WEIGHT));
		rm.addMember(new Member(Detective.ROLE_NAME, Constants.A_TOWN, DETECTIVE_WEIGHT));
		rm.addMember(new Member(Lookout.ROLE_NAME, Constants.A_TOWN, LOOKOUT_WEIGHT));
		rm.addMember(new Member(BusDriver.ROLE_NAME, Constants.A_TOWN, BUSDRIVER_WEIGHT));
		rm.addMember(new Member(Escort.ROLE_NAME, Constants.A_TOWN, ESCORT_WEIGHT));
		rm.addMember(new Member(Vigilante.ROLE_NAME, Constants.A_TOWN, VIGILANTE_WEIGHT));
		rm.addMember(new Member(Mayor.ROLE_NAME, Constants.A_TOWN, MAYOR_WEIGHT));
		rm.addMember(new Member(Veteran.ROLE_NAME, Constants.A_TOWN, VETERAN_WEIGHT));

		rm.addMember(new Member(Agent.ROLE_NAME, Constants.A_MAFIA, AGENT_WEIGHT));
		rm.addMember(new Member(Consort.ROLE_NAME, Constants.A_MAFIA, CONSORT_WEIGHT));
		rm.addMember(new Member(Mafioso.ROLE_NAME, Constants.A_MAFIA, MAFIOSO_WEIGHT));
		rm.addMember(new Member(Janitor.ROLE_NAME, Constants.A_MAFIA, JANITOR_WEIGHT));
		rm.addMember(new Member(Blackmailer.ROLE_NAME, Constants.A_MAFIA, BLACKMAILER_WEIGHT));
		rm.addMember(new Member(Chauffeur.ROLE_NAME, Constants.A_MAFIA, CHAUFFEUR_WEIGHT));
		rm.addMember(new Member(Framer.ROLE_NAME, Constants.A_MAFIA, FRAMER_WEIGHT));
		
		rm.addMember(new Member(Jester.ROLE_NAME, Constants.A_BENIGN, JESTER_WEIGHT));
		rm.addMember(new Member(Executioner.ROLE_NAME, Constants.A_BENIGN, EXECUTIONER_WEIGHT));
		
		rm.addMember(new Member(Witch.ROLE_NAME, Constants.A_OUTCASTS, WITCH_WEIGHT));
		
		rm.addMember(new Member(SerialKiller.ROLE_NAME, Constants.A_SK, SK_WEIGHT));
		rm.addMember(new Member(MassMurderer.ROLE_NAME, Constants.A_MM, MM_WEIGHT));
		rm.addMember(new Member(Arsonist.ROLE_NAME, Constants.A_ARSONIST, ARSON_WEIGHT));
		
		rm.addMember(new Member(Cultist.ROLE_NAME, Constants.A_CULT, CULTIST_WEIGHT));
		rm.addMember(new Member(CultLeader.ROLE_NAME, Constants.A_CULT, CULTIST_LEADER_WEIGHT));
		
		return rm;
	}
	
	public boolean equals(Object o){
		if (o == null)
			return false;
		if(o == this)
			return true;
		
		if(o.getClass() != getClass())
			return false;
		
		RandomRole r = (RandomRole) o;
		
		if(alignmentCount != r.alignmentCount)
			return false;
		if(Equals.notEqual(name, r.name))
			return false;
		if(Equals.notEqual(list, r.list))
			return false;
		if(color != r.color)
			return false;
		
		return true;
	}
	
	public RandomRole(Packager in){
		alignmentCount = in.readInt();
		color = in.readInt();
		
		int size = in.readInt();
		for(int i = 0; i < size; i++){
			String key = in.readString();
			Member m = new Member(in);
			list.put(key, m);
		}
		
		name = in.readString();

		
		
			
	}
	
	
	
	public int describeContents() {
		return 0;
	}
	
	public String toString(){
		return name;
	}
	public int getAlignmentCount() {
		return alignmentCount;
	}
	public int getColor() {
		return color;
	}
	public HashMap<String, Member> getList() {
		return list;
	}
	
	public void writeToPackage(Packager dest) {
		dest.write(alignmentCount);
		dest.write(color);
		
		dest.write(list.size());
		for(String s: list.keySet()){
			dest.write(s);
			list.get(s).writeToPackage(dest);
		}

		dest.write(name);
	}
	
	
	
}
