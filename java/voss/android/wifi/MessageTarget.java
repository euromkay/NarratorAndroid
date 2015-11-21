package voss.android.wifi;

import android.os.Handler;

/**
 * Created by Michael on 9/8/2015.
 */
public interface MessageTarget {

    void addHandler(Handler h, ChatManager.ChatListener c);
    void write(String s);
    void onChatReady();
}
