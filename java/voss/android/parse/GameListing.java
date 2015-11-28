package voss.android.parse;


import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

public class GameListing {

    private ParseObject parse;
    public GameListing(ParseObject parse){
        this.parse = parse;
    }

    private String hostName;

    public String getHostName(){
        return parse.getString(ParseConstants.INSTANCE_HOST_KEY);
    }

    private List<String> players;

    public List<String> getPlayerNames(){
        return parse.getList(ParseConstants.PLAYERS);
    }


    public  List<String> getRoleNames(){
        return parse.getList(ParseConstants.ROLES);
    }

    public String getHeader() {
        return getHostName() + " (" + getPlayerNames().size() + "/" + getRoleNames().size() + ")";
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

    public List<String> getCommands(){
        return parse.getList(ParseConstants.EVENTS);
    }

    public long getSeed(){
        return parse.getLong(ParseConstants.SEED);
    }

    public ParseObject getParseObject(){
        return parse;
    }

}

