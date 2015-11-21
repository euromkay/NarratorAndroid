package voss.android;

import java.util.ArrayList;

import voss.logic.support.Communicator;
import voss.logic.support.CommunicatorHandler;
import voss.packaging.Packager;


public class CommunicatorPhone extends Communicator{

	public CommunicatorPhone() {
	}

	public void sendMessage(String message) {

	}
	public void sendMessage(ArrayList<String> messages) {
		for(String s: messages)
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

}
