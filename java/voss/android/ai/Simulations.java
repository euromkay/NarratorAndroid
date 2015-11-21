package voss.android.ai;

import java.util.Random;

import voss.logic.Player;
import voss.logic.PlayerList;


public class Simulations {

    private Brain brain;
    private Controller controller;
    public Simulations(Controller controller, Random rand){
        PlayerList slaves = new PlayerList();
        PlayerList masters = new PlayerList();
        
        for (Player potential: controller.getNarrator().getLivePlayers()){
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

    public void next(){
        if (controller.getNarrator().isDay()) {
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
