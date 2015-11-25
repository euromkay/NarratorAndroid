package voss.shared.roles;

import java.util.ArrayList;

import voss.shared.logic.Event;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Rules;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.UnsupportedMethodException;
import voss.shared.logic.listeners.NarratorListener;
import voss.shared.logic.support.Constants;

public class Arsonist extends Role {


	public Arsonist(Player p) {
		super(p);
		Rules r = p.getNarrator().getRules();
		p.setImmune(r.arsonInvlunerable);
		setBool(1, r.arsonDayIgnite);
	}

	

	public static final String ROLE_NAME = "Arsonist";
	public static final String BURN = "burn";
	public static final String DOUSE = "douse";
	public static final String UNDOUSE = "undouse";
	public String getRoleName() {
		return ROLE_NAME;
	}
	
	public String getKeyWord(){
		throw new UnsupportedMethodException("");
	}
	
	public String getNightText(ArrayList<Team> t) {
		return "  Type " + NQuote(DOUSE) + " to douse someone, " + NQuote(UNDOUSE) + " to undouse them, or or " + SQuote(BURN) +" to ignite everyone.";
	}


	public static final String NIGHT_ACTION_DESCRIPTION = "You have the ability to douse someone in flammable gasoline, undouse them, or burn everyone you previously doused.";
	public String getRoleInfo() {
		return NIGHT_ACTION_DESCRIPTION;
	}


	public static final String DEATH_FEEDBACK = "You were burned to death by an Arsonist";
	public ArrayList<String> getAbilities() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(DOUSE);
		list.add(UNDOUSE);
		list.add(BURN);
		return list;
	}

	
	public static final int DOUSE_ = MAIN_ABILITY;
	public static final int UNDOUSE_ = SECONDARY_ABILITY;
	public static final int BURN_ = TERTIARY_ABILITY;

	
	public void setAction(Player owner, Player target, int ability, boolean simulation){
		Team t = owner.getTeam();
		
		Event e = new Event();
		e.add(owner);
		e.setPrivate();
		if(ability == DOUSE_){
			e.setCommand(owner, DOUSE, target.getName());
			if(simulation)
				return;
			
			e.add(" will douse ", target, ".");
			//t.notifyTeammates(owner, " will douse " + target.getName() + ".");

			owner.getNarrator().addEvent(e);
		}else if(ability == BURN_){
			e.setCommand(owner, BURN, target.getName());
			if(simulation)
				return;
			
			e.add(" will ignite those doused.");
			//t.notifyTeammates(owner, " will ignite those doused.");

			owner.getNarrator().addEvent(e);
		}else if(ability == UNDOUSE_){
			e.setCommand(owner, UNDOUSE, target.getName());
			if(simulation)
				return;
			e.add(" will undouse ", target, ".");
			//t.notifyTeammates(owner, " will undouse " + target.getName() + ".");
			owner.getNarrator().addEvent(e);
		}else
			t.getSelectionFeedback(owner, target, ability, simulation);
	}
	
	public void isAcceptableTarget(Player owner, Player target, int ability) {
		deadCheck(target);
		allowedAbilities(ability, DOUSE_, BURN_, UNDOUSE_);
		
		boolean hasBurnTarget = owner.getTarget(BURN_) != null;
		boolean hasDouseTarget = owner.getTarget(DOUSE_) != null;
		boolean hasUndouseTarget = owner.getTarget(UNDOUSE_) != null;
		
		if(ability == DOUSE_ && (hasBurnTarget || hasUndouseTarget))
			Exception("You cant douse if you've already chosen to burn or douse someone tonight");

		if(ability == BURN_ && burnedDuringDay())
			Exception("You can't burn again if you burned during the day");

		if(ability == BURN_ && (hasDouseTarget || hasUndouseTarget))
			Exception("You can't burn if you've already selected someone to douse or undouse.");

		if(ability == UNDOUSE_ && (hasBurnTarget || hasDouseTarget))
			Exception("You can't undouse if you've already selected someone to douse or burn");
	}
	private boolean secondRound(){
		return getBool(0);
	}
	public boolean doNightAction(Player owner, Narrator n) {
		Player undouse = owner.getTarget(UNDOUSE_);
		Player douse = owner.getTarget(DOUSE_);
		Player ignite = owner.getTarget(BURN_);
		
		if (undouse == null && douse == null && ignite == null)
			return true;
		
		if(secondRound()){
			if(ignite != null){
				owner.visit(owner);
				burn(owner, n);
				setBurnedLastNight();
			}
			return true;
		}else{
			setFirstRound(true);
			if(douse == null && ignite == null){
				if(undouse.isDoused()){
					undouse.setDoused(false);
					Role.event(owner, " undoused ", undouse);
					owner.visit(undouse);
				}
				return false;
			}else{//douse someone
				if(douse != null){
					owner.visit(douse);
					douse.setDoused(true);
					Role.event(owner, " doused ", douse);
				}
					
				return false;
			}
		}
		
		
		
	}
	private void setFirstRound(boolean b){
		setBool(0, b);
	}
	public void dayReset(Player p){
		setFirstRound(false);
		if (getBool(3))
			setBool(3, false);
	}
	public void nightReset(){
		if (getBool(2))
			setBool(2, false);
	}
	private boolean getBurnedLastNight(){
		return getBool(2);
	}
	private void setBurnedLastNight(){
		setBool(2, true);
	}
	
	private void setBurnedDuringDay(){
		setBool(3, true);
	}
	
	public int getAbilityCount(){
		return 3;
	}
	private boolean burnedDuringDay(){
		return getBool(3);
	}
	
	private boolean hasDayIgnite(){
		return getBool(1);
	}
	public boolean hasDayAction(){
		return hasDayIgnite() && !getBurnedLastNight();
	}
	public void doDayAction(Player owner, Narrator n, boolean simulation){
		Event e = new Event();
		e.setCommand(owner, BURN);
		
		if(simulation)
			return;
		
		PlayerList burned = burn(owner, n);
		if(burned.isEmpty())
			e.add("There was an explosion but no one was found dead.");
		else if(burned.size() == 1)
			e.add("There was an explosion and ", burned.get(0), " was found twitching on the floor. Dead.");
		else
			e.add("There was a huge explosion, and ", burned.getDeadPlayers(), " were found dead.");
		owner.getNarrator().addEvent(e);
		//it might be possible that arsons wont be able to douse the next night because they just burned 
		setBool(1, false);
		setBurnedDuringDay();

		n.checkVote();
		if(n.isDay())
			for (NarratorListener nL: n.getListeners()){
				nL.onArsonDayBurn(owner, burned);
			}
	}
	private PlayerList burn(Player owner, Narrator n){
		PlayerList dousedTargets = new PlayerList();
		for(Player p: n.getLivePlayers()){
			if(p.isDoused()){
				Kill(owner, p, Constants.ARSON_KILL_FLAG, n);
				//if youre target isn't an arson, or if they're you, or if they weren't about to burn
				if (!p.is(Arsonist.ROLE_NAME) || p == owner || p.getTarget(BURN_) == null)
					p.setDoused(false);
				dousedTargets.add(p);
			}
		}
		Event e = new Event();
		e.setPrivate();
		e.add(owner, " ignited, killing ", dousedTargets, ".");

		if (n.isDay()){
			n.removeFromVotes(dousedTargets);
		}

		return dousedTargets;
	}
}
