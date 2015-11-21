package voss.android.alerts;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import voss.android.R;
import voss.android.setup.ActivityCreateGame;
import voss.android.setup.SetupListener;
import voss.android.wifi.CommunicatorInternet;
import voss.logic.Narrator;
import voss.logic.Player;
import voss.logic.PlayerList;
import voss.logic.support.CommunicatorNull;
import voss.logic.support.RoleTemplate;


public class PlayerPopUp extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SetupListener{


    public View mainView;
    ListView lv;
    public PlayerList players;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        //todo what happens first, oncreate or on attach

        mainView = inflater.inflate(R.layout.create_player_list, container);

        lv = (ListView) mainView.findViewById(R.id.listView1);

        ArrayAdapter<String> adapter = getAdapter(players.getNamesToStringArray());
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
        setTitle();
        mainView.findViewById(R.id.addPlayerConfirm).setOnClickListener(this);

        return mainView;
    }

    public void setTitle(){
        getDialog().setTitle("Players in Game - " + mListener.getNarrator().getPlayerCount());
    }

    public void onItemClick(AdapterView<?> a, View v, int i, long l){
        Player clicked = players.get(i);

        if(clicked.getCommunicator().getClass() == CommunicatorInternet.class)
            return;
        if(clicked.getCommunicator().getClass() == CommunicatorNull.class)
            return;
        if(players.get(i).isComputer() || !activity.getManager().isHost()) {
            activity.getManager().removePlayer(clicked);
        }else{
            TextView tv = (TextView) v;
            tv.setTextColor(ActivityCreateGame.parseColor(activity, R.color.redBlood));
            clicked.setComputer();
        }

    }

    private ArrayAdapter<String> getAdapter(String[] players){
        return new ArrayAdapter<>(getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, players);
    }


    public void onClick(View v) {
        EditText et = (EditText) mainView.findViewById(R.id.addPlayerContent);
        String name = et.getText().toString();
        if (name.length() == 0)
            return;

        if (checkName(name, et))
            return;

        if (mListener.getNarrator().getAllPlayers().hasName(name)){
            Toast.makeText(getActivity(), "Name taken", Toast.LENGTH_LONG).show();
            et.setText("");
            return;
        }


        activity.getManager().addPlayer(name);

        et.setText("");
    }

    public void updatePlayerList(){
        players = mListener.getNarrator().getAllPlayers();
        lv.setAdapter(getAdapter(players.getNamesToStringArray()));
    }

    public void pushPlayersDown(){
        lv.post(new Runnable() {
            public void run() {
                lv.setSelection(players.size()-1);
            }
        });
    }



    private boolean checkName(String name, EditText et){
        if (name.toLowerCase().equals("cancel") || name.toLowerCase().equals("skip")){
            Toast.makeText(getActivity(), "don't use this name", Toast.LENGTH_LONG).show();
            et.setText("");
            return true;
        }
        return false;
    }

    public interface AddPlayerListener {
        Narrator getNarrator();
        void onPopUpDismiss();
        void onPopUpCreate(PlayerPopUp p);
    }

    private AddPlayerListener mListener;

    private ActivityCreateGame activity;
    public void onAttach(Activity a){
        super.onAttach(a);
        this.activity = (ActivityCreateGame) a;
        activity.getManager().addListener(this);
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (AddPlayerListener) a;
            mListener.onPopUpCreate(this);
            players = mListener.getNarrator().getAllPlayers();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(a.toString()
                    + " must implement AddPlayerListenerListener");
        }
    }

    public void onDismiss(final DialogInterface arg0) {
        mListener.onPopUpDismiss();
        activity.getManager().removeListener(this);
        super.onDismiss(arg0);
    }


    public void onRoleAdd(RoleTemplate l){}
    public void onRoleRemove(RoleTemplate l){}

    public void onPlayerAdd(Player p){
        updatePlayerList();
        pushPlayersDown();
        setTitle();
    }

    public void onPlayerRemove(Player p){
        updatePlayerList();
        setTitle();
    }

    public void onNameChange(Player p, String name){
        updatePlayerList();
    }
}
