package voss.shared.logic.support;

import java.util.ArrayList;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.IllegalActionException;
import voss.shared.logic.exceptions.PhaseException;
import voss.shared.logic.exceptions.UnknownPlayerException;
import voss.shared.logic.exceptions.UnknownTeamException;
import voss.shared.logic.exceptions.VotingException;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Framer;
import voss.shared.roles.Mayor;
import voss.shared.roles.Role;
import voss.shared.roles.Veteran;

public class CommandHandler {

	protected Narrator n;
	
	public CommandHandler(Narrator n){
		this.n = n;
	}
	
	public static final int SYNCH = 1;
	public static final int ASYNCH = 0;
	
	public int parseCommand(String command){
		if(command.length() == 0)
			return -1;
		
		int index = command.indexOf(Constants.NAME_SPLIT);
		
		String name = command.substring(0, index);
		Player owner = n.getPlayerByName(name);
		command = command.substring(index + Constants.NAME_SPLIT.length());
		command = command.replace("\n", "");
		
		return command(owner, command, name);
	}

    public static final String VOTE = Constants.VOTE;
    public static final String UNVOTE = Constants.UNVOTE;
    public static final String SKIP_VOTE = Constants.SKIP_VOTE;
    public static final String SAY = Constants.SAY;
    public static final String END_NIGHT = Constants.END_NIGHT;
    public static final String MODKILL = Constants.MODKILL;
	
	public int command(Player owner, String message, String name){
        switch(message.toLowerCase()){
            case MODKILL:
            	owner.modkill();
            	return SYNCH;
            case END_NIGHT:
                if(n.isDay())
                	throw new PhaseException("You can only end night during the day");
                else{
                    if(owner.endedNight()) {
                        owner.sendMessage( "You have canceled your motion to end the night.");
                        owner.cancelEndNight();
                    }else{
                        owner.sendMessage( "You have moved to end the night.");
                        owner.endNight();
                    }
                    return SYNCH;
                }
            case SKIP_VOTE:
                if (dayAttempt()){
                    owner.voteSkip();
                    return SYNCH;
                }
                break;
            case UNVOTE:
                if(dayAttempt()){
                    if(owner.getVoteTarget() == null)
                    	throw new VotingException("You haven't vote anyone.");
                    else {
                        owner.unvote();
                        owner.sendMessage("Successfully unvoted.");
                        return SYNCH;
                    }
                }
                break;
            case Mayor.REVEAL:
                if(dayAttempt()){
                    if (owner.hasDayAction() && owner.is(Mayor.ROLE_NAME)){
                        owner.doDayAction();
                        return SYNCH;
                    }
                    else
                    	throw new IllegalActionException("Only mayors can do this.");
                }
                break;
            case Arsonist.BURN:
                if (owner.is(Arsonist.ROLE_NAME)){
                    if(n.isNight()) {
                        if (owner.getTarget(Arsonist.DOUSE_) != null || owner.getTarget(Arsonist.UNDOUSE_) != null){
                        	throw new IllegalActionException("You can't burn if you have someone doused or are going to undouse someone tonight");
                        }else{
                            return tryDoubleCommand(owner, message, name);
                        }
                    }else if(owner.hasDayAction()){
                        owner.doDayAction();
                        return SYNCH;
                    }else{
                        if (n.getRules().arsonDayIgnite)
                        	throw new IllegalActionException("You can't do this again.");
                        else
                        	throw new IllegalActionException("You can't do this during the day.");
                    }
                }else{
                	throw new IllegalActionException("Only arsonists can do this.");
                }
		case Veteran.ALERT:
                if (owner.getRoleName().equals(Veteran.ROLE_NAME)){
                    if(n.isNight()) {
                        owner.setTarget(null, (owner.parseAbility(message)));
                        return ASYNCH;
                    }else{
                    	throw new IllegalActionException("You can't do this during the day.");
                    }
                }else{
                	throw new IllegalActionException("Only veterans can do this.");
                }
		case Constants.CANCEL:
                if (!n.isNight())
                	throw new PhaseException("You can't cancel night actions during the day.");
                try{
                	for(String s: owner.getAbilities()){
                		owner.removeTarget(owner.parseAbility(s), true);
                	}
                	owner.sendMessage(message);
                	return ASYNCH;
                }catch(NumberFormatException e){
                    owner.sendMessage("canceling requires a number");
                }

            default:
            	return tryDoubleCommand(owner, message, name);
                //TODO handle invalid people
                //TODO driver double handling
        }
        return -1;
    }

