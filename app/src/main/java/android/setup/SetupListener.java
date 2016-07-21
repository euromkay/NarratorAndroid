package android.setup;

import shared.logic.support.Communicator;
import shared.logic.support.RoleTemplate;

public interface SetupListener {
    void onRoleAdd(RoleTemplate s);
    void onRoleRemove(String roleName, String roleColor);

    void onPlayerAdd(String name, Communicator c);
    void onPlayerRemove(String name);
}
