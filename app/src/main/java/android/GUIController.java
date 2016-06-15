package android;

import java.util.Random;

import voss.narrator.R;
import android.util.Log;
import android.view.View;
import android.day.ActivityDay;
import android.screens.SimpleGestureFilter;
import android.texting.TextController;
import android.texting.TextInput;
import shared.ai.Controller;
import shared.event.Event;
import shared.logic.Narrator;
import shared.logic.Player;
import shared.logic.PlayerList;
import shared.logic.exceptions.PlayerTargetingException;
import shared.roles.Framer;

public class GUIController extends Controller implements TextInput{

    public ActivityDay dScreen;
    private Random rand;
    private TextController logger;
    public GUIController(ActivityDay dScreen){
        this.dScreen = dScreen;
        rand = new Random();
        logger = new TextController(this);
    }


	public static final boolean VOTE = true;
    public static final boolean END_NIGHT = true;
	public static final boolean SAY = false;
	public static final boolean TARGET = true;
    
    public Player vote(Player slave, Player target){
    	target = Translate(slave.getNarrator(), target);
    	if(VOTE && target != slave.getSkipper())
    		logger.vote(slave, target);
        
        selectSlave(slave);
        actionPanelClick();
        
        try{
        	clickPlayer(target);
        }catch(PlayerTargetingException e){
        	if(slave.getNarrator().isDay()){
        		throw e;
        	}
        }
        return target;
    }

    public Player skipVote(Player slave){
    	if(VOTE)
    		logger.skipVote(slave);
        vote(slave, slave.getSkipper());
        return slave.getSkipper();
    }

    public void setNightTarget(Player slave, Player choice, String ability){
    	choice = Translate(slave.getNarrator(), choice);
    	if(TARGET && !ability.toLowerCase().equals(Framer.FRAME.toLowerCase())){
    		logger.setNightTarget(slave, choice, ability);
    	}
    	selectSlave(slave);
        swipeAbilityPanel(slave, ability);
        clickPlayer(choice);
    }

    public void setNightTarget(Player slave, Player choice, String ability, String teamName){
    	if(TARGET)
    		logger.setNightTarget(slave, choice, ability, teamName);
        setFrame(teamName);
        setNightTarget(slave,choice,ability);
    }

    private void clickPlayer(Player p){
        int position = dScreen.actionList.indexOf(p);
        if (position == -1){
        	System.out.println(p.getNarrator().getPrivateEvents().access(Event.PRIVATE, false));
            throw new PlayerTargetingException(p + " not found\n" );
        }
        
        dScreen.onItemClick(null, null, position, 0);
    }

    private boolean swipeAbilityPanel(Player slave, String action){
        setActionPanel();
        int cycles = slave.getAbilities().length;
        
        int swipe;
        if(rand.nextBoolean())
        	swipe = SimpleGestureFilter.SWIPE_RIGHT;
        else
        	swipe = SimpleGestureFilter.SWIPE_LEFT;
        
        for (int i = 0; i < cycles; i++){
            if (dScreen.commandTV.getText().toString().equals(action))
                return true;
            else
                dScreen.onSwipe(swipe);
        }
        return false;
    }

    private void setActionPanel(){
        if (dScreen.panel != dScreen.actionButton){
            dScreen.onClick(dScreen.actionButton);
        }
    }
    private void setMessagePanel(){
    	if(dScreen.panel != dScreen.messagesButton)
    		dScreen.onClick(dScreen.messagesButton);
    }
    private void setInfoPanel(){
    	if (dScreen.panel != dScreen.infoButton){
    		dScreen.onClick(dScreen.infoButton);
    	}
    }

    protected void selectSlave(Player slave){
    	GUIController.selectScreen(dScreen, slave);
    }
    
    public void selectHost(Player host){
    	GUIController.selectScreen(dScreen, host);
        dScreen.onClick(dScreen.actionButton);
    }

    public static void selectScreen(ActivityDay dScreen, Player slf){
        if(dScreen.manager.getCurrentPlayer() == slf)
            return;

        View v = dScreen.findViewById(R.id.day_playerDrawerButton);
        dScreen.onClick(v);

        if (slf != null && slf.isDead()){
            throw new PlayerTargetingException(slf.getDescription() + " is dead and can't be selected on Player Click");
        }
        if(!dScreen.manager.getNarrator().getAllPlayers().has(slf))
        	throw new PlayerTargetingException("Player not part of game!");
        dScreen.onPlayerClick(slf);

        dScreen.closeDrawer();
        dScreen.onDrawerClosed(null);
    }

    public void actionPanelClick(){
        dScreen.onClick(dScreen.actionButton);
    }

    public void endNight(Player slave){
    	if(END_NIGHT)
    		logger.endNight(slave);
    	clickButton(slave);
    }

    public void cancelEndNight(Player slave){
    	logger.cancelEndNight(slave);
    	clickButton(slave);    	
    }
    
    public void clickButton(Player slave){
        selectSlave(slave);
        setActionPanel();
        dScreen.onClick(dScreen.button);
    }

    public void setFrame(String team){
        int id = dScreen.frameOptions.indexOf(team);
        dScreen.framerSpinner.setSelection(id);
    }

	public Narrator getNarrator() {
		return dScreen.manager.getNarrator();
	}

	public void log(String string) {
		dScreen.log(string);
	}
	
	public void say(Player slave, String message, String team){
		if(SAY)
			logger.say(slave, message, team);
		selectSlave(slave);
		setMessagePanel();
		dScreen.chatET.setText(message);
		dScreen.onClick(dScreen.chatButton);
	}


	public void doDayAction(Player p) {
		logger.doDayAction(p);
		selectSlave(p);
		setInfoPanel();
        dScreen.onClick(dScreen.button);
	}

	public void unvote(Player slave) {
		if(VOTE)
			logger.unvote(slave);
		selectSlave(slave);
        actionPanelClick();
        PlayerList pList = dScreen.getCheckedPlayers();
        clickPlayer(pList.get(0));
	}
	
	public void removeNightTarget(Player slave, String ability){
		logger.removeNightTarget(slave, ability);
		selectSlave(slave);
        swipeAbilityPanel(slave, ability);
        PlayerList pList = dScreen.getCheckedPlayers();
        clickPlayer(pList.get(0));
	}

	public void text(Player p, String message, boolean sync) {
		Log.i(p.toString(), message);
	}
}
