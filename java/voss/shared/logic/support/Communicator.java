package voss.shared.logic.support;

import java.util.ArrayList;

import voss.shared.packaging.Packager;


public abstract class Communicator{

	public abstract void sendMessage(String message);

	public abstract void sendMessage(ArrayList<String> message);
	
	public boolean equals(Object o){
		if(o == null)
			return false;
		if(o == this)
			return true;
		if(o.getClass() != getClass())
			return false;
		
		return true;
	}

	public abstract void writeToParcel(Packager p, CommunicatorHandler ch);
	public abstract void getFromParcel(Packager p);
	public abstract Communicator copy();
	
}
