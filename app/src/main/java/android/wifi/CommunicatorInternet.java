package android.wifi;

import shared.event.EventList;
import shared.event.Message;
import shared.logic.support.Communicator;
import shared.logic.support.CommunicatorHandler;
import shared.packaging.Packager;

public class CommunicatorInternet extends Communicator{

    private ChatManager c;
    public CommunicatorInternet(ChatManager c){
        this.c = c;
    }

    public void sendMessage(Message e){
        //c.write(message);
    }


    public void sendMessage(EventList message){
        
    }

    public ChatManager getManager(){
        return c;
    }

    public void writeToParcel(Packager p, CommunicatorHandler ch){
    	ch.writeHeading(p, this);
    }

    public void getFromParcel(Packager p){

    }

    public Communicator copy(){
    	return new CommunicatorInternet(c);
    }
    
    public boolean equals(Object o){
    	if (o == null)
    		return false;
    	
    	if(o.getClass() != getClass())
    		return false;
    	
    	CommunicatorInternet cI = (CommunicatorInternet) o;
    	
    	if (c.getID() != cI.c.getID())
    		return false;
    	
    	return true;
    }
}
