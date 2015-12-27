package voss.shared.ai;


import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Rules;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.logic.support.Constants;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Executioner;
import voss.shared.roles.Framer;
import voss.shared.roles.Janitor;
import voss.shared.roles.Jester;
import voss.shared.roles.Mayor;
import voss.shared.roles.Sheriff;


public class Computer {

    public static final String NAME = "Slave";
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
        PlayerList selections = slave.getNarrator().getLivePlayers();
        if(!brain.targetAnyone)
        	selections.remove(brain.masters);

        String[] abilities = slave.getAbilities();
        for(String action: abilities){
            int ability = slave.parseAbility(action);

            if(mafSendAbility(ability))
            	continue;
            if (arsonAbility(ability, selections))
            	continue;
            
            if (mafKillAbility(ability, selections))
            	continue;
            if (janitorAbility(ability))
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
    	if (!slave.getRoleName().equals(Arsonist.ROLE_NAME))
    		return false;
    	
    	boolean arsonAbility = (ability != Arsonist.BURN_ && ability != Arsonist.DOUSE_ && ability != Arsonist.UNDOUSE_);
    	arsonAbility = !arsonAbility;
    	
    	//if arson already performed an arson action and this is an arson ability
    	if (arsonAction)
    		return arsonAbility;
    	
    	//if arson didn't perform their action yet and this isn't an arson ability
    	if (!arsonAbility)
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
    	if (slave.is(Framer.ROLE_NAME) && ability == Framer.MAIN_ABILITY){
        	int teamChoiceSize = slave.getNarrator().getNumberOfTeams();
        	int random = brain.random.nextInt(teamChoiceSize);
            String teamName = choice.getNarrator().getAllTeams().get(random).getName();
            controller.setNightTarget(slave, choice, Framer.FRAME, teamName);
            return true;
    	}
    	return false;
    }
    private boolean janitorAbility(int ability){
    	if (slave.is(Janitor.ROLE_NAME) && ability == Janitor.MAIN_ABILITY) {
    		if (brain.getMafSender(slave) != slave){
    			Player killTarget = brain.getMafKillTarget(slave);
    			if(killTarget != null)
    				controller.setNightTarget(slave, killTarget, Janitor.CLEAN);
    		}
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
    
    private boolean mafKillAbility(int ability, PlayerList selections){
    	if(ability == Team.KILL_){
    		selections = selections.copy();
    		selections.remove(slave.getTeam().getMembers());
    		if(brain.getMafSender(slave) == slave){
        		Player killTarget = brain.getMafKillTarget(slave);
        		if(killTarget != null)
        			controller.setNightTarget(slave, killTarget, Team.KILL);
    		}
    		return true;
    	}
    		
    	return false;	
    }

    private Player getTargetChoice(int ability, PlayerList possible){
        Player selection = possible.getRandom(brain.random);
        if (selection == null)
            return null;

        if (slave.isAcceptableTarget(selection, ability)){
        	selection = slave.getNarrator().getPlayerByName(selection.getName());
            return selection;
        }
        else
            return getTargetChoice(ability, possible.remove(selection));
    }
    
    private boolean noPrevNight(){
    	Narrator n = slave.getNarrator();
    	Rules r = n.getRules();
    	if(n.getDayNumber() == 1 && r.DAY_START)
    		return true;
    	return false;
    }
    
    void talkings(){
    	if(slave.is(Mayor.ROLE_NAME)){
    		if(slave.hasDayAction())
            	controller.doDayAction(slave);
    	}
    	if(noPrevNight())
    		return;
    	if(slave.is(Sheriff.ROLE_NAME)){
    		sheriffTalk();
    	}else if(slave.is(Executioner.ROLE_NAME)){
    		exeuctionerTalk();
    	}else if(slave.is(Jester.ROLE_NAME)){
    		jesterTalk();
    	}
    	
    }
    
    private Narrator getNarrator(){
    	return slave.getNarrator();
    }
    
    private Team getTeam(int i){
    	return getNarrator().getTeam(i);
    }
    
    private void exeuctionerTalk(){
    	Executioner ex = (Executioner) slave.getRole();
    	Player target = ex.getTarget(slave);
    	if(target.isDead())
    		return;

    	Team arsons = getTeam(Constants.A_ARSONIST);
    	String say = "I'm Sheriff. ";
    	say = say + target.getName() + " is a(n) " + arsons.getName() + ".";
		slave.say(say);
		
    	brain.claim(target, arsons, slave);
    }
    
    private void jesterTalk(){
    	Team arsons = getTeam(Constants.A_ARSONIST);
    	Player randAccused = slave.getNarrator().getLivePlayers().getRandom(brain.random);
    	
    	String say = "I'm Sheriff. ";
    	say = say + randAccused.getName() + " is a(n) " + arsons.getName() + ".";
		slave.say(say);
    	
    	brain.claim(randAccused, arsons, slave);
    }
    
    private void sheriffTalk(){
    	int lastNight = slave.getNarrator().getDayNumber() - 1;
    	for(Event e: slave.getFeedback(lastNight)){
			String s = e.access(slave, false).replace("\n", "");
			if(s.startsWith("Your target")){
				Player target = slave.getPrevNightTarget(lastNight)[Sheriff.MAIN_ABILITY];
				String say = "I'm Sheriff. ";
				if(s.equals(Sheriff.NOT_SUSPICIOUS)){
					say = say + target.getName() + " is not suspicious.";
				}else{
					Team t = slave.getNarrator().getTeam(e.getHTStrings().get(0).getColor());
					brain.claim(target, t, slave);
					say = say + target.getName() + " is a(n) " + t.getName() + ".";
					slave.say(say);
				}
			}
			
		}
    }

	public Player vote(PlayerList choices) {
    	if (slave.isDead())
    		return null;
		Narrator n = slave.getNarrator();
    	if (n.isNight() || !n.isInProgress())
            return null;

        //arson killed himself
        if (slave.isDead())
            return null;
    	
		if(slave.isBlackmailed()){
			if(slave.getVoteTarget() == slave.getSkipper())
				return null;
			return controller.skipVote(slave);
		
		}
		choices = choices.copy();
		choices.remove(slave);
		if(choices.isEmpty())
			return controller.skipVote(slave);
		
		PlayerList temp;
		
		if (slave.getTeam().knowsTeam()){
			temp = choices.copy();
			temp.remove(slave.getTeam().getMembers().getLivePlayers());
			if(!temp.isEmpty())
				choices = temp;
		}
		for(Claim c: brain.claims){
			if(choices.size() <= 1)
				break;
			
			temp = choices.copy();
			if (c.believable(slave) && choices.contains(c.accused)){
				if(slave.getVoteTarget() == c.accused)
					return null;
				return controller.vote(slave, c.accused);
			}else if(c.outlandish(slave) && choices.contains(c.prosecutor)){
				if(slave.getVoteTarget() == c.prosecutor)
					return null;
				return controller.vote(slave, c.prosecutor);
			}
		}
		Player choice = choices.getRandom(brain.random);
		
		if(slave.getVoteTarget() != choice)
			return controller.vote(slave, choice);
		
		
		return null;
	}
}