    private int tryDoubleCommand(Player owner, String message, String name){
        ArrayList<String> block = new ArrayList<>();
        for(String s: message.split(" "))
        	block.add(s);
       
        
        if (block.size() < 2)
        	throw new IllegalActionException();
        String command = block.remove(0);
        

        if(command.equalsIgnoreCase(SAY)){
        	message = message.substring(SAY.length() + 1);
        	if(owner == null && !n.isStarted())
        		owner = new Player(name + "[Pregame]", null, n);
        	message = message.substring(block.get(0).length() + 1);
        	owner.say(message, block.get(0));
        	return ASYNCH;
        }
        int untarget = message.indexOf(block.get(0));
        if(message.substring(untarget).equals(Constants.UNTARGET)){
        	if (n.isDay()){
        		throw new IllegalActionException("You can't untarget during the night.");
        	}else{
        		int ability = owner.parseAbility(command);
                if (ability == Role.INVALID_ABILITY){
                	throw new IllegalActionException();
                }
                owner.removeTarget(ability, true);
            	return ASYNCH;
        	}
        }
        	
        
        Player target = findName(block, n);//removes names from block as well
        if(target == null) //might be framer team at the end
        	throw new UnknownPlayerException(block.toString());
        
        if(target.equals(n.Skipper) && n.isDay()){
        	throw new VotingException("You can't vote the skipper directly.!");
        }
        
        if(n.isDay()){
            if (!command.equalsIgnoreCase(VOTE)) {
                throw new IllegalActionException();
            }else if(owner == target){
                throw new VotingException("You can't vote yourself.");
            }else if(owner.isBlackmailed()){
                throw new VotingException("You can't vote people if you're blackmailed.");
            }else{
            	owner.vote(target);
            	return SYNCH;
            }
        }else{
            int ability = owner.parseAbility(command);
            if (ability == Role.INVALID_ABILITY){
            	throw new IllegalActionException("Unknown ability " + command);
            }
            if (framerCheck(ability, target, block, owner))
                return ASYNCH;

            owner.setTarget(target, ability);
            return ASYNCH;
        }
            
    }
    private boolean framerCheck(int ability, Player target, ArrayList<String> blocks, Player owner){
        if(!owner.is(Framer.ROLE_NAME))
            return false;
        if(Framer.FRAME_ != ability)
            return false;
        
        if(blocks.isEmpty()){
        	owner.sendMessage("To frame, you need to specify a team");
        	return true;
        }

        StringBuilder sb = new StringBuilder();
        for(String s: blocks){
        	sb.append(s);	
        }
        
        int teamColor = parseTeam(sb.toString(), n);
        if(Constants.A_INVALID == teamColor)
        	throw new UnknownTeamException();
        else
            owner.setTarget(target, Framer.FRAME_, teamColor);

        return true;
    }

    public static int parseTeam(String name, Narrator n){
		for(Team t: n.getAllTeams())
			if(t.getName().replace(" ", "").equalsIgnoreCase(name.replace(" ", "")))
				return t.getAlignment();
		return Constants.A_INVALID;
	}
	
    public boolean dayAttempt(){
        if (!n.isDay())
        	throw new PhaseException("Can't do this during the night");
        
        return true;
    }
    public static Player findName(ArrayList<String> blocks, Narrator n){
    	for(int i = 0; i < blocks.size(); i++){
    		String possName = "";
    		for(int j = 0; j <= i; j++){
    			possName += " " + blocks.get(j);
    		}
            possName = possName.substring(1);
    		Player p = n.getPlayerByName(possName);
    		if(p != null){
    			if(possName.equals(Constants.UNTARGET))
    				return n.Skipper;
    			for(String s: possName.split(" "))
    				blocks.remove(s);
    			return p;
    		}
    	}
    	return null;
    }

}
