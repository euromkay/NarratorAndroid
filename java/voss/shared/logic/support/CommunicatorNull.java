package voss.shared.logic.support;

import java.util.ArrayList;

import voss.shared.packaging.Packager;


public class CommunicatorNull extends Communicator{

	public CommunicatorNull() {}
	public void sendMessage(String message) {
		if(message == null)
			throw new NullPointerException();
		messages.add(message);
	}
	
	private ArrayList<String> messages = new ArrayList<String>();
	public ArrayList<String> getMessages(){
		return messages;
	}
	public void sendMessage(ArrayList<String> input) {
		for(String s: input)
			messages.add(s);
		
	}
	
	
	
	
	
	public void writeToParcel(Packager p, CommunicatorHandler ch){
		ch.writeHeading(p, this);
		//Communicator.GetId(p, this);
		p.signal("writing communicator null\n\n");
		//p.write(messages);
	}
	public void getFromParcel(Packager p){
		//messages = p.readStringList();
	}
	
	public boolean equals(Object o){
		if(!super.equals(o))
			return false;
		
		//CommunicatorNull c = (CommunicatorNull) o;
		//if(messages.size() != c.messages.size())
			//return false;
		
		//for(int i = 0; i < messages.size(); i++){
			//if(!messages.get(i).equals(c.messages.get(i)))
				//return false;
		//}
		return true;
	}
	public String getLastMessage() {
		return messages.get(messages.size()-1);
		
	}

	
}
