package voss.shared.event;

import java.util.ArrayList;
import java.util.HashMap;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;


public class EventManager {
	
	
	private ArrayList<DayChat> dayChats = new ArrayList<>();
	private ArrayList<HashMap<String, NightChat>> nightChats = new ArrayList<>();
	
	private boolean nightStart;
	private Narrator n;
	public EventManager(Narrator n){
		this.n = n;
	}
	public void setPhaseStart(boolean day){
		nightStart = !day;
	}
	
	public HashMap<String, ArrayList<Event>> nightParts = new HashMap<>(); 
	
	public DayChat getDayChat(int dayNumber){
		if(dayChats.size() <= dayNumber){
			dayChats.add(new DayChat(dayNumber));
			return getDayChat(dayNumber);
		}
		return dayChats.get(dayNumber);
	}

	/*public ArrayList<Event> getEvents(){
		return events;
	}*/
	
	public synchronized String getPublic(boolean html){
		return getEvents(Event.PUBLIC, html);
	}

	public String getPlayerEvents(Player p, boolean html) {
		StringBuilder sb = new StringBuilder();
		
		int size = dayChats.size() > nightChats.size() ? dayChats.size() : nightChats.size();
		
		for(int day = 0; day < size; day++){
			sb.append(dayChats.get(day).access(p.getName(), html));
			if(nightChats.size() > day)
				sb.append(accessNightChats(p.getName(), day, html));		
			
		}
		
		
		return sb.toString();
	}
	
	public synchronized String getEvents(String key, boolean html){
		StringBuilder sb = new StringBuilder();
		
		int size = dayChats.size() > nightChats.size() ? dayChats.size() : nightChats.size();
		
	
		for(int day = 0; day < size; day++){
			sb.append(dayChats.get(day).access(key, html));
			if(nightChats.size() > day)
				sb.append(accessNightChats(key, day, html));		
			
		}
		
		
		return sb.toString();
	}
	
	public synchronized String getPrivate(boolean html){
		return getEvents(Event.PRIVATE, html);
	}
	
	
	
	public String accessNightChats(String nameKey, int dayNumber, boolean html){
		HashMap<String, NightChat> nightChats = this.nightChats.get(dayNumber);
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		
		String ret = "Night " + dayNumber;
		if(html)
			ret = EventLog.UnderLine(ret);
		ret += "\n";
		sb.append(ret);
		
		for(NightChat em: nightChats.values()){
			if(em.getTeam() == null)
				continue;
			if(em.hasAccess(nameKey))
				sb.append(em.access(nameKey, html)); // private?...
		}
		if(nightChats.get(null) != null)
			sb.append(nightChats.get(null).access(nameKey, html));
		return sb.toString();
	}
	
	/*public synchronized String access(String level, boolean html){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < dayChats.size() + 1; i++){
			if(nightStart){
				for(NightChat nc: getNightChat())
			}
		}
		for (Event e: events)
			sb.append(e.access(level, html));
		return sb.toString();
	}*/

	


	public ArrayList<String> getCommands() {
		ArrayList<String> list = new ArrayList<String>();
		
		int day;
		if(nightStart)
			day = 0;
		else
			day = 1;
		for(; day < dayChats.size(); day++){
			if(day != 0)
				commandExtractor(dayChats.get(day), list);
			if(nightChats.size() > day){
				for(NightChat eLog: nightChats.get(day).values()){
					if(eLog.getTeam() != null)
						commandExtractor(eLog, list);
				}
				commandExtractor(nightChats.get(day).get(null), list);
			}
			
		}
		return list;
	}
	private void commandExtractor(EventLog eLog, ArrayList<String> list){
		String comm;
		for(Event e: eLog.getEvents()){
			comm = e.getCommand();
			if(comm.length() != 0)
				list.add(comm);
		}
	}

	public EventLog getNightLog(String key, int i) {
		if(nightChats.size() <= i){
			nightChats.add(new HashMap<String, NightChat>());
			return getNightLog(key, i);
		}
		EventLog el = nightChats.get(i).get(key);
		if(el != null)
			return el;
		if(key == null){
			nightChats.get(i).put(null, new NightChat(null, i));
			return getNightLog(key, i);
		}
		Team k = null;
		for(Team t: n.getAllTeams()){
			if(t.getName().equals(key)){
				k = t;
				break;
			}
		}
		nightChats.get(i).put(k.getName(), new NightChat(k, i));
		return getNightLog(key, i);
	}

	

}
