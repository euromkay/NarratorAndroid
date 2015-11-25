package voss.android.setup;

import voss.shared.logic.Player;
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

    public void onPlayerAdd(Player p){
    	screen.toast(p.getName() + " joined.");
    }
    public void onPlayerRemove(Player p){
    	screen.toast(p.getName() + " left.");

    }
    public void onNameChange(Player p, String name){}
}
