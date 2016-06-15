package android;

import java.util.ArrayList;

import shared.event.Event;
import shared.logic.Player;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorHandler;
import shared.packaging.Packager;


public class CommunicatorPhone extends Communicator{

	public CommunicatorPhone() {
	}

	public void sendMessage(Event e) {

	}
	public void sendMessage(ArrayList<Event> messages) {
		for(Event s: messages)
			sendMessage(s);
	}
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		return true;
	}

	public void writeToParcel(Packager p, CommunicatorHandler ch){
		ch.writeHeading(p, this);
	}
	public void getFromParcel(Packager p){}
	public Communicator copy(){
		return new CommunicatorPhone();
	}
}
