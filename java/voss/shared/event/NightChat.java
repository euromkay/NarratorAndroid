package voss.shared.event;

import voss.shared.logic.Team;

public class NightChat extends EventLog{
	
	private Team teamChat; 
	private int day;
	
	public NightChat(Team t, int day){
		teamChat = t;
		this.day = day;
	}

	public boolean hasAccess(String name) {
		if(teamChat == null)//signifies the general night chat
			return true;
		if(teamChat != null && teamChat.getMembers().getNamesToStringList().contains(name))
			return true;
		
		return super.hasAccess(name);
	}
	
	public String getHeader(boolean html){
		if(teamChat == null)
			return "";
		else
			return teamChat.getName() + " Chat\n";
	}

	public Team getTeam() {
		return teamChat;
	}
}
