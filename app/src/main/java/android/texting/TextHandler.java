package android.texting;


import shared.event.Message;
import shared.event.OGIMessage;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.Team;
import shared.logic.exceptions.IllegalActionException;
import shared.logic.exceptions.PhaseException;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.exceptions.UnknownPlayerException;
import shared.logic.exceptions.UnknownTeamException;
import shared.logic.exceptions.VotingException;
import shared.logic.listeners.NarratorListener;
import shared.logic.support.CommandHandler;
import shared.logic.support.RoleTemplate;
import shared.roles.Arsonist;
import shared.roles.Assassin;
import shared.roles.Mayor;


public class TextHandler extends CommandHandler implements NarratorListener {

	public PlayerList texters;
    public TextHandler(Narrator n, PlayerList texters){
        super(n);
        n.addListener(this);
        
        this.texters = texters;
        
        if (n.isStarted()){
        	onGameStart();
        }
    }
    
    public void setTexters(PlayerList texters){
    	this.texters = texters;
    }
    
    public void onGameStart(){
    	String message;
        Team t;
        for(Player texter: texters){
            message = "You are a " + texter.getRoleName() + "!";
            t = texter.getTeam();
            if(!t.isSolo())
                message += " You are part of the " + t.getName() + ".";
            if(t.knowsTeam() && t.getMembers().getLivePlayers().size() > 1){
                message += " Your teammates are " +t.getMembers().sortByName().toString() + ".";
            }
            message += " " + texter.getRoleInfo();
            new OGIMessage(texter, message);
        }
        if(n.isDay())
            onDayStart(null);
        else
            onNightStart(null);
        
        sendHelpPrompt();
        
    }

    private void sendHelpPrompt(Player owner){
        String message = "See roles List - " + SQuote(ROLE_INFO);
        message += "\nSee live players - " + SQuote(LIVE_PEOPLE);
        if(owner.getTeam().knowsTeam() && owner.getTeam().getMembers().size() > 1)
            message += "\nSee team members - " + SQuote(TEAM_INFO);
        message += "\nGet day help - " + SQuote(DAY_HELP);
        message += "\nGet night help - " + SQuote(NIGHT_HELP);
        new OGIMessage(owner, message);
    }

    private static final String HELP = "help";
    public static final String ROLE_INFO = "roles list";
    public static final String LIVE_PEOPLE = "get players";
    public static final String TEAM_INFO = "get team";
    public static final String NIGHT_HELP = "help night";
    public static final String NIGHT_HELP2 = "night help";
    public static final String DAY_HELP = "help day";
    public static final String DAY_HELP2 = "day help";


    public void text(Player owner, String message, boolean sync){
    	while(message.substring(message.length() - 1).equals(" "))
    		message = message.substring(0, message.length() - 1);
        switch(message.toLowerCase()){
            case HELP:
                sendHelpPrompt(owner);
                return;
            case ROLE_INFO:
                sendRoleInfo(owner);
                return;
            case LIVE_PEOPLE:
                sendLivePlayerList(owner);
                return;
            case TEAM_INFO:
                sendTeamInfo(owner);
                return;
            case NIGHT_HELP2:
            case NIGHT_HELP:
                sendNightTextPrompt(owner);
                return;
            case DAY_HELP2:
            case DAY_HELP:
                sendDayTextPrompt(owner);
                return;
            default:
                try{
                	synchronized(n){
                		super.command(owner, message, "text");	
                		//if(tc != null)
                			//tc.text(owner, message, sync);
                	}
                }catch(IllegalActionException e){
                    if (e.getMessage().length() == 0){
                        new OGIMessage(owner, "Unknown command.  Type " + HELP + " to see a list of commands.");
                    }else{
                    	new OGIMessage(owner, e.getMessage());
                    }

                    printException(e);
                }catch(UnknownPlayerException f){
                    new OGIMessage(owner, "Unknown player name. Type "  + SQuote(LIVE_PEOPLE) + " to get a list of players.");
                    printException(f);

                }catch(UnknownTeamException e){
                    new OGIMessage(owner,  "Unknown team name. Type " + NIGHT_HELP + " to get info about what you can do during the night.");
                    printException(e);

                }catch (PlayerTargetingException g){
                    new OGIMessage(owner, g.getMessage());
                    printException(g);

                }catch (PhaseException e){
                    new OGIMessage(owner, e.getMessage());
                    printException(e);

                }catch (VotingException e){
                	new OGIMessage(owner, e.getMessage());
                    printException(e);
                }
        }
    }

