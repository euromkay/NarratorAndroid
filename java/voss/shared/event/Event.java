package voss.shared.event;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.listeners.CommandListener;
import voss.shared.logic.support.Constants;
import voss.shared.logic.support.Equals;
import voss.shared.logic.support.HTString;
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
	public Event(Object s){
		this();
		add(s);
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
		if ((level.equals(PRIVATE) && showInPrivate) || access.contains(level) || isPublic()){
			for(Object o: eventParts){
				if(o instanceof HTString){
					HTString ht = (HTString) o;
					sb.append(ht.access(html));
				} else if(o instanceof Player){
					accessHelper((Player) o, sb, level, html);
				} else if (o instanceof StringChoice){
					StringChoice sc = (StringChoice) o;
					Object object = sc.getString(level);
					if(object instanceof String)
						sb.append(object.toString());
					else
						accessHelper((Player) object, sb, level, html);
				}
				else if (o instanceof PlayerList){
					for (Player p: (PlayerList) o){
						accessHelper(p, sb, level, html);
						sb.append(", ");
					}
					sb.deleteCharAt(sb.length()-1);
					sb.deleteCharAt(sb.length()-1);
				}else{
					sb.append(o.toString());
				}
			}
			if(sb.length() > 2)
				sb.append("\n");
		}
		return sb.toString();
	}
	
	private void accessHelper(Player p, StringBuilder sb, String level, boolean html){
		if(html){
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

	
	//because access is no longer empty, you need a key to access this event's contents.  setting private and adding someone is redundant.
	public void setPrivate(){
		access.add(PRIVATE);
	}

	public void setVisibility(String v){
		access.add(v);
	}
	public Event setVisibility(Player p){
		access.add(p.getName());
		return this;
	}
	
	public Event add(Object part){
		if(part == null)
			throw new NullPointerException();
		eventParts.add(part);
		return this;
	}
	
	public Event add(Object ... objects) {
		for(Object o: objects)
			add(o);
		return this;
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
		String hexColor = ToHex(color);
		return WrapHTML(p.getDescription(), hexColor);
	}
	public static String WrapHTML(String s, String color){

		return "<font color = #" + color + ">" + s + "</font>";
	}
	public static String ToHex(int color){
		String hexColor = Integer.toHexString(color);//apparently there are 2 ffs
		while(hexColor.length() < 6){
			hexColor = "0" + hexColor;
		}
		while(hexColor.length() > 6){
			hexColor = hexColor.substring(1);
		}
		return hexColor;
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
	public Event dontShowPrivate() {
		showInPrivate = false;
		return this;
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
	public static Event StringFeedback(String feedback, Player player) {
		return new Event(feedback).dontShowPrivate().setVisibility(player);
	}
	public ArrayList<HTString> getHTStrings() {
		ArrayList<HTString> hts = new ArrayList<HTString>();
		for(Object o: eventParts){
			if(o.getClass() == HTString.class)
				hts.add((HTString) o);
		}
		return hts;
	}
	public static void AddSelectionFeedback(Event e, Player owner) {
		EventLog el;
		Narrator n = owner.getNarrator();
		if(owner.getTeam().knowsTeam()){
			el = n.getEventManager().getNightLog(owner.getTeam().getName(), n.getDayNumber());
		}else{
			el = n.getEventManager().getNightLog(null,  n.getDayNumber());
			e.setVisibility(owner);
		}
		el.add(e);
	}
	public PlayerList getPlayers() {
		PlayerList pList = new PlayerList();
		for(Object o: eventParts){
			Class<?> c = o.getClass();
			if(c == Player.class)
				pList.add((Player) o);
			else if(c == PlayerList.class)
				pList.add((PlayerList) o);
		}
		
		return pList;
	}
}
