package voss.shared.logic.templates;

import voss.shared.logic.Member;
import voss.shared.logic.support.Constants;
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
import voss.shared.roles.SerialKiller;
import voss.shared.roles.Sheriff;
import voss.shared.roles.Veteran;
import voss.shared.roles.Vigilante;
import voss.shared.roles.Witch;

public class BasicRoles {

	public static Member Citizen(){
		return new Member(Citizen.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Doctor(){
		return new Member(Doctor.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Sheriff(){
		return new Member(Sheriff.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Lookout() {
		return new Member(Lookout.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Detective() {
		return new Member(Detective.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Mafioso(){
		return new Member(Mafioso.ROLE_NAME, Constants.A_MAFIA);
	}
	
	public static Member Jester(){
		return new Member(Jester.ROLE_NAME, Constants.A_BENIGN);
	}
	
	public static Member SerialKiller(){
		return new Member(SerialKiller.ROLE_NAME, Constants.A_SK);
	}

	public static Member Janitor() {
		return new Member(Janitor.ROLE_NAME, Constants.A_MAFIA);
	}

	public static Member Blackmailer() {
		return new Member(Blackmailer.ROLE_NAME, Constants.A_MAFIA);
	}

	public static Member Executioner() {
		return new Member(Executioner.ROLE_NAME, Constants.A_BENIGN);
	}

	public static Member Agent() {
		return new Member(Agent.ROLE_NAME, Constants.A_MAFIA);
	}
	
	public static Member Consort() {
		return new Member(Consort.ROLE_NAME, Constants.A_MAFIA);
	}

	public static Member Witch() {
		return new Member(Witch.ROLE_NAME, Constants.A_OUTCASTS);
	}
	
	public static Member Chauffeur() {
		return new Member(Chauffeur.ROLE_NAME, Constants.A_MAFIA);
	}
	
	public static Member Chauffeur2() {
		return new Member(Chauffeur.ROLE_NAME, Constants.A_YAKUZA);
	}
	
	public static Member BusDriver() {
		return new Member(BusDriver.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Escort() {
		return new Member(Escort.ROLE_NAME, Constants.A_TOWN);
	}

	public static Member Mayor() {
		return new Member(Mayor.ROLE_NAME, Constants.A_TOWN);
	}

	public static Member Vigilante() {
		return new Member(Vigilante.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Framer() {
		return new Member(Framer.ROLE_NAME, Constants.A_MAFIA);
	}

	public static Member Bodyguard() {
		return new Member(Bodyguard.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member Veteran(){
		return new Member(Veteran.ROLE_NAME, Constants.A_TOWN);
	}
	
	public static Member MassMurderer(){
		return new Member(MassMurderer.ROLE_NAME, Constants.A_MM);
	}
	
	public static Member Cultist(){
		return new Member(Cultist.ROLE_NAME, Constants.A_CULT);
	}
	
	public static Member CultLeader(){
		return new Member(CultLeader.ROLE_NAME, Constants.A_CULT);
	}

	public static Member Arsonist() {
		return new Member(Arsonist.ROLE_NAME, Constants.A_ARSONIST);
	}

	public static Member Godfather() {
		return new Member(Godfather.ROLE_NAME, Constants.A_MAFIA);
	}
}
