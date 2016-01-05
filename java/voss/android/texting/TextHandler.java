package voss.android.texting;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.ContextWrapper;
import voss.android.PhoneBook;
import voss.android.day.DayManager;
import voss.shared.event.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.IllegalActionException;
import voss.shared.logic.exceptions.PhaseException;
import voss.shared.logic.exceptions.PlayerTargetingException;
import voss.shared.logic.exceptions.UnknownPlayerException;
import voss.shared.logic.exceptions.UnknownTeamException;
import voss.shared.logic.exceptions.VotingException;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.CommandHandler;
import voss.shared.logic.support.RoleTemplate;


public class TextHandler extends CommandHandler implements NarratorListener, TextInput {


	private TextInput tc;
    public TextHandler(Narrator n, DayManager tc){
        super(n);
        n.addListener(this);
        if(tc.isHost())
        	this.tc = tc;
        String message;
        Team t;
        for(Player texter: getTexters(n.getAllPlayers())){
            message = "You are a " + texter.getRoleName() + ".";
            t = texter.getTeam();
            if(!t.isSolo())
                message += " You are part of the " + t.getName() + ".";
            if(texter.getTeam().knowsTeam()){
                message += " Your teammates are " +t.getMembers().sortByName().toString() + ".";
            }
            texter.sendMessage(message);
        }
        sendHelpPrompt();
        if(n.isDay())
            onDayStart(null);
        else
            onNightStart(null);
    }

    private void sendHelpPrompt(Player owner){
        String message = "See roles List - " + ROLE_INFO;
        message += "\nSee live players - " + LIVE_PEOPLE;
        if(owner.getTeam().knowsTeam())
            message += "\nSee team members - " + TEAM_INFO;
        message += "\nGet day help - " + DAY_HELP;
        message += "\nGet night help - " + NIGHT_HELP;
        owner.sendMessage(message);
    }

    private static final String HELP = "help";
    public static final String ROLE_INFO = "roles list";
    public static final String LIVE_PEOPLE = "get players";
    public static final String TEAM_INFO = "get team";
    public static final String NIGHT_HELP = "night help";
    public static final String DAY_HELP = "help day";


