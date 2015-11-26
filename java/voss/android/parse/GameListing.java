package voss.android.parse;


import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class GameListing {

    private ParseObject parse;
    public GameListing(ParseObject parse){
        this.parse = parse;
    }

    private String hostName;

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName(){
        return hostName;
    }

    private List<String> players;

    public void setPlayers(List<String> players) {
        this.players = players;
    }
    public List<String> getPlayerNames(){
        return players;
    }


    private List<String> roles;

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public  List<String> getRoleNames(){
        return roles;
    }

    public String getHeader() {
        return hostName + " (" + players.size() + "/" + roles.size() + ")";
    }
    private void addStrings(List<String> from, ArrayList<String> to){
        for(String f: from)
            to.add(f);
    }

    public String getID(){
        return parse.getObjectId();
    }

    public boolean inProgress(){
        return parse.getBoolean(ParseConstants.STARTED);
    }
}
