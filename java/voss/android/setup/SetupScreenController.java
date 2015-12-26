package voss.android.setup;

import android.widget.Toast;

import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.RoleTemplate;

public class SetupScreenController implements SetupListener{

    private Toast toast;
    private ActivityCreateGame screen;
    public SetupScreenController(ActivityCreateGame a) {
        this.screen = a;
        toast = Toast.makeText(screen, "", Toast.LENGTH_SHORT);
    }


    public void onRoleAdd(RoleTemplate listing){
        screen.refreshRolesList();
    }
    public void onRoleRemove(RoleTemplate listing){ screen.refreshRolesList();}

    public void onPlayerAdd(String name, Communicator c){
        if(toast != null)
            toast.cancel();
        toast = screen.toast(name + " has joined.");
    }
    public void onPlayerRemove(String s){
    	screen.toast(s + " has left the lobby.");

    }
}
