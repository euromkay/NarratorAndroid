package android.texting;

import shared.ai.Controller;
import shared.logic.Player;
import shared.logic.exceptions.UnsupportedMethodException;
import shared.roles.Arsonist;
import shared.roles.Assassin;
import shared.roles.Framer;
import shared.roles.Mayor;
import shared.roles.Role;
import shared.roles.Veteran;



public class TextController implements Controller {

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

    public void setNightTarget(Player a){
    	if(a.is(Veteran.class))
    		texter.text(a, Veteran.ALERT, ASYNC);
    	else if(a.is(Arsonist.class))
    		texter.text(a, Arsonist.BURN, ASYNC);
    }
    
    public void setNightTarget(Player a, Player ...b ){
    	StringBuilder parts = new StringBuilder();
    	parts.append(a.reverseParse(Role.MAIN_ABILITY));
    	parts.append(" ");
    	
    	for(int i = 1; i <= b.length; i++){
    		parts.append(b[i].getName());
    		if(i != b.length)
    			parts.append(" ");
    	}
    	texter.text(a, parts.toString(), ASYNC);
    }
    
    public void setNightTarget(Player a, Player b, String action) {
    	b = Controller.Translate(a.getNarrator(), b);
        texter.text(a, action + " " + b.getName(), ASYNC);
    }

    public void setNightTarget(Player a, Player b, String action, String teamName) {
    	b = Controller.Translate(a.getNarrator(), b);
        texter.text(a, action + " " +  b.getName() + " " + teamName, ASYNC);
    }
    
    public void cancelNightTarget(Player a, Player b, String action) {
		texter.text(a, action + " " +  b.getName(), ASYNC);
	}

	public void clearTargets(Player p){

	}

	public void frame(Player framer, String option, Player framed){
		texter.text(framer, Framer.FRAME + " " + option + " " + framed.getName(), ASYNC);
	}
	
	public void spy(Player framer, String team){
		texter.text(framer, Framer.FRAME + " " + team, ASYNC);
	}

    public Player vote(Player slave, Player target) {
    	target = Controller.Translate(slave.getNarrator(), target);
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
		if(p.is(Mayor.class)){
			texter.text(p, Mayor.REVEAL, SYNC);
			return;
		}
		else if(p.is(Arsonist.class)){
			texter.text(p, Arsonist.BURN, SYNC);
			return;
		}
		throw new UnsupportedMethodException();
	}
	
	public void doDayAction(Player p, Player target){
		if(p.is(Assassin.class)){
			texter.text(p, Assassin.ASSASSINATE + " " + target.getName(), SYNC);
			return;
		}
		throw new UnsupportedMethodException();
	}

	public void unvote(Player slave) {
		texter.text(slave, TextHandler.UNVOTE, SYNC);
		
	}

	
}
