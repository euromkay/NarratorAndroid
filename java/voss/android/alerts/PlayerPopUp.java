package voss.android.alerts;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import voss.android.CommunicatorPhone;
import voss.android.NActivity;
import voss.android.R;
import voss.android.parse.Server;
import voss.android.setup.ActivityCreateGame;
import voss.android.setup.SetupListener;
import voss.android.wifi.CommunicatorInternet;
import voss.shared.logic.Narrator;
import voss.shared.logic.Player;
import voss.shared.logic.PlayerList;
import voss.shared.logic.support.Communicator;
import voss.shared.logic.support.CommunicatorNull;
import voss.shared.logic.support.RoleTemplate;


public class PlayerPopUp extends DialogFragment implements View.OnClickListener, AdapterView.OnItemClickListener, SetupListener{


    public View mainView;
    ListView lv;
    public PlayerList players;
    private boolean first = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        mainView = inflater.inflate(R.layout.create_player_list, container);

        lv = (ListView) mainView.findViewById(R.id.listView1);

        ListAdapter adapter = getAdapter(players);
        lv.setAdapter(adapter);
        if (Server.IsLoggedIn()) {
            mainView.findViewById(R.id.addPlayerContent).setVisibility(View.GONE);
            mainView.findViewById(R.id.addPlayerConfirm).setVisibility(View.GONE);
        }else {
            lv.setOnItemClickListener(this);
            mainView.findViewById(R.id.addPlayerConfirm).setOnClickListener(this);
        }
        setTitle();

        EditText edit_txt = (EditText) mainView.findViewById(R.id.addPlayerContent);

        edit_txt.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(null);
                    return true;
                }
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    onClick(null);
                    return true;
                }
                return false;
            }
        });

        return mainView;
    }

    public void setTitle(){
        getDialog().setTitle("Players in Game - " + mListener.getNarrator().getPlayerCount());
    }

    public void onItemClick(AdapterView<?> a, View v, int i, long l){
        Player clicked = players.get(i);

        if(clicked.getCommunicator().getClass() == CommunicatorInternet.class)
            return;
        if(clicked.getCommunicator().getClass() == CommunicatorNull.class){
            if(clicked.isComputer())
                activity.getManager().removePlayer(clicked.getName(), false);//gotta be a host delete
            else
                return;
        }
        if(clicked.isComputer()){
        	activity.getManager().removePlayer(clicked.getName(), false);//gotta be a host delete
        }else if(!activity.getManager().isHost()) {
        	activity.getManager().requestRemovePlayer(clicked.getName());
        }else {
            TextView tv = (TextView) v;
            tv.setTextColor(ActivityCreateGame.ParseColor(activity, R.color.redBlood));
            clicked.setComputer();
            if (first){
                activity.toast(clicked.getName() + " is now a computer. Click again to delete.");
                first = false;
            }
        }
    }

    private ListAdapter getAdapter(PlayerList players){
        return new ListAdapter(players, activity);
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


        activity.getManager().addPlayer(name, new CommunicatorPhone());

        et.setText("");
    }

    public void updatePlayerList(){
        players = mListener.getNarrator().getAllPlayers();
        lv.setAdapter(getAdapter(players));
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
            mListener = (AddPlayerListener) a;
            mListener.onPopUpCreate(this);
            players = mListener.getNarrator().getAllPlayers();
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString() + " must implement AddPlayerListenerListener");
        }
    }

    public void onDismiss(final DialogInterface arg0) {
        mListener.onPopUpDismiss();
        activity.getManager().removeListener(this);
        super.onDismiss(arg0);
    }


    public void onRoleAdd(RoleTemplate l){}
    public void onRoleRemove(RoleTemplate l){}

    public void onPlayerAdd(String name, Communicator c){
        updatePlayerList();
        pushPlayersDown();
        setTitle();
    }

    public void onPlayerRemove(String name){
        updatePlayerList();
        setTitle();
    }


    public class ListAdapter extends BaseAdapter{

        private PlayerList data;
        private NActivity c;
        private Typeface font;

        public ListAdapter(PlayerList data, NActivity c){
            this.data = data;
            this.c = c;
            font = Typeface.createFromAsset(c.getAssets(), "JosefinSans-Regular.ttf");
        }

        public int getCount() {
            return data.size();
        }

        public String getItem(int position) {
            return data.get(position).getName();
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView result;

            if (convertView == null) {
                result = (TextView) c.getLayoutInflater().inflate(R.layout.create_roles_right_item, parent, false);
            } else {
                result = (TextView) convertView;
            }

            Player p = data.get(position);
            String name = p.getName();
            int color;
            if (p.isComputer()){
                color = getColor(R.color.redBlood);
            }else{
                color = getColor(R.color.white);
            }
            result.setText(name);
            //result.setTypeface(font);
            result.setTextColor(color);

            return result;


        }

        private int getColor(int id){
            return NActivity.ParseColor(c, id);
        }
    }
}