    public void text(Player owner, String message, boolean sync){
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
            case NIGHT_HELP:
                sendNightTextPrompt(owner);
                return;
            case DAY_HELP:
                sendDayTextPrompt(owner);
                return;
            default:
                try{
                	synchronized(n){
                		super.command(owner, message, "text");	
                		if(tc != null)
                			tc.text(owner, message, sync);
                	}
                }catch(IllegalActionException e){
                    if (e.getMessage().length() == 0){
                        owner.sendMessage("Unknown command.  Type " + HELP + " to see a list of commands.");
                    }else{
                        owner.sendMessage(e.getMessage());
                    }

                    printException(e);
                }catch(UnknownPlayerException f){
                    owner.sendMessage("Unknown player name. Type "  + SQuote(LIVE_PEOPLE) + " to get a list of players.");
                    printException(f);

                }catch(UnknownTeamException e){
                    owner.sendMessage( "Unknown team name. Type " + NIGHT_HELP + " to get info about what you can do during the night.");
                    printException(e);

                }catch (PlayerTargetingException g){
                    owner.sendMessage( g.getMessage());
                    printException(g);

                }catch (PhaseException e){
                    owner.sendMessage(e.getMessage());
                    printException(e);

                }catch (VotingException e){
                    owner.sendMessage(e.getMessage());
                    printException(e);
                }
        }
    }

    private void printException(Throwable e){
    	System.err.println(e.getMessage());
    }
    
    public static Player findName(ArrayList<String> blocks, PhoneBook phonebook){
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
    }




    private void sendRoleInfo(Player owner){
        String roles_list_message = "These are the roles in game:\n";
        for(RoleTemplate r : n.getAllRoles()){
            if(r.isRandom())
                roles_list_message += r.getName() + "\n";
            else
                roles_list_message += n.getTeam(r.getColor()) + " " + r.getName() + "\n";
        }
        roles_list_message = roles_list_message.substring(0, roles_list_message.length() - 1);
        owner.sendMessage( roles_list_message);
    }
    private void sendLivePlayerList(Player owner){
        String list_of_texters = "These are the list of people in the game: ";
        for(Player p: n.getLivePlayers())
            list_of_texters += (p.getName() + ", ");
        owner.sendMessage(list_of_texters);
    }
    private void sendTeamInfo(Player p){
        if (p.getTeam().knowsTeam()) {
            Team t = p.getTeam();
            PlayerList team = t.getMembers();
            if(team.isEmpty()){
                p.sendMessage("You dont' have any teammembers.");
            }else {
                PlayerList living = team.getLivePlayers();
                if(living.isEmpty()){
                    p.sendMessage("All your teammates died");
                }else
                    p.sendMessage("You're teammates are: " + p.getTeam().getMembers().getLivePlayers().sortByName().toString() + ".");
            }
        }else
            p.sendMessage( "I can't tell you who's on your team.");
    }

    private void sendNightPrompt(Player p){
        String message = SQuote(END_NIGHT)  + " so night can end.";
        if (p.getAbilities().length == 0){
            p.sendMessage( "It is now  time. Type " + message );
        }else{
            p.sendMessage( "It is now nighttime. Submit your night action.  When you're done, type " + message);
        }
    }

    public void onEndGame(){
        getTexters(n.getAllPlayers()).sendMessage( n.getWinMessage().access(Event.PUBLIC, false));
    }

    private void sendHelpPrompt(){
        getTexters(n.getAllPlayers()).sendMessage( "To see a list of possible commands, text " + SQuote(HELP) + ".");
    }


    public static PlayerList getTexters(PlayerList playerList){
        PlayerList list = new PlayerList();
        for(Player p: playerList){
            if(isTexter(p))
                list.add(p);
        }

        return list;
    }

    private static boolean isTexter(Player p){
        if (p.getCommunicator() == null)
            return false;
        return p.getCommunicator().getClass() == CommunicatorText.class;
    }

    public void sendNightTextPrompt(Player texter){
        texter.sendMessage( texter.getNightText());
        texter.sendTeamTextPrompt();
        texter.sendMessage("To talk to your allies : -  " + SQuote(SAY + " message") + ".");
        texter.sendMessage( "After you're done submitting actions, text " + SQuote(END_NIGHT) + " so night can end.  If you want to cancel your bid to end night, type it again.");
    }

    public void sendDayTextPrompt(Player texter){
        String message = "To vote or change your vote : - " + SQuote(VOTE + " name");
        message += ".\nTo unvote: - " + SQuote(UNVOTE);
        message += ".\nTo end the day so that no one is lynched : - " + SQuote(SKIP_VOTE) + ".";

        texter.sendMessage( message);
    }

    public void onModKill(Player p){
        getTexters(n.getAllPlayers()).sendMessage(p + " suicided!");
    }

    public void onDayStart(PlayerList dead){
        PlayerList votees = getTexters(n.getAllPlayers());
        if(dead != null){
            if(dead.isEmpty())
                votees.sendMessage("No one died!");
            else{
                for(Player deadPerson: dead){
                    votees.sendMessage(deadPerson.getDescription() + " was found " +deadPerson.getDeathType().toString() + ".");
                }
            }
        }

        for(Player p: getTexters(n.getLivePlayers())){
            sendDayPrompt(p);
        }

    }

    private void sendDayPrompt(Player p){
        String message = "It is now daytime.  ";
        if(p.isBlackmailed())
            message += "You're blackmailed. You can't talk and can only vote to skip the day.";
        else
            message += "Please vote.  Once someone has enough votes, the day will end.";
        p.sendMessage( message);
    }

    public void onNightStart(PlayerList dead){
        PlayerList votees = getTexters(n.getAllPlayers());
        if(dead != null) {
            if (dead.isEmpty() || dead.get(0) == n.Skipper)
                votees.sendMessage(n.Skipper.getVoters().getStringName() + " opted to skip lynching today.");
            else {
                for (Player deadPerson : dead) {
                    votees.sendMessage(deadPerson.getDescription() + " was lynched by " + deadPerson.getVoters().getStringName() + ".");
                }
            }
        }
        for(Player p: n.getLivePlayers()){
            sendNightPrompt(p);
        }
    }

    public static String SQuote(String s){
        return "\'" + s + "\'";
    }

    public void onArsonDayBurn(Player arson, PlayerList burned){

    }

    public void onNightTarget(Player p, Player t){

    }

    public void onNightTargetRemove(Player p, Player r){

    }

    public void onEndNight(Player p) {

    }

    public void onCancelEndNight(Player p) {

    }

    public void onMayorReveal(Player m){
        getTexters(n.getAllPlayers()).sendMessage(m.getName() + " has revealed as the mayor!");
    }

    public void onMessageReceive(Player p){

    }

    public void onVote(Player p, Player q, int toLynch){
        getTexters(n.getAllPlayers()).sendMessage(p.getName() + " voted for " + q.getName() + "." + numberOfVotesNeeded(toLynch));
    }

    public void onUnvote(Player p, Player q, int toLynch){
        getTexters(n.getAllPlayers()).sendMessage(p.getName() + " unvoted " + q.getName() + "." + numberOfVotesNeeded(toLynch));
    }

    public void onChangeVote(Player voter, Player prevTarget, Player target, int toLynch) {
        getTexters(n.getAllPlayers()).sendMessage(voter.getName() + " changed their vote from " + prevTarget.getName() + " to " + target.getName() + "." + numberOfVotesNeeded(toLynch));

    }

    public static void stopTexting(ContextWrapper a, BroadcastReceiver b){
        try{
            a.unregisterReceiver(b);
        }catch (IllegalArgumentException e){
        }
    }

    private String numberOfVotesNeeded(int i){
        return Narrator.numberOfVotesNeeded(i);
    }
}
