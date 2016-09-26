package android;

import java.util.ArrayList;

import android.day.ActivityDay;
import android.screens.SimpleGestureFilter;
import android.texting.TextController;
import android.texting.TextInput;
import android.util.Log;
import android.view.View;
import shared.ai.Controller;
import shared.logic.Player;
import shared.logic.exceptions.PlayerTargetingException;
import shared.logic.support.Random;
import shared.roles.Framer;
import shared.roles.Role;
import voss.narrator.R;

public class GUIController extends Controller implements TextInput{

    public ActivityDay dScreen;
    public Random rand;
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
        	clickPlayer(target.getName()  );
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
    	if(slave.getActions().isTargeting(choice, slave.parseAbility(ability)))
    		return;
    	if(TARGET && !ability.toLowerCase().equals(Framer.FRAME.toLowerCase())){
    		logger.setNightTarget(slave, choice, ability);
    	}
    	selectSlave(slave);
        swipeAbilityPanel(ability);
        clickPlayer(choice.getName());
    }

    public void setNightTarget(Player slave, Player choice, String ability, String teamName){
    	if(TARGET)
    		logger.setNightTarget(slave, choice, ability, teamName);
        setFrame(teamName);
        setNightTarget(slave,choice,ability);
    }

    private void clickPlayer(String p){
        int position = dScreen.actionList.indexOf(p);
        if (position == -1){
            throw new PlayerTargetingException(p + " not found\n" );
        }
        
        dScreen.onItemClick(null, null, position, 0);
    }

    public boolean swipeAbilityPanel(String action){
        setActionPanel();
        
        int swipe;
        if(rand.nextBoolean())
        	swipe = SimpleGestureFilter.SWIPE_RIGHT;
        else
        	swipe = SimpleGestureFilter.SWIPE_LEFT;
        
        for (int i = 0; i < Role.MAIN_ABILITY; i++){
            if (dScreen.commandTV.getText().toString().equals(action))
                return true;
            else
                dScreen.onSwipe(swipe);
        }
        return false;
    }

    public void clearTargets(Player p){}

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

    public void selectSlave(Player slave){
    	GUIController.selectScreen(dScreen, slave.getName());
    }
    
    public void selectHost(Player host){
    	GUIController.selectScreen(dScreen, host.getName());
        dScreen.onClick(dScreen.actionButton);
    }

    public static void selectScreen(ActivityDay dScreen, String slf){
        if(slf.equals(dScreen.manager.getCurrentPlayer()))
            return;

        View b = dScreen.findViewById(R.id.day_playerDrawerButton);
        dScreen.onClick(b);

        //if (slf != null && slf.isDead()){
        //    throw new PlayerTargetingException(slf.getDescription() + " is dead and can't be selected on Player Click");
        //}
        //if(!dScreen.manager.getNarrator().getAllPlayers().has(slf))
        	//throw new PlayerTargetingException("Player not part of game!");
        dScreen.onPlayerClick(slf);

        dScreen.closeDrawer();
        dScreen.onDrawerClosed(null);
    }

    public void actionPanelClick(){
        dScreen.onClick(dScreen.actionButton);
    }
    public void infoPanelClick(){
        dScreen.onClick(dScreen.infoButton);
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
        ArrayList<String> pList = dScreen.getCheckedPlayers();
        clickPlayer(pList.get(0));
	}
	
	public void cancelNightTarget(Player slave, Player target, String ability){
		logger.cancelNightTarget(slave, target, ability);
		selectSlave(slave);
        swipeAbilityPanel(ability);
        ArrayList<String> pList = dScreen.getCheckedPlayers();
        clickPlayer(pList.get(0));
	}

	public void text(Player p, String message, boolean sync) {
		Log.i(p.toString(), message);
	}
	
	public void doDayAction(Player slave, Player target){
		logger.doDayAction(slave, target);
		selectSlave(slave);
		swipeAbilityPanel(target.getDayAbility().get(0));
	}
}
