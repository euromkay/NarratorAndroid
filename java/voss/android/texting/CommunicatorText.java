package voss.android.texting;

import java.util.ArrayList;

import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorHandler;
import voss.shared.packaging.Packager;

public class CommunicatorText extends Communicator{

	private PhoneNumber number;
	
	public CommunicatorText(PhoneNumber number){
		this.number = number;
	}
	
	public CommunicatorText() {
	}

	public void sendMessage(String message) {
		if(message.length() < 139){
			sendText(message);
			return;
		}
		sendText(message.substring(0, 139));
		sendMessage(message.substring(139));
	}
	

	public void sendMessage(ArrayList<String> messages) {
		for(String s: messages)
			sendMessage(s);
	}

	private void sendText(String message){
		ReceiverText.sendText(number, message);//.replace("\n", ""));

	}
	
	public PhoneNumber getNumber() {
		return number;
	}


	public boolean equals(Object o){
		if(!super.equals(o))
			return false;
		
		CommunicatorText ct = (CommunicatorText) o;
		
		if(!number.equals(ct.number))
			return false;
		
		return true;
	}
	
	public void writeToParcel(Packager p, CommunicatorHandler ch){
		ch.writeHeading(p, this);
		p.write(number.toString());
	}

	public void getFromParcel(Packager p){
		number = new PhoneNumber(p.readString());
	}
}
