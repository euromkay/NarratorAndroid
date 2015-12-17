package voss.shared.logic;

import java.util.ArrayList;

import voss.shared.logic.exceptions.IllegalActionException;
import voss.shared.logic.listeners.CommandListener;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.Equals;
import voss.shared.logic.support.StringChoice;


public class Event {
	
	public static final int DAY_LABEL = 0;
	public static final int JESTER_CHANGE = 1;
    public static final int DEATH_DESCRIPTION  = 2;//" changed into a Jester."
    public static final int CULT_CHANGE = 3; //TODO CHECK IF USED
	
    //VISIBLITY
	public static final String PRIVATE = Constants.PRIVATE;
	public static final String PUBLIC = Constants.PUBLIC;
    
	public static int NORMAL_EVENT = 0;
	public static int PLAYER_EVENT = 1;

	
	private ArrayList<String> access;
	private ArrayList<Object> eventParts;
	
	public Event(){
		eventParts = new ArrayList<>();
		access = new ArrayList<>();
	}

	public String toString(){
		boolean b = showInPrivate;
		showInPrivate = true;
		String m  = access(Event.PRIVATE, false);
		showInPrivate = b;
		return m;
	}

	public String access(Player p, boolean html){
		return access(p.getName(), html);
	}

	public String access(String level, boolean html){
		StringBuilder sb = new StringBuilder("");
		boolean first = true;
		if ((level.equals(PRIVATE) && showInPrivate) || access.contains(level) || isPublic()){
			for(Object o: eventParts){
				if(o instanceof Player){
					accessHelper((Player) o, sb, first, level, html);
				} else if (o instanceof StringChoice){
					StringChoice sc = (StringChoice) o;
					sb.append(sc.getString(level));
				}
				else if (o instanceof PlayerList){
					for (Player p: (PlayerList) o){
						accessHelper(p, sb, first, level, html);
						sb.append(", ");
					}
					sb.deleteCharAt(sb.length()-1);
					sb.deleteCharAt(sb.length()-1);
				}else{
					sb.append(o.toString());
				}
				first = false;
			}
			if(sb.length() > 2)
				sb.append("\n");
		}
		return sb.toString();
	}
	
	private void accessHelper(Player p, StringBuilder sb, boolean first, String level, boolean html){
		if (first && level == p.getName()){
			sb.append("You");
		}else if (level == p.getName()){
			sb.append("yourself");
		}else if(html){
			sb.append(toHTML(p));
		}else if(level == PRIVATE || !p.getNarrator().isInProgress()){
			sb.append(p);
		}else{
			sb.append(p.getDescription());
		}
	}
	

	public boolean isPublic(){
		return access.isEmpty();
	}

	
	
	public void setPrivate(){
		access.add(PRIVATE);
	}

	public void setVisibility(String v){
		access.add(v);
	}
	public void setVisibility(Player p){
		access.add(p.getName());
	}

	public void setVisibility(PlayerList list){
		for(Player p: list){
			setVisibility(p);
		}
	}
	
	public void add(Object part){
		if(part == null)
			throw new NullPointerException();
		eventParts.add(part);
	}
	
	public void add(Object ... objects) {
		for(Object o: objects)
			add(o);
	}
	
	
	public boolean isEndGameDisplay(Player requester){
		return requester != null && requester.getSkipper() == requester;	
	}
	
	public static String toHTML(Player p){
		int color = p.getAlignment();
		if (!p.getNarrator().isStarted()) 
			color = Integer.parseInt("FFFFFF", 16);
		else if((p.isAlive() || p.isCleaned()) && p.getNarrator().isInProgress())
			color = Integer.parseInt("FFFFFF", 16);
		//String red   = toHex(Color.red(color));
		//String blue  = toHex(Color.blue(color));
		//String green = toHex(Color.green(color));
		String hexColor = Integer.toHexString(color);//apparently there are 2 ffs
		while(hexColor.length() < 6){
			hexColor = "0" + hexColor;
		}
		while(hexColor.length() > 6){
			hexColor = hexColor.substring(1);
		}

		return "<font color = #" + hexColor + ">" +p.getDescription() + "</font>";
	}
	
	public String toHex(int i){
		String s = Integer.toHexString(i);
		if (s.length() == 1)
			return "0" + s;
		return s;
	}
	
	
	
	
	
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(!(o instanceof Event))
			return false;
		
		Event e = (Event) o;
		if (Equals.notEqual(e.access, access))
			return false;
		if(showInPrivate != e.showInPrivate)
			return false;
		if(eventParts.size() !=  e.eventParts.size())
			return false;
		for (int i = 0; i < eventParts.size(); i++){
			Object o1 = eventParts.get(i);
			Object o2 = eventParts.get(i);
			if(o2 instanceof Player && o1 instanceof Player){
				if (((Player) o1).getName() == ((Player) o2).getName())
					continue;
			}
			if(o2 instanceof PlayerList && o1 instanceof PlayerList){
				if (o1.equals(o2))
					continue;
			}
			if(o2 instanceof String && o1 instanceof String){
				if (o1.equals(o2))
					continue;
			}
			return false;
		}
		if (Equals.notEqual(command, e.command))
			return false;
		
		
		return true;
	}


	
	private boolean showInPrivate = true; 
	public void dontShowPrivate() {
		showInPrivate = false;
		
	}

	
	private String command = "";
	public void setCommand(Player p, String ... command) {
		Narrator n = p.getNarrator();
		String name = p.getName();
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append(Constants.NAME_SPLIT);
		for(String s : command){
			sb.append(s + " ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("\n");
		this.command = sb.toString();

		if(getCommand().length() != 0)
			synchronized (n.commandLock){
				for(CommandListener cl: n.getCommandListeners()){
					cl.onCommand(getCommand());
				}
			}
		
	}
	
	public boolean hasCommand(){
		return command.length() != 0;
	}

	public String getCommand() {
		return command;
	}
}
