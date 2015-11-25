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
	

	
	public void parseCommand(String command){
		if(command.length() == 0)
			return;
		int index = command.indexOf(Constants.NAME_SPLIT);
		
		int id = Integer.parseInt(command.substring(0, index));
		Player owner = n.getPlayerByID(id);
		command = command.substring(index + Constants.NAME_SPLIT.length());
		command = command.replace("\n", "");
		
		command(owner, command);
	}

    public static final String VOTE = Constants.VOTE;
    public static final String UNVOTE = Constants.UNVOTE;
    public static final String SKIP_VOTE = Constants.SKIP_VOTE;
    public static final String SAY = Constants.SAY;
    public static final String END_NIGHT = Constants.END_NIGHT;
    public static final String MODKILL = Constants.MODKILL;
	
	public void command(Player owner, String message){
        switch(message.toLowerCase()){
            case MODKILL:
            	owner.modkill();
            	return;
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
                }
                return;

            case SKIP_VOTE:
                if (dayAttempt()){
                    owner.voteSkip();
                }
                return;

            case UNVOTE:
                if(dayAttempt()){
                    if(n.getVoteTarget(owner) == null)
                    	throw new VotingException("You haven't vote anyone.");
                    else {
                        owner.unvote();
                        owner.sendMessage("Successfully unvoted.");
                    }
                }
                return;

            case Mayor.REVEAL:
                if(dayAttempt()){
                    if (owner.hasDayAction() && owner.is(Mayor.ROLE_NAME)){
                        owner.doDayAction();
                        //handled onreveal
                    }
                    else
                    	throw new IllegalActionException("Only mayors can do this.");
                }
                return;
            case Arsonist.BURN:
                if (owner.is(Arsonist.ROLE_NAME)){
                    if(n.isNight()) {
                        if (owner.getTarget(Arsonist.DOUSE_) != null || owner.getTarget(Arsonist.UNDOUSE_) != null){
                        	throw new IllegalActionException("You can't burn if you have someone doused or are going to undouse someone tonight");
                        }else{
                            tryDoubleCommand(owner, message);
                        }
                    }else if(owner.hasDayAction()){
                        owner.doDayAction();
                        return;
                    }else{
                        if (n.getRules().arsonDayIgnite)
                        	throw new IllegalActionException("You can't do this again.");
                        else
                        	throw new IllegalActionException("You can't do this during the day.");
                    }
                }else{
                	throw new IllegalActionException("Only arsonists can do this.");
                }
                return;

            case Veteran.ALERT:
                if (owner.getRoleName().equals(Veteran.ROLE_NAME)){
                    if(n.isNight()) {
                        owner.setTarget(null, (owner.parseAbility(message)));
                    }else{
                    	throw new IllegalActionException("You can't do this during the day.");
                    }
                }else{
                	throw new IllegalActionException("Only veterans can do this.");
                }
                return;

            case Constants.CANCEL:
                if (!n.isNight())
                	throw new PhaseException("You can't cancel night actions during the day.");
                
                try{
                    int ability = Integer.parseInt(message);
                    owner.removeTarget(ability, true);
                }catch(NumberFormatException e){
                    owner.sendMessage("canceling requires a number");
                }

            default:
            	tryDoubleCommand(owner, message);
                //TODO handle invalid people
                //TODO driver double handling
        }
    }

    private void tryDoubleCommand(Player owner, String message){
        ArrayList<String> block = new ArrayList<>();
        for(String s: message.split(" "))
        	block.add(s);
       
        
        if (block.size() < 2)
        	throw new IllegalActionException();
        String command = block.remove(0);
        

        if(command.equalsIgnoreCase(SAY)){
        	message = message.substring(SAY.length() + 1);
        	owner.say(message);
        	return;
        }
        
        
        Player target = findName(block, n);//removes names from block as well
        if(target == null) //might be framer team at the end
        	throw new UnknownPlayerException();
        
        
        if(n.isDay()){
            if (!command.equalsIgnoreCase(VOTE)) {
                throw new IllegalActionException();
            }else if(owner == target){
                owner.sendMessage("You can't vote yourself.");
            }else if(owner.isBlackmailed()){
                owner.sendMessage("You can't vote people if you're blackmailed.");
            }else
            	owner.vote(target);
        }else{
            int ability = owner.parseAbility(command);
            if (ability == Role.INVALID_ABILITY){
            	throw new IllegalActionException();
            }
            if (framerCheck(ability, target, block, owner))
                return;

            owner.setTarget(target, ability);
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
    			for(String s: possName.split(" "))
    				blocks.remove(s);
    			return p;
    		}
    	}
    	return null;
    }
}
