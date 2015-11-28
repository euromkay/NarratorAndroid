package voss.android.setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import voss.android.texting.CommunicatorText;
import voss.android.texting.PhoneNumber;
import voss.android.texting.ReceiverText;
import voss.android.texting.TextHandler;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;


public class TextAdder extends BroadcastReceiver{

    private SetupManager manager;
    public TextAdder(SetupManager manager){
        this.manager = manager;
    }

    public void onReceive(Context context, Intent intent){
        if (intent.getExtras().getString("number") == null){
            manager.updateNarrator(intent);
            return;
        }
        PhoneNumber number = new PhoneNumber(intent.getExtras().getString("number"));
        String name = intent.getExtras().getString("message");


        if(weirdName(name, number)) {
            manager.toast("Someone sent a text or did a weird name");
            //don't add it
            return;
        }if(duplicateNumber(name, number)) {
            manager.toast("duplicate number");
            //don't add it, change it
            //abortBroadcast();
            return;

        }if(name.toLowerCase().equals("cancel")){
            ReceiverText.sendText(number, "don't use that name");
            //abortBroadcast();
            return;
        }

        if(name.toLowerCase().equals("skip")){
            ReceiverText.sendText(number, "don't use that name");
            //abortBroadcast();
            return;
        }

        if(manager.getNarrator().getAllPlayers().hasName(name)) {
            nameTaken(number);
            //abortBroadcast();
            return;
        }
        //abortBroadcast();
        manager.addPlayer(name, number);


    }

    private boolean weirdName(String name, PhoneNumber number){
        if(name.length() > 50){
            ReceiverText.sendText(number, "I'm running an app using my texts.  Please facebook me, or call me if urgent");
            return true;
        }

        int i = 0;
        while(name.contains(" ")){
            name = name.replaceFirst(" ", "");
            i++;
        }
        if( i > 3 ){
            ReceiverText.sendText(number, "I'm running an app using my texts.  Please facebook me, or call me if urgent!");
            return true;
        }
        return false;
    }

    private boolean duplicateNumber(String name, PhoneNumber number){
        PlayerList allPlayers = manager.getNarrator().getAllPlayers();
        if(allPlayers.hasName(name)){
            nameTaken(number);
            return true;
        }
        for(Player p: TextHandler.getTexters(allPlayers)){
            if(((CommunicatorText) p.getCommunicator()).getNumber().equals(number)) {
                manager.changeName(p, name);
                p.sendMessage("You are now " + name + ".");
                return true;
            }
        }
        return false;
    }

    private void nameTaken(PhoneNumber number){
        ReceiverText.sendText(number, "Name was already taken!");
    }
}
