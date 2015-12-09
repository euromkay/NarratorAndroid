package voss.android.setup;

import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.RoleTemplate;

public class SetupScreenController implements SetupListener{

    private ActivityCreateGame screen;
    public SetupScreenController(ActivityCreateGame a) {
        this.screen = a;
    }


    public void onRoleAdd(RoleTemplate listing){
        screen.refreshRolesList();
    }
    public void onRoleRemove(RoleTemplate listing){ screen.refreshRolesList();}

    public void onPlayerAdd(String name, Communicator c){
    	screen.toast(name + " joined.");
    }
    public void onPlayerRemove(String s){
    	screen.toast(s + " left.");

    }
}
