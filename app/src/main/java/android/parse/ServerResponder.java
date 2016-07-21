package android.parse;



import android.setup.ActivityCreateGame;
import android.setup.SetupListener;
import android.setup.SetupManager;
import shared.logic.support.Communicator;
import shared.logic.support.RoleTemplate;

public class ServerResponder implements SetupListener{

    private GameListing object;
    private ActivityCreateGame ac;
    public ServerResponder(GameListing gl, SetupManager manager){
        manager.addListener(ServerResponder.this);
        object = gl;
        this.ac = manager.screen;
    }

    public void onRoleRemove(String roleName, String roleColor){
        if(ac.getManager().isHost())
            Server.RemoveRole(roleName, roleColor, object, ac);
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
