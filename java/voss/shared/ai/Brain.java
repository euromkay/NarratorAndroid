package voss.shared.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import voss.shared.logic.DeathType;
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
		suspList.clear();
	}

	public Player getMafSender(Player slave) {
		Player killer = null;
		for(Team x: slave.getTeams()){
			killer = mafKiller.get(x.getAlignment());
			if(x.hasMember(slave) && killer != null){
				return killer;
			}
		}
		
		Team theTeam = null;
		ArrayList<Team> teams = slave.getTeams();
		for(Team t: teams){
			if(t.canKill()){
				theTeam = t;
				break;
			}
		}
		if(theTeam == null)
			return null;
		for(Player poss: theTeam.getMembers().getLivePlayers()){
			if(poss.getAbilityCount() == 0){
				killer = poss;
				mafKiller.put(theTeam.getAlignment(), killer);
				return killer;
			}
		}
		killer = theTeam.getMembers().getLivePlayers().getRandom(random);
        mafKiller.put(theTeam.getAlignment(), killer);
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
			//if(n.getDeadSize() == 11)
				//System.out.println(n.getHappenings());
			//System.out.println(n.getDeadSize());
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

	HashMap<Team, ArrayList<PlayerList>> suspList = new HashMap<>();
	public ArrayList<PlayerList> getSuspiciousPeople(Team slaveAlignment) {

		ArrayList<PlayerList> ret = suspList.get(slaveAlignment);
		if(ret != null)
			return ret;
		ret = new ArrayList<>();
		Narrator n = slaves.get(0).getNarrator();
		
		HashMap<Player, Integer> scores = new HashMap<Player, Integer>();
		for(Player p: n.getLivePlayers()){
			scores.put(p, 0);
		}
		
		for(Player dead: n.getDeadPlayers()){
			DeathType dt = dead.getDeathType();
			if(!dt.isLynch() || dead.isCleaned())
				continue;
			PlayerList lynch = dt.getLynchers().getLivePlayers();
			Team deadTeam = dead.getTeam();
			
			if(slaveAlignment.isEnemy(deadTeam)){//if person voted for my enemy, i think more highly of them
				for(Player lyncher: lynch){
					increment(scores, lyncher);
				}
				for(Player notLyncher: n.getLivePlayers().remove(lynch)){
					decrement(scores, notLyncher);
				}
			}else{
				for(Player lyncher: lynch){
					decrement(scores, lyncher);
				}
				for(Player notLyncher: n.getLivePlayers().remove(lynch)){
					increment(scores, notLyncher);
				}
			}
		}
		//this gives me a list of all the different types of ratings that exist
		ArrayList<Integer> ratings = new ArrayList<>();
		for(Integer rating: scores.values()){
			if(!ratings.contains(rating))
				ratings.add(rating);
		}

		if(ratings.size() == 1)
			return ret;
		
		//put them 
		Collections.sort(ratings);
		Collections.reverse(ratings);
		
		//fill the return
		for(int i : ratings){
			ret.add(new PlayerList());
		}
		
		for(Player p: n.getLivePlayers()){
			Integer rating = scores.get(p);
			int index = ratings.indexOf(rating);
			ret.get(index).add(p);
		}
		suspList.put(slaveAlignment, ret);
		return ret;
	}
	private void increment(HashMap<Player, Integer> map, Player p){
		map.put(p, map.get(p) + 1);
	}
	private void decrement(HashMap<Player, Integer> map, Player p){
		map.put(p, map.get(p) -1 );
	}

	public boolean gridlock() {
		int greatDeathDay = 0;
		Narrator n = slaves.get(0).getNarrator();
		for(Player p: n.getDeadPlayers()){
			greatDeathDay = Math.max(greatDeathDay, p.getDeathDay());
		}
		if(n.getDayNumber() - greatDeathDay > 10){
			return true;
		}
		return false;
	}
}