package voss.android.setup;

import voss.logic.Player;
import voss.logic.support.RoleTemplate;

public interface SetupListener {
    void onRoleAdd(RoleTemplate s);
    void onRoleRemove(RoleTemplate s);

    void onPlayerAdd(Player p);
    void onPlayerRemove(Player p);
    void onNameChange(Player p, String name);
}
