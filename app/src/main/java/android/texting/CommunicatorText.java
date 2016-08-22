package android.texting;

import java.util.ArrayList;

import shared.event.EventList;
import shared.event.Message;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorHandler;
import shared.packaging.Packager;

public class CommunicatorText extends Communicator{

	private PhoneNumber number;
	public static final int MAX_LENGTH = 139;
	
	public CommunicatorText(PhoneNumber number){
		this.number = number;
	}
	
	public CommunicatorText() {
	}

	public void sendMessage(Message e){
		sendMessage(e.access(getPlayer(), false));
	}
	public void sendMessage(String message) {
		if(message.length() < MAX_LENGTH){
			sendText(message);
			return;
		}
		for(String text: splitMessages(message))
			sendText(text);
	}
	
	//garunteed to be greater than 139 characters
	public static ArrayList<String> splitMessages(String text){
		ArrayList<String> chunks = new ArrayList<>();
		
		String chunk;
		int loc;
		char[] delimiters = {'\n', '!', '.', ',', ' '};
		while(text.length() > MAX_LENGTH){
			boolean foundSomething = false;
			chunk = text.substring(0, MAX_LENGTH - 1);
			for(char delimiter : delimiters){
				loc = chunk.lastIndexOf(delimiter);
				if(loc == -1)
					continue;
				if(delimiter != '\n' && delimiter != ' ')
					loc++;
				chunk = chunk.substring(0, loc);
				chunks.add(chunk);
				text = text.substring(loc);
				
				if(text.length() != 0)
				while(text.charAt(0) == ' ' || text.charAt(0) == '\n')
					text = text.substring(1);
				
				foundSomething = true;
				break;
			}
			if(!foundSomething){
				chunk = chunk.substring(0, MAX_LENGTH - 1);
				chunks.add(chunk);
				text = text.substring(MAX_LENGTH - 1);
			}
		}
		chunks.add(text);
		
		return chunks;
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
