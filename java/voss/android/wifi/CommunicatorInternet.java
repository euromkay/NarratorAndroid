package voss.android.wifi;

import java.util.ArrayList;

import voss.logic.support.Communicator;
import voss.logic.support.CommunicatorHandler;
import voss.packaging.Packager;

public class CommunicatorInternet extends Communicator{

    private ChatManager c;
    public CommunicatorInternet(ChatManager c){
        this.c = c;
    }

    public void sendMessage(String message){
        //c.write(message);
    }


    public void sendMessage(ArrayList<String> message){
        
    }

    public ChatManager getManager(){
        return c;
    }

    public void writeToParcel(Packager p, CommunicatorHandler ch){
    	ch.writeHeading(p, this);
    }

    public void getFromParcel(Packager p){

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
