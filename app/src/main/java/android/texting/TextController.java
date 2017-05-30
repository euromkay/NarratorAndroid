package android.texting;

import shared.ai.Controller;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.exceptions.UnsupportedMethodException;
import shared.logic.support.Constants;
import shared.roles.Arsonist;
import shared.roles.Assassin;
import shared.roles.DrugDealer;
import shared.roles.Framer;
import shared.roles.Jailor;
import shared.roles.Mayor;
import shared.roles.Role;
import shared.roles.Tailor;
import shared.roles.Ventriloquist;
import shared.roles.Veteran;



public class TextController implements Controller {

    protected TextInput texter;
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
    	b = Narrator.Translate(a.getNarrator(), b);
        texter.text(a, action + " " + b.getName(), ASYNC);
    }

    public void setNightTarget(Player a, Player b, String action, String teamName) {
    	b = Narrator.Translate(a.getNarrator(), b);
        texter.text(a, action + " " +  b.getName() + " " + teamName, ASYNC);
    }
    
    public void cancelNightTarget(Player a, PlayerList b, String action) {
    	String text = action;
    	for(Player x: b){
    		text += (" " + x.getName());
    	}
		texter.text(a, text, ASYNC);
	}

	public void clearTargets(Player p){

	}

	public void frame(Player framer, String option, Player framed){
		texter.text(framer, Framer.FRAME + " " + option + " " + framed.getName(), ASYNC);
	}
	public void drug(Player dd, String option, Player dealt){
		texter.text(dd, DrugDealer.COMMAND + " " + option + " " + dealt.getName(), ASYNC);
	}
	
	public void spy(Player framer, String team){
		texter.text(framer, Framer.FRAME + " " + team, ASYNC);
	}
	
	public void suit(Player tailor, String team, String role, Player suited){
		texter.text(tailor, Tailor.COMMAND + " " + team + " " + role + " " + suited.getName(), ASYNC);
	}
	
	public void vest(Player vester){
		texter.text(vester, Constants.VEST_COMMAND, ASYNC);
	}

    public Player vote(Player slave, Player target) {
    	target = Narrator.Translate(slave.getNarrator(), target);
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
		if(p.is(Jailor.class)){
			texter.text(p, Jailor.JAIL + " " + target.getName(), SYNC);
			return;
		}
		throw new UnsupportedMethodException();
	}

	public void unvote(Player slave) {
		texter.text(slave, TextHandler.UNVOTE, SYNC);
	}

	public void cancelDayAction(Player slave, int i) {
		texter.text(slave, Constants.CANCEL + " " + (i), SYNC);
	}

	public void ventVote(Player vent, Player puppet, Player target) {
		texter.text(vent, Ventriloquist.VENT_VOTE + " " + puppet.getName() + " " + target.getName(), SYNC);
	}

	public void ventUnvote(Player vent, Player puppet) {
		texter.text(vent, Ventriloquist.VENT_UNVOTE + " " + puppet.getName(), SYNC);
	}

	public void ventSkipVote(Player vent, Player puppet) {
		texter.text(vent, Ventriloquist.VENT_SKIP_VOTE + " " + puppet, SYNC);
	}

	
}
