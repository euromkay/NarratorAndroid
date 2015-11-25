package voss.shared.ai;

import java.util.HashMap;
import java.util.Random;

import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.Team;
import voss.shared.logic.exceptions.PlayerTargetingException;


public class Brain {

    PlayerList masters;
    Random random;
    PlayerList slaves;
    public Brain(PlayerList masters, Random random){
        this.masters = masters;
        mafKiller = new HashMap<>();
        this.random = random;
        
        slaves = new PlayerList();
    }
    
    public void addSlave(Player p, Controller controller){
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
        if (c == null && !p.equals(p.getSkipper()))
            throw new NullPointerException(p.getName() + " - name");

        return c;
    }

    private Player[] voteChoices;
    private Player skipper;
    private boolean firstTimeThrough = true;
    public void dayAction(){
    	skipper = slaves.get(0).getSkipper();
    	if(firstTimeThrough){
    		initialDayAction();
    		firstTimeThrough = false;
    	}else{
    		skipDay();
    	}
        
    }
    private void initialDayAction(){
        PlayerList choices = slaves.copy().getLivePlayers().add(skipper);
        
        if (choices.size() < 2)
            return;

        voteChoices = new Player[2];

        voteChoices[0] = choices.getRandom(random);
        choices.remove(voteChoices[0]);
        voteChoices[1] = choices.getRandom(random);
        choices.remove(voteChoices[1]);

        Computer current;


        if (voteChoices[0] != skipper) {
            current = getComputer(voteChoices[0]);
            current.doDayAction();
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
    		try{
    			comp.doDayAction();
    		}catch(PlayerTargetingException e){
    			e.printStackTrace();
    		}
    	}
    }
    
    public void nightAction(){
        for (Player c: slaves.getLivePlayers().remove(masters).sortByName()) {
            Computer comp = getComputer(c);
            if(comp != null)
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
			return slave.getNarrator().getPlayerByID(killer.getID());
		
		Team t = slave.getTeam();
		killer = t.getMembers().getLivePlayers().getRandom(random);
        mafKiller.put(slave.getAlignment(), killer);
		return killer;
	}
}