    private void printException(Throwable e){
    	System.err.println(e.getMessage());
    }
    
    /*public static Player findName(ArrayList<String> blocks, PhoneBook phonebook){
        for(int i = 0; i < blocks.size(); i++){
            String possName = "";
            for(int j = 0; j <= i; j++){
                possName += " " + blocks.get(j);
            }
            possName = possName.substring(1);
            Player p = phonebook.getByName(possName);
            if(p != null){
                for(String s: possName.split(" "))
                    blocks.remove(s);
                return p;
            }
        }
        return null;
    }
    
    
    private void invalid(Player target){
        target.sendMessage( "unknown message.  " + HELP);
    }*/




    private void sendRoleInfo(Player owner){
        String roles_list_message = "These are the roles in game:\n";
        for(RoleTemplate r : n.getAllRoles()){
            if(r.isRandom())
                roles_list_message += r.getName() + "\n";
            else
                roles_list_message += n.getTeam(r.getColor()) + " " + r.getName() + "\n";
        }
        roles_list_message = roles_list_message.substring(0, roles_list_message.length() - 1);
        new OGIMessage(owner, roles_list_message);
    }
    private void sendLivePlayerList(Player owner){
        String list_of_texters = "These are the list of people in the game: ";
        for(Player p: n.getLivePlayers())
            list_of_texters += (p.getName() + ", ");
        new OGIMessage(owner, list_of_texters);
    }
    private void sendTeamInfo(Player p){
        if (p.getTeam().knowsTeam()) {
            Team t = p.getTeam();
            PlayerList team = t.getMembers();
            if(team.isEmpty()){
            	new OGIMessage(p, "You dont' have any teammembers.");
            }else {
                PlayerList living = team.getLivePlayers();
                if(living.isEmpty()){
                	new OGIMessage(p, "All your teammates died");
                }else
                	new OGIMessage(p, "You're teammates are: " + p.getTeam().getMembers().getLivePlayers().sortByName().toString() + "");
            }
        }else
        	new OGIMessage(p, "I can't tell you who's on your team.");
    }

    private void sendNightPrompt(Player p){
        String message = SQuote(END_NIGHT)  + " so night can end.";
        if (p.getAbilities().length == 0){
        	new OGIMessage(p, "Type " + message );
        }else{
        	new OGIMessage(p, "Submit your night action.  When you're done, type " + message);
        }
    }

    public void onEndGame(){
    	broadcast(n.getWinMessage().access(Message.PUBLIC, false));
    }

    private void sendHelpPrompt(){
    	broadcast("To see a list of possible commands, text " + SQuote(HELP) + "");
    }

    

    public void sendNightTextPrompt(Player texter){
    	new OGIMessage(texter, texter.getRoleInfo());
    	new OGIMessage(texter, texter.getNightText());
    	new OGIMessage(texter, "If you want to cancel your night actions, type " + SQuote("cancel") + ".");
        texter.sendTeamTextPrompt();
        if(texter.getTeam().knowsTeam() && texter.getTeam().size() > 1)
        	new OGIMessage(texter, "To talk to your allies : -  " + SQuote(SAY + " message") + "");
        new OGIMessage(texter, "After you're done submitting actions, text " + SQuote(END_NIGHT) + " so night can end.  If you want to cancel your bid to end night, type it again.");
    }

