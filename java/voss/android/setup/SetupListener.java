package voss.android.setup;

import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.RoleTemplate;

public interface SetupListener {
    void onRoleAdd(RoleTemplate s);
    void onRoleRemove(RoleTemplate s);

    void onPlayerAdd(String name, Communicator c);
    void onPlayerRemove(String name);
}
