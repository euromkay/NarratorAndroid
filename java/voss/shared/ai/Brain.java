package voss.shared.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;


public class Brain {

    PlayerList masters;
    Random random;
    public PlayerList slaves;
    public Brain(PlayerList masters, Random random){
        this.masters = masters;
        mafKiller = new HashMap<>();
        this.random = random;
        
        slaves = new PlayerList();
        namesInBrain = new ArrayList<>();
    }
    
    protected boolean targetAnyone = false;
    public void setTargetAnyone(boolean b){
    	targetAnyone = b;
    }
    
    ArrayList<String> namesInBrain;
    public void addSlave(Player p, Controller controller){
    	if(namesInBrain.contains(p.getName()))
    		return;
    	namesInBrain.add(p.getName());
    		
        Computer c = new Computer(p, this, controller);
        map.put(p, c);
        slaves.add(p);
    }
   
    public int size(){
    	return map.size();
    }
   
    private HashMap<Player, Computer> map = new HashMap<>();
    
    public Computer getComputer(Player p){
        Computer c = map.get(p);
        //if (c == null && !p.equals(p.getSkipper()))
            //throw new NullPointerException(p.getName() + " - name");

        return c;
    }

    private Player[] voteChoices;
    private Player skipper;
    private boolean firstTimeThrough = true;
    public void dayAction(){
    	if(slaves.isEmpty())
    		return;
    	skipper = slaves.get(0).getSkipper();
    	if(firstTimeThrough){
    		initialDayAction();
    		firstTimeThrough = false;
    	}else{
    		skipDay();
    		firstTimeThrough = true;
    	}
        
    }
    private void initialDayAction(){
        PlayerList choices;
        if(!targetAnyone)
        	choices = slaves.copy().getLivePlayers();
        else
        	choices = slaves.get(0).getNarrator().getLivePlayers();
        
        choices.add(slaves.get(0).getSkipper());
        
        if (choices.size() < 2)
            return;

        voteChoices = new Player[2];

        voteChoices[0] = choices.getRandom(random);
        choices.remove(voteChoices[0]);
        voteChoices[1] = choices.getRandom(random);
        choices.remove(voteChoices[1]);



        if (voteChoices[0] != skipper) {
        	doDayAction(voteChoices[0]);
        }
        if (voteChoices[1] != skipper) {
            doDayAction(voteChoices[1]);
        }


        choices.remove(skipper);
        for (Player slave: choices){
        	doDayAction(slave);
        }
    }
    
    private void skipDay(){
    	if (skipper.getNarrator().isNight() || !skipper.getNarrator().isInProgress())
            return;

        //blackmailing is stopping this
        if (!mastersExist() && skipper.getNarrator().isDay()){
            for (Player slave: slaves){
            	doDayAction(slave);
            }
        }
    }
    
    private void doDayAction(Player p){
    	Narrator n = p.getNarrator();
    	if (p.isDead())
    		return;
    	if (n.isNight() || !n.isInProgress())
            return;
    	Computer comp = getComputer(p);
    	if(comp != null){
    		comp.doDayAction();
    	}
    }
    
    public void nightAction(){
        for (Player c: slaves.getLivePlayers().remove(masters).sortByName()) {
            Computer comp = getComputer(c);
            if(comp != null && c.getNarrator().isNight())
            	comp.doNightAction();
        }
        reset();
    }
    
    protected boolean mastersExist(){
        return !masters.isEmpty();
    }
    
    
    public Player[] getDayChoices(){
        return voteChoices;
    }

    private HashMap<Integer, Player> mafKiller; 
	public void reset() {
		mafKiller.clear();
	}

	public Player getMafSender(Player slave) {
		Player killer = mafKiller.get(slave.getAlignment());
		if(killer != null)
			return slave.getNarrator().getPlayerByName(killer.getName());
		
		PlayerList aliveTeammates = slave.getTeam().getMembers().getLivePlayers();
		for(Player poss: aliveTeammates){
			if(poss.getAbilityCount() == 0){
				killer = poss;
				return killer;
			}
		}
		killer = aliveTeammates.getRandom(random);
        mafKiller.put(slave.getAlignment(), killer);
		return killer;
	}

	public void endGame() {
		if(slaves.isEmpty())
			return;
		Narrator n = slaves.get(0).getNarrator();
		while(n.isInProgress()){
			if(n.isDay())
				dayAction();
			else
				nightAction();
		}
	}
}