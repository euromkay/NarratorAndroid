package voss.shared.event;

import java.util.ArrayList;

public abstract class EventLog {

	public abstract String getHeader(boolean html);
	
	public boolean hasAccess(String name) {
		return name.equals(Event.PRIVATE);
	}


	private ArrayList<Event> events = new ArrayList<>();
	public String access(String access, boolean html){
		if(!hasAccess(access))
			return "";
		
		StringBuilder sb = new StringBuilder();
		sb.append("\n");
		sb.append(getHeader(html));
		for(Event e: events)
			sb.append(e.access(access, html));
		
		return sb.toString();
	}
	
	public void add(Event e) {
		events.add(e);
	}

	public ArrayList<Event> getEvents() {
		return events;
	}
	
	public static String UnderLine(String ret){
		return "<u>" + ret + "</u>";
	}
}
