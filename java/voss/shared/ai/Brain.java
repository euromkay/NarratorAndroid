package voss.shared.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

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
        mafKillTarget = new HashMap<>();
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

    private Player skipper;
    private boolean firstTimeThrough = true;
    public void dayAction(){
    	if(slaves.isEmpty() || !slaves.get(0).getNarrator().isDay())
    		return;
    	skipper = slaves.get(0).getSkipper();
    	if(firstTimeThrough){
    		talkings();
    		firstTimeThrough = false;
    	}else{
    		voting();
    	}
        
    }
    private void talkings(){
    	for(Player s: slaves.getLivePlayers()){
    		getComputer(s).talkings();
    	}
    	PlayerList list;
    	if(targetAnyone)
    		list = slaves.get(0).getNarrator().getLivePlayers();
    	else
    		list = slaves.getLivePlayers();
    	for(Player s: slaves){
    		getComputer(s).vote(list);
    	}
    }
    
    
    private void voting(){
    	if(slaves.isEmpty())
    		return;
    	PlayerList livePlayers = slaves.get(0).getNarrator().getLivePlayers();
    	
    	int min = slaves.get(0).getNarrator().getMinLynchVote();
    	boolean dub = false;
    	for(Player p: livePlayers){
    		int voteCount = p.getVoteCount();
    		if(voteCount == 0){
    			continue;
    		}
    		
    		if(voteCount < min){//if i find a new low non0 
    			min = voteCount;
    			dub = false;
    		}else if(voteCount == min){
    			dub = true;
    		}
    	}

    	
    	PlayerList choices = new PlayerList();
    	PlayerList needToChange = new PlayerList();
    	for(Player p: livePlayers){
    		if(p.getVoteCount() > min){
    			if(targetAnyone || slaves.contains(p))
    				choices.add(p);
    		}else if (p.getVoteCount() == min){
    			if(dub){
	    			if(targetAnyone || slaves.contains(p))
	    				choices.add(p);
    			}
    			for(Player toChange: p.getVoters())
    				if(slaves.contains(toChange))
    					needToChange.add(toChange);
    		}else{
    			for(Player toChange: p.getVoters())
    				if(slaves.contains(toChange))
    					needToChange.add(toChange);
    		}
    	}
    	
    	Player voted = null;
    	for(Player p: needToChange){
    		if(p.isComputer()){
    			p = getComputer(p).vote(choices);
    			if(p != null)
    				voted = p;
    		}
    	}
    	
    	if(voted == null){
            //blackmailing is stopping this
            if (!mastersExist() && skipper.getNarrator().isDay()){
                for (Player slave: slaves){
                	if(!slave.getNarrator().isDay())
                		return;
                	if(!slave.isComputer())
                		continue;
                	if(slave.getVoteTarget() != slave.getSkipper())
                		getComputer(slave).controller.skipVote(slave);
                }
            
            }
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

    private HashMap<Integer, Player> mafKiller;
    private HashMap<Integer, Player> mafKillTarget;
	public void reset() {
		mafKiller.clear();
		mafKillTarget.clear();
		firstTimeThrough = true;
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
	
	public Player getMafKillTarget(Player slave) {
		Player killTarget = mafKillTarget.get(slave.getAlignment());
		if(killTarget != null)
			return slave.getNarrator().getPlayerByName(killTarget.getName());
		
		PlayerList aliveTeammates = slave.getTeam().getMembers().getLivePlayers();
		PlayerList alive = slave.getNarrator().getAllPlayers().getLivePlayers();
		
		killTarget = alive.compliment(aliveTeammates).getRandom(random);
		mafKillTarget.put(slave.getAlignment(), killTarget);
		return killTarget;
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

	public ArrayList<Claim> claims = new ArrayList<>();
	public void claim(Player target, Team t, Player slave) {
		Claim c = new Claim(target, t, slave);
		if(!slave.isBlackmailed())
			claims.add(c);
		
	}

	
}