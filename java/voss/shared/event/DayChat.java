package voss.shared.event;

public class DayChat extends EventLog{

	private int day;
	public DayChat(int day){
		this.day = day;
	}
	public String getHeader(boolean html){
		if(day == 0)
			return "";
		String ret = "Day " + day;
		
		if(html){
			ret = UnderLine(ret);
		}
		
		ret = "\n" + ret;
		ret += "\n\n";
		return ret;
	}
	
	public boolean hasAccess(String name){
		return true;
	}
}
