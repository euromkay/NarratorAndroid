package voss.shared.logic;

import java.util.ArrayList;

import voss.shared.packaging.Packager;


public class EventManager {

	private ArrayList<Event> events = new ArrayList<>();
	
	public EventManager(){}
	
	public synchronized Event add(Event e) {
		events.add(e);
		return e;
	}


	public ArrayList<Event> getEvents(){
		return events;
	}


	public void writeToPackage(Packager dest) {
		ArrayList<Event> list = new ArrayList<>();
		for(Event e: events){
			String event = e.getCommand();
			if(event.length() != 0)
				list.add(e);
		}
		dest.write(list.size());
		for(Event e: list){
			dest.write(e.getCommand());
		}
		
	}

	public synchronized String access(String level, boolean html){
		StringBuilder sb = new StringBuilder();
		for (Event e: events)
			sb.append(e.access(level, html));
		return sb.toString();
	}

	public boolean equals(Object o){
		if (o == null)
			return false;
		if (!(o instanceof EventManager))
			return false;
		EventManager em = (EventManager) o;

		if(events.size() != em.events.size()){
			return false;
		}
		
		for(int i = 0; i < events.size(); i++){
			Event e1 = em.events.get(i);
			Event e2 = events.get(i);
			if(!e1.equals(e2))
				return false;
		}
		return  true;
	}


	public ArrayList<String> getCommands() {
		ArrayList<String> list = new ArrayList<String>();
		String comm;
		for(Event e: events){
			comm = e.getCommand();
			if(comm.length() > 0)
				list.add(comm);
		}
		return list;
	}

	

}
