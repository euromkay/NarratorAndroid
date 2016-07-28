package android.texting;

import shared.event.EventList;
import shared.event.Message;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorHandler;
import shared.packaging.Packager;

public class CommunicatorText extends Communicator{

	private PhoneNumber number;
	
	public CommunicatorText(PhoneNumber number){
		this.number = number;
	}
	
	public CommunicatorText() {
	}

	public void sendMessage(Message e){
		sendMessage(e.access(getPlayer(), false));
	}
	public void sendMessage(String message) {
		if(message.length() < 139){
			sendText(message);
			return;
		}
		sendText(message.substring(0, 139));
		sendMessage(message.substring(139));
	}
	
	public Communicator copy(){
		return new CommunicatorText(new PhoneNumber(number.number));
	}

	public void sendMessage(EventList messages) {
		for(Message s: messages)
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
