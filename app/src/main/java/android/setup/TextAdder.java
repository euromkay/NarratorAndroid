package android.setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import android.parse.Server;
import android.texting.CommunicatorText;
import android.texting.PhoneNumber;
import android.texting.ReceiverText;
import shared.event.Event;
import shared.logic.Player;
import shared.logic.PlayerList;


public class TextAdder extends BroadcastReceiver{

    private SetupManager manager;
    public TextAdder(SetupManager manager){
        this.manager = manager;
    }

    public void onReceive(Context context, Intent intent){
        Log.e("TextAdder", "gotem");
        if (intent.getExtras().getString("number") == null){
            manager.updateNarrator(intent);
            return;
        }
        if(Server.IsLoggedIn())
            return;
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
        manager.addPlayer(name, new CommunicatorText(number));


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
        for(Player p: getTexters(allPlayers)){
            CommunicatorText ct = (CommunicatorText) p.getCommunicator();
            if(ct.getNumber().equals(number)) {
            	manager.removePlayer(p.getName(), false);
                manager.addPlayer(name, p.getCommunicator());
                p.sendMessage(Event.String("You are now " + name + ""));
                return true;
            }
        }
        return false;
    }

    public static PlayerList getTexters(PlayerList playerList){
        PlayerList list = new PlayerList();
        for(Player p: playerList){
            if(isTexter(p))
                list.add(p);
        }

        return list;
    }

    private static boolean isTexter(Player p){
        if (p.getCommunicator() == null)
            return false;
        return p.getCommunicator().getClass() == CommunicatorText.class;
    }

    private void nameTaken(PhoneNumber number){
        ReceiverText.sendText(number, "Name was already taken!");
    }
}
