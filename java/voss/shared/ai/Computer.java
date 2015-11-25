package voss.shared.ai;


import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.PhaseException;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Framer;
import voss.shared.roles.Mayor;


public class Computer {

    public static final String NAME = "Slave ";
    public static final int NUM_PLAYERS = 10;

    protected Player slave;
    protected Brain brain;
    protected Controller controller;
    public Computer(Player slave, Brain brain, Controller controller){
        this.slave = slave;
        this.brain = brain;
        this.controller = controller;
    }

    public void doNightAction(){
        if (slave.endedNight())
            controller.cancelEndNight(slave);
        PlayerList selections = slave.getNarrator().getLivePlayers().remove(brain.masters);

        String[] abilities = slave.getAbilities();
        for(String action: abilities){
            int ability = slave.parseAbility(action);

            if(mafSendAbility(ability))
            	continue;
            if (arsonAbility(ability, selections))
            	continue;
            
            Player choice = getTargetChoice(ability, selections.copy());
            if (choice == null)
                continue;

            if (framerMainAbility(ability, choice))
            	continue;
  
            
            try{
            	controller.setNightTarget(slave, choice, action);
            }catch(PlayerTargetingException e){
            	e.printStackTrace();
            }
        }
        arsonAction = false;
        controller.endNight(slave);
    }
    
    private boolean arsonAction = false;
    private boolean arsonAbility(int ability, PlayerList choices){
    	if (arsonAction)
    		return false;
    	if (!slave.getRoleName().equals(Arsonist.ROLE_NAME))
    		return false;
    	
    	if (ability != Arsonist.BURN_ && ability != Arsonist.DOUSE_ && ability != Arsonist.UNDOUSE_)
    		return false;
    	arsonAction = true;
    	
    	int choice = brain.random.nextInt(10);
    	if (choice == 0)
    		controller.setNightTarget(slave, choices.getRandom(brain.random), Arsonist.UNDOUSE);
    	else if (choice <= 3 && slave.isAcceptableTarget(choices.getRandom(brain.random), Arsonist.BURN_))
    		controller.setNightTarget(slave, choices.getRandom(brain.random), Arsonist.BURN);
    	else
    		controller.setNightTarget(slave, choices.getRandom(brain.random), Arsonist.DOUSE);
    		
    	return true;
    }
    
    private boolean framerMainAbility(int ability, Player choice){
    	if (slave.getRoleName().equals(Framer.ROLE_NAME) && ability == Framer.MAIN_ABILITY){
        	int teamChoiceSize = slave.getNarrator().getNumberOfTeams();
        	int random = brain.random.nextInt(teamChoiceSize);
            String teamName = choice.getNarrator().getAllTeams().get(random).getName();
            controller.setNightTarget(slave, choice, Framer.FRAME, teamName);
            return true;
    	}
    	return false;
    }
    
    private boolean mafSendAbility(int ability){
    	if(ability == Team.SEND_){
    		Player sender = brain.getMafSender(slave);
    		if(sender == slave || !slave.isBlackmailed()) //if not blackmailed, they can choose whoever they want. if they are, they'll shut up if its not themselves
    			controller.setNightTarget(slave, sender, Team.SEND);
    		return true;
    	}
    		
    	return false;	
    }

    private Player getTargetChoice(int ability, PlayerList possible){
        Player selection = possible.getRandom(brain.random);
        if (selection == null)
            return null;

        if (slave.isAcceptableTarget(selection, ability)){
        	selection = slave.getNarrator().getPlayerByID(selection.getID());
            return selection;
        }
        else
            return getTargetChoice(ability, possible.remove(selection));
    }

    private int day = -1;
    public void doDayAction(){
        Narrator n = slave.getNarrator();
        if (n.isNight())
            throw new PhaseException(slave.getName() + " trying to do day action during night");
        if (slave.is(Mayor.ROLE_NAME) && slave.hasDayAction())
        	slave.doDayAction();

        //arson killed himself
        if (slave.isDead())
            return;

        //second time around, vote skip
        if (day == n.getDayNumber()){
            if (n.getVoteTarget(slave) != slave.getSkipper())
            	controller.skipVote(slave);
            return;
        }

        day = n.getDayNumber();

        Player choiceA = brain.getDayChoices()[0];
        choiceA = n.getPlayerByID(choiceA.getID());
        Player choiceB = brain.getDayChoices()[1];
        choiceB = n.getPlayerByID(choiceB.getID());
        
        controller.say(slave, "What.");
        
        if(slave.isBlackmailed() || (choiceA.isDead() && choiceB.isDead())){
        	controller.vote(slave, slave.getSkipper());
        	return;
        }
        
        if(choiceA.isDead()){
        	if(choiceB == slave)
            	controller.skipVote(slave);
        	else
        		controller.vote(slave, choiceB);
        	return;
        }
        
        if(choiceB.isDead()){
        	if(choiceA == slave)
            	controller.skipVote(slave);
        	else
        		controller.vote(slave, choiceA);
        	return;
        }
        
        if (choiceA == slave){
            controller.vote(slave, choiceB);
        }else if (choiceB == slave){
            controller.vote(slave, choiceA);
        }else{

            if (brain.mastersExist()) {
                if (choiceA != null && n.getVoteCountOf(choiceA) + slave.getVotePower() == n.getMinLynchVote())
                    choiceA = null;
                if (choiceB != null && n.getVoteCountOf(choiceB) + slave.getVotePower() == n.getMinLynchVote())
                    choiceB = null;
            }
            if(choiceA == null && choiceB == null)
            	controller.skipVote(slave);
            else if(choiceA == null) 
                controller.vote(slave, choiceB);
            else if(choiceB == null)
                controller.vote(slave, choiceA);
            else if(!brain.mastersExist())
            	 controller.vote(slave, choiceA);
            else{
            	if (brain.random.nextBoolean())
            		controller.vote(slave, choiceA);
            	else
            		controller.vote(slave, choiceB);
            }

        }
    }
}