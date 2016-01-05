package voss.android.texting;

import voss.shared.ai.Controller;
import voss.shared.logic.Player;
import voss.shared.logic.exceptions.UnsupportedMethodException;
import voss.shared.logic.support.Constants;
import voss.shared.roles.Arsonist;
import voss.shared.roles.Mayor;



public class TextController extends Controller {

    private TextInput texter;
    public TextController(TextInput texter){
        this.texter = texter;
    }

    public void log(String string) {

    }

	public static final boolean SYNC = true;
	public static final boolean ASYNC = false;

    public void endNight(Player slave) {
        texter.text(slave, TextHandler.END_NIGHT, SYNC);
    }

    public void cancelEndNight(Player slave) {
        endNight(slave);
    }

    public void setNightTarget(Player a, Player b, String action) {
    	b = Translate(a.getNarrator(), b);
        texter.text(a, action + " " + b.getName(), ASYNC);
    }

    public void setNightTarget(Player a, Player b, String action, String teamName) {
    	b = Translate(a.getNarrator(), b);
        texter.text(a, action + " " +  b.getName() + " " + teamName, ASYNC);
    }
    
    public void removeNightTarget(Player a, String action) {
		texter.text(a, action + " " + Constants.UNTARGET, ASYNC);
	}

    public Player vote(Player slave, Player target) {
    	target = Translate(slave.getNarrator(), target);
    	if(target == slave.getSkipper())
    		skipVote(slave);
    	else
    		texter.text(slave, TextHandler.VOTE + " " + target.getName(), SYNC);
        return target;
    }

    public void selectHost(Player host) {

    }
    
    public Player skipVote(Player a){
    	texter.text(a, TextHandler.SKIP_VOTE, SYNC);
    	return a.getSkipper();
    }


	public void say(Player slave, String message, String key) {
		texter.text(slave, TextHandler.SAY + " " + key + " " + message, ASYNC);
	}


	public void doDayAction(Player p) {
		if(p.is(Mayor.ROLE_NAME)){
			texter.text(p, Mayor.REVEAL, SYNC);
			return;
		}
		else if(p.is(Arsonist.ROLE_NAME)){
			texter.text(p, Arsonist.BURN, SYNC);
			return;
		}
		throw new UnsupportedMethodException();
	}

	public void unvote(Player slave) {
		texter.text(slave, TextHandler.UNVOTE, SYNC);
		
	}

	
}