    public void sendDayTextPrompt(Player texter){
        String message = "To vote or change your vote : - " + SQuote(VOTE + " name");
        message += ".\nTo unvote: - " + SQuote(UNVOTE);
        message += ".\nTo end the day so that no one is lynched : - " + SQuote(SKIP_VOTE) + "";

    	new OGIMessage(texter, message);
    	
    	if(!texter.hasDayAction())
    		return;
    	if(texter.is(Arsonist.class)){
    		new OGIMessage(texter, "To burn everyone you doused right now, text " + SQuote(Arsonist.BURN_LOWERCASE));
    	}
    	else if(texter.is(Mayor.class)){
    		new OGIMessage(texter, "To reveal as mayor, text " + SQuote(Mayor.REVEAL_LOWERCASE));
    	}
    	else if(texter.is(Assassin.class))
    		new OGIMessage(texter, "To assassinate someone, text " + SQuote(Assassin.ASSASSINATE_LOWERCASE + " name"));
    }

    public void onModKill(Player p){
    	broadcast(p + " suicided!");
    }

    public void onDayStart(PlayerList dead){
        if(dead != null){
            if(dead.isEmpty())
            	broadcast("No one died!");
            else{
                for(Player deadPerson: dead){
                	broadcast(deadPerson.getDescription() + " was found " + deadPerson.getDeathType().toString() + ".");
                }
            }
        }

        broadcast("It is now daytime. Please vote.  Once someone has enough votes, the day will end.");
        
        for(Player p: texters){
            if(p.isBlackmailed())
            	new OGIMessage(p,"You're blackmailed. You can't talk and can only vote to skip the day.");
        }

    }
    
    public void broadcast(String s){
    	new OGIMessage(texters, s);
    }

    public void onNightStart(PlayerList dead){
        if(dead != null) {
            if (dead.isEmpty() || dead.get(0) == n.Skipper){
            	if (n.getLiveSize() == 2){
            		broadcast("Voting became impossible so night started!");
            	} else if (n.Skipper.getVoters().isEmpty())
            		broadcast("The day ended due to inactivity.");
            	else
            		broadcast(n.Skipper.getVoters().getStringName() + " opted to skip lynching today.");
            }
            else {
                for (Player deadPerson : dead) {
                	broadcast(deadPerson.getDescription() + " was lynched by " + deadPerson.getVoters().getStringName() + "");
                }
            }
        }
        broadcast("It is now nighttime.");
        for(Player p: n.getLivePlayers()){
            sendNightPrompt(p);
        }
    }

    public static String SQuote(String s){
        return "\'" + s + "\'";
    }

    public void onArsonDayBurn(Player arson, PlayerList burned, Message e){

    }

    public void onNightTarget(Player p, Player t){

    }

    public void onNightTargetRemove(Player p, Player r){

    }

    public void onEndNight(Player p) {
    	new OGIMessage(p, "You've ended the night");
    }

    public void onCancelEndNight(Player p) {
    	new OGIMessage(p, "You've canceled your motion to end the night");
    }

    public void onMayorReveal(Player m, Message e){
    	broadcast(m.getName() + " has revealed as the mayor!");
    }
    
    public void onAssassination(Player assassin, Player victim, Message e){
    	broadcast(assassin.getName() + " assassinated " + victim.getDescription());
    }

    public void onMessageReceive(Player receiver, Message s){
    	
    }

    public void onVote(Player p, Player q, int toLynch, Message e){
    	broadcast(e.access(Message.PUBLIC, false));
    }

    public void onUnvote(Player p, Player q, int toLynch, Message e){
    	broadcast(e.access(Message.PUBLIC, false));
    }

    public void onChangeVote(Player voter, Player prevTarget, Player target, int toLynch, Message e) {
    	broadcast(e.access(Message.PUBLIC, false));
    }

    
}
