package voss.shared.logic.support;

import java.util.HashMap;


public class StringChoice {

    private String def;
    private HashMap<Integer, String> idToString = new HashMap<>();

    public StringChoice(String def){
        this.def = def;
    }

    public String getString(int level){
        String ret = idToString.get(level);
        if(ret == null)
            ret = def;
        return ret;
    }

    public StringChoice add(int level, String word){
        idToString.put(level, word);
        return this;
    }

}
