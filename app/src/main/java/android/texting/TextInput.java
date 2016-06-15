package android.texting;

import shared.logic.Player;


public interface TextInput {

    void text(Player p, String message, boolean sync);
}
