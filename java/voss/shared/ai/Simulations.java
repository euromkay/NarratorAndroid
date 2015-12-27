package voss.shared.ai;

import java.util.Random;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;


public class Simulations {

    public Brain brain;
    private Controller controller;
    public Simulations(Controller controller, Random rand, Narrator n){
        PlayerList slaves = new PlayerList();
        PlayerList masters = new PlayerList();
        
        for (Player potential: n.getLivePlayers()){
            if (potential.isComputer())
                slaves.add(potential);
            else
                masters.add(potential);
        }
        brain = new Brain(masters, rand);

        for (Player p: slaves){
        	brain.addSlave(p, controller);
        }
        this.controller = controller; 
    }

    private Player getPlayer(){
    	if (!brain.masters.isEmpty())
    		return brain.masters.get(0);
    	if(!brain.slaves.isEmpty())
    		return brain.slaves.get(0);
    	
    	return null;
    }
    
    public void next(){
    	Player p = getPlayer();
    	if(p == null)
    		return;
    	
    	
        if (p.getNarrator().isDay()) {
        	updateMasters();
            brain.dayAction();
            brain.dayAction();
        }else {
        	updateMasters();
            brain.nightAction();
        }
        if(brain.mastersExist())
        	controller.selectHost(brain.masters.get(0));
            //Controller.selectYourself(slave., brain.masters.get(0));

        controller.log("simulations finished");
    }

    

    private void updateMasters(){
        PlayerList newMasters = new PlayerList();
        for(Player p: brain.masters)
            if (p.isAlive())
                newMasters.add(p);

        brain.masters = newMasters;
    }


    

}
