package voss.shared.logic.support;

import voss.shared.logic.Event;
import voss.shared.roles.Role;

public class HTString {
	private String data;
	private int color;
	
	public HTString(String data, int color){
		this.data = data;
		this.color = color;
	}

	public String access(boolean html) {
		if(html){
			return Event.WrapHTML(data, Event.ToHex(color));
		}else{
			return data;
		}
	}

	public int getColor() {
		return color;
	}
}
