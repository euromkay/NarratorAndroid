package voss.shared.logic.support;

import java.util.HashMap;

import voss.shared.logic.Player;


public class StringChoice {

    private String def;
    private HashMap<String, String> idToString = new HashMap<>();

    public StringChoice(String def){
        this.def = def;
    }

    public String getString(String level){
        String ret = idToString.get(level);
        if(ret == null)
            ret = def;
        return ret;
    }

    public StringChoice add(String level, String word){
        idToString.put(level, word);
        return this;
    }
    
    public StringChoice add(Player p, String word){
    	return add(p.getName(), word);
    }

}
