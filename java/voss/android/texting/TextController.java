package voss.android.texting;

import voss.ai.Controller;
import voss.android.texting.TextHandler;
import voss.android.texting.TextInput;
import voss.logic.Narrator;
import voss.logic.Player;



public class TextController implements Controller {

    private Narrator n;
    private TextInput texter;
    public TextController(Narrator n, TextInput texter){
        this.n = n;
        this.texter = texter;
    }

    public Narrator getNarrator() {
        return n;
    }

    public void log(String string) {

    }


    public void endNight(Player slave) {
        texter.text(slave, TextHandler.END_NIGHT);
    }

    public void cancelEndNight(Player slave) {
        endNight(slave);
    }

    public void setNightTarget(Player a, Player b, String action) {
        texter.text(a, action + " " + b.getName());
    }

    public void setNightTarget(Player a, Player b, String action, String teamName) {
        texter.text(a, action + " " +  b.getName() + " " + teamName);
    }

    public void vote(Player slave, Player target) {
        texter.text(slave, TextHandler.VOTE + " " + target.getName());
        
    }

    public void selectHost(Player host) {

    }
    
    public void skipVote(Player a){
    	texter.text(a, TextHandler.SKIP_VOTE);
    }


	public void say(Player slave, String string) {
		texter.text(slave, TextHandler.SAY + " " + string);
	}
}
