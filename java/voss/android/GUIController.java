package voss.android;

import android.view.View;

import java.util.Random;

import voss.ai.Controller;
import voss.android.R;
import voss.android.day.ActivityDay;
import voss.android.screens.SimpleGestureFilter;
import voss.logic.Narrator;
import voss.logic.Player;
import voss.logic.exceptions.PlayerTargetingException;

public class GUIController implements Controller{

    private ActivityDay dScreen;
    private Random rand;
    public GUIController(ActivityDay dScreen){
        this.dScreen = dScreen;
        rand = new Random();
    }

    public void vote(Player slave, Player target){
        selectSlave(slave);
        actionPanelClick();

        clickPlayer(target);
    }

    public void skipVote(Player slave){
        vote(slave, slave.getSkipper());
    }

    public void setNightTarget(Player slave, Player choice, String ability){
    	selectSlave(slave);
        swipeAbilityPanel(slave, ability);
        clickPlayer(choice);
    }

    public void setNightTarget(Player slave, Player choice, String ability, String teamName){
        setFrame(slave, choice, teamName);
        setNightTarget(slave,choice,ability);
    }

    private void clickPlayer(Player p){
        int position = dScreen.actionList.indexOf(p);
        if (position == -1)
            throw new PlayerTargetingException(p + " not found\n" );
        
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
            if (dScreen.commandsTV.getText().toString().equals(action))
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
        dScreen.onPlayerClick(slf);

        dScreen.closeDrawer();

        dScreen.onDrawerClosed(null);
    }

    public void actionPanelClick(){
        dScreen.onClick(dScreen.actionButton);
    }

    public void endNight(Player slave){
    	selectSlave(slave);
        dScreen.onClick(dScreen.button);
    }

    public void cancelEndNight(Player slave){
        selectSlave(slave);
        dScreen.onClick(dScreen.button);
    }

    public void setFrame(Player slave, Player target, String team){
        int id = dScreen.frameOptions.indexOf(team);
        dScreen.framerSpinner.setSelection(id);
    }

	public Narrator getNarrator() {
		return dScreen.manager.getNarrator();
	}

	public void log(String string) {
		dScreen.log(string);
	}
	
	public void say(Player slave, String message){
		selectSlave(slave);
		setMessagePanel();
		dScreen.chatET.setText(message);
		dScreen.onClick(dScreen.chatButton);
	}
}
