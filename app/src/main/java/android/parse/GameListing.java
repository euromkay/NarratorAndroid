package android.parse;


import java.util.List;

import com.parse.ParseObject;

import android.ActivitySettings;
import android.setup.SetupDeliverer;
import shared.logic.Rules;
import shared.packaging.Packager;

public class GameListing {

    public static final String ID = "keyToStartActivityDay";

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

    public Rules getRules(){

        String compress = parse.getString("rules");
        if(compress == null || compress.length() == 0)
            return ActivitySettings.getRules();
        SetupDeliverer sd = new SetupDeliverer(compress);
        Packager p = new Packager(sd);
        try {
            return new Rules(p);
        }catch(ArrayIndexOutOfBoundsException e){
            return new Rules();
        }

    }
}

