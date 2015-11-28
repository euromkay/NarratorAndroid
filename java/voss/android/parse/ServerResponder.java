package voss.android.parse;


import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import voss.android.setup.ActivityCreateGame;
import voss.android.setup.SetupListener;
import voss.android.setup.SetupManager;
import voss.shared.logic.Player;
import voss.shared.logic.support.RoleTemplate;

public class ServerResponder implements SetupListener{

    private GameListing object;
    private ActivityCreateGame ac;
    public ServerResponder(String id, final SetupManager manager){
        final ActivityCreateGame ac = manager.screen;
        Server.GetNarratorInfo(id, new GetCallback<ParseObject>(){
            public void done(ParseObject retObject, ParseException e) {
                if (e == null) {
                    object = new GameListing(retObject);
                    if (Server.GetCurrentUserName().equals(object.getHostName())){
                        manager.addListener(ServerResponder.this);
                    }
                }else{
                    ac.toast(e.getMessage());
                }
            }
        });
    }

    public void onRoleRemove(RoleTemplate rt){
        Server.RemoveRole(rt, object, ac);
    }
    public void onRoleAdd(RoleTemplate rt){
        Server.AddRole(rt, object, ac);
    }
    public void onPlayerRemove(Player p){
        //no player removals when talking with the server
    }
    public void onPlayerAdd(Player p){
        //no player adding when going to the server
    }

    public void onNameChange(Player p, String s){
        //no name changes when going to the server
    }

    public void exitGame(){
        Server.LeaveGame(object);
    }
}
