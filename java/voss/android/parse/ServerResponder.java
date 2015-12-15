package voss.android.parse;


import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;

import voss.android.setup.ActivityCreateGame;
import voss.android.setup.SetupListener;
import voss.android.setup.SetupManager;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.RoleTemplate;

public class ServerResponder implements SetupListener{

    private GameListing object;
    private ActivityCreateGame ac;
    public ServerResponder(GameListing gl, SetupManager manager){
        manager.addListener(ServerResponder.this);
        object = gl;
        this.ac = manager.screen;
    }

    public void onRoleRemove(RoleTemplate rt){
        if(ac.getManager().isHost())
            Server.RemoveRole(rt, object, ac);
    }
    public void onRoleAdd(RoleTemplate rt){
        if(ac.getManager().isHost())
            Server.AddRole(rt, object, ac);
    }
    public void onPlayerRemove(String n){
        //no player removals when talking with the server
    }
    public void onPlayerAdd(String n, Communicator c){
        //no player adding when going to the server
    }

    public void exitGame(){
        Server.LeaveGame(object);
    }
}
