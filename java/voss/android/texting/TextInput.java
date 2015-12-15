package voss.android.texting;

import voss.shared.logic.Player;


public interface TextInput {

    void text(Player p, String message, boolean sync);